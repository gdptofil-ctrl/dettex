package main

import (
	"bufio"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
)

// Account is who plays.
type Account struct {
	Name        string `json:"name"`
	UUID        string `json:"uuid"` // without dashes, as Mojang returns it
	AccessToken string `json:"-"`
	XUID        string `json:"xuid,omitempty"`
	Offline     bool   `json:"offline,omitempty"` // nickname-only account, no licence
}

type LaunchOptions struct {
	RAMMB     int
	Width     int
	Height    int
	ExtraJVM  []string
	QuickPlay string // server address to join right away (optional)
}

// gcFlags: tuned G1 settings for a smooth client (short pauses, young gen sized for MC's allocation rate).
var gcFlags = []string{
	"-XX:+UnlockExperimentalVMOptions",
	"-XX:+UseG1GC",
	"-XX:G1NewSizePercent=20",
	"-XX:G1ReservePercent=20",
	"-XX:MaxGCPauseMillis=50",
	"-XX:G1HeapRegionSize=32M",
	"-XX:+ParallelRefProcEnabled",
	"-XX:+DisableExplicitGC",
	"-XX:+UseStringDeduplication",
	"-XX:+PerfDisableSharedMem",
}

func dashedUUID(u string) string {
	u = strings.ReplaceAll(u, "-", "")
	if len(u) != 32 {
		return u
	}
	return u[0:8] + "-" + u[8:12] + "-" + u[12:16] + "-" + u[16:20] + "-" + u[20:]
}

// BuildCommand returns java + arguments for the installed game.
func BuildCommand(in *Installed, acc Account, opt LaunchOptions) (string, []string) {
	l := in.Layout
	versionName := MCVersion
	mainClass := in.Vanilla.MainClass
	if in.Fabric != nil {
		versionName = in.Fabric.ID
		mainClass = in.Fabric.MainClass
	}
	sep := string(os.PathListSeparator)
	userType := "msa"
	if acc.Offline {
		userType = "legacy"
	}
	vars := map[string]string{
		"auth_player_name":     acc.Name,
		"version_name":         versionName,
		"game_directory":       l.Game(),
		"assets_root":          l.Assets(),
		"assets_index_name":    in.AssetIndex,
		"auth_uuid":            dashedUUID(acc.UUID),
		"auth_access_token":    acc.AccessToken,
		"clientid":             "",
		"auth_xuid":            acc.XUID,
		"user_type":            userType,
		"version_type":         "release",
		"natives_directory":    l.Natives(),
		"launcher_name":        "DettexLauncher",
		"launcher_version":     "1.0",
		"classpath":            strings.Join(in.Classpath, sep),
		"classpath_separator":  sep,
		"library_directory":    l.Libraries(),
		"resolution_width":     strconv.Itoa(opt.Width),
		"resolution_height":    strconv.Itoa(opt.Height),
		"quickPlayMultiplayer": opt.QuickPlay,
	}
	features := map[string]bool{
		"has_custom_resolution":     opt.Width > 0 && opt.Height > 0,
		"is_quick_play_multiplayer": opt.QuickPlay != "",
	}
	subst := func(s string) string {
		for {
			i := strings.Index(s, "${")
			if i < 0 {
				return s
			}
			j := strings.Index(s[i:], "}")
			if j < 0 {
				return s
			}
			key := s[i+2 : i+j]
			s = s[:i] + vars[key] + s[i+j+1:]
		}
	}
	expand := func(list []Arg) []string {
		var out []string
		for _, a := range list {
			if !allowed(a.Rules, features) {
				continue
			}
			for _, v := range a.Values {
				out = append(out, subst(v))
			}
		}
		return out
	}

	ram := opt.RAMMB
	if ram < 1024 {
		ram = 4096
	}
	var args []string
	args = append(args, "-Xmx"+strconv.Itoa(ram)+"M", "-Xms"+strconv.Itoa(min(ram, 1024))+"M")
	args = append(args, gcFlags...)
	args = append(args, "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8")
	args = append(args, opt.ExtraJVM...)
	if in.LogConfig != "" && in.Vanilla.Logging.Client.Argument != "" {
		args = append(args, strings.ReplaceAll(in.Vanilla.Logging.Client.Argument, "${path}", in.LogConfig))
	}
	args = append(args, expand(in.Vanilla.Arguments.JVM)...)
	if in.Fabric != nil {
		args = append(args, expand(in.Fabric.Arguments.JVM)...)
	}
	args = append(args, mainClass)
	args = append(args, expand(in.Vanilla.Arguments.Game)...)
	if in.Fabric != nil {
		args = append(args, expand(in.Fabric.Arguments.Game)...)
	}
	return in.Java, args
}

// Game is a running Minecraft.
type Game struct {
	cmd     *exec.Cmd
	mu      sync.Mutex
	tail    []string
	Started chan struct{} // the game window is up
	Exited  chan error
}

// Launch starts the game; output goes to game/logs/launcher.log, the last lines are kept for errors.
func Launch(in *Installed, acc Account, opt LaunchOptions) (*Game, error) {
	java, args := BuildCommand(in, acc, opt)
	if _, err := os.Stat(java); err != nil {
		return nil, fmt.Errorf("Java не найдена: %s", java)
	}
	_ = os.MkdirAll(filepath.Join(in.Layout.Game(), "logs"), 0o755)
	logf, err := os.Create(filepath.Join(in.Layout.Game(), "logs", "launcher.log"))
	if err != nil {
		return nil, err
	}
	cmd := exec.Command(java, args...)
	cmd.Dir = in.Layout.Game()
	hideConsole(cmd)
	stdout, _ := cmd.StdoutPipe()
	cmd.Stderr = cmd.Stdout
	g := &Game{cmd: cmd, Started: make(chan struct{}), Exited: make(chan error, 1)}
	if err := cmd.Start(); err != nil {
		logf.Close()
		return nil, err
	}
	var once sync.Once
	go func() {
		sc := bufio.NewScanner(stdout)
		sc.Buffer(make([]byte, 64<<10), 1<<20)
		w := io.Writer(logf)
		for sc.Scan() {
			line := sc.Text()
			fmt.Fprintln(w, line)
			g.mu.Lock()
			g.tail = append(g.tail, line)
			if len(g.tail) > 60 {
				g.tail = g.tail[len(g.tail)-60:]
			}
			g.mu.Unlock()
			// these lines appear when the window and the renderer are up
			if strings.Contains(line, "Backend library:") || strings.Contains(line, "Sound engine started") || strings.Contains(line, "Created:") {
				once.Do(func() { close(g.Started) })
			}
		}
	}()
	go func() {
		err := cmd.Wait()
		logf.Close()
		once.Do(func() { close(g.Started) })
		g.Exited <- err
	}()
	return g, nil
}

// Tail returns the last lines the game printed (for crash messages).
func (g *Game) Tail(n int) []string {
	g.mu.Lock()
	defer g.mu.Unlock()
	if n > len(g.tail) {
		n = len(g.tail)
	}
	return append([]string(nil), g.tail[len(g.tail)-n:]...)
}

func (g *Game) Kill() {
	if g.cmd.Process != nil {
		_ = g.cmd.Process.Kill()
	}
}
