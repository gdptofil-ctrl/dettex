package main

import (
	"archive/zip"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"sync"
	"time"
)

const launcherVersion = "1.0.0"

type Settings struct {
	RAMMB         int    `json:"ramMB"`
	PerfMods      bool   `json:"perfMods"`
	CloseOnLaunch bool   `json:"closeOnLaunch"`
	Server        string `json:"server"` // join this server right away (optional)
}

// App is everything the window talks to.
type App struct {
	mu        sync.Mutex
	layout    Layout
	settings  Settings
	session   *Session
	license   *licenseRecord
	busy      bool
	loginBusy bool
	cancel    context.CancelFunc
	game      *Game
	exeDir    string
	// the client carried inside the exe (extracted once)
	embeddedOnce sync.Once
	embeddedJar  string

	// set by the window
	emit          func(event string, payload any)
	onGameStarted func()
	onGameExited  func(err error, tail []string, early bool)
}

func NewApp(exeDir string) *App {
	a := &App{layout: Layout{Root: dataRoot()}, exeDir: exeDir, emit: func(string, any) {}}
	a.settings = Settings{RAMMB: defaultRAM(), PerfMods: true, CloseOnLaunch: true}
	a.loadSettings()
	a.loadSession()
	a.loadLicense()
	return a
}

func defaultRAM() int {
	total := totalRAMMB()
	switch {
	case total >= 16000:
		return 6144
	case total >= 12000:
		return 4096
	case total >= 8000:
		return 3072
	default:
		return 2048
	}
}

func (a *App) settingsPath() string { return filepath.Join(a.layout.Root, "launcher.json") }
func (a *App) sessionPath() string  { return filepath.Join(a.layout.Root, "account.dat") }

func (a *App) loadSettings() {
	if b, err := os.ReadFile(a.settingsPath()); err == nil {
		_ = json.Unmarshal(b, &a.settings)
	}
	if a.settings.RAMMB < 1024 {
		a.settings.RAMMB = defaultRAM()
	}
}

func (a *App) saveSettings() {
	_ = os.MkdirAll(a.layout.Root, 0o755)
	b, _ := json.MarshalIndent(a.settings, "", "  ")
	_ = os.WriteFile(a.settingsPath(), b, 0o644)
}

func (a *App) loadSession() {
	b, err := os.ReadFile(a.sessionPath())
	if err != nil {
		return
	}
	plain, err := unprotect(b)
	if err != nil {
		return
	}
	var s Session
	if json.Unmarshal(plain, &s) == nil && (s.Offline || s.RefreshToken != "") {
		a.session = &s
	}
}

func (a *App) saveSession() {
	_ = os.MkdirAll(a.layout.Root, 0o755)
	if a.session == nil {
		_ = os.Remove(a.sessionPath())
		return
	}
	b, _ := json.Marshal(a.session)
	if enc, err := protect(b); err == nil {
		_ = os.WriteFile(a.sessionPath(), enc, 0o600)
	}
}

// embeddedClient: the client jar built into this exe (pack-launcher.bat), extracted to the cache.
func (a *App) embeddedClient() string {
	a.embeddedOnce.Do(func() {
		exe, err := os.Executable()
		if err != nil {
			return
		}
		if j, err := extractEmbeddedClient(exe, filepath.Join(a.layout.Cache(), "client")); err == nil {
			a.embeddedJar = j
		}
	})
	return a.embeddedJar
}

// findClientJar: the client built into the exe first; otherwise DettexClient.jar next to the
// launcher, or a RecoinDLC-*.jar from the project's build/libs (development only).
// Only known client locations are searched — a random jar in the same folder must never be
// installed as the client.
func (a *App) findClientJar() string {
	if j := a.embeddedClient(); j != "" {
		return j
	}
	if f := filepath.Join(a.exeDir, "DettexClient.jar"); fileExists(f) && isDettexClientJar(f) {
		return f
	}
	var cands []string
	dirs := []string{
		filepath.Join(a.exeDir, "build", "libs"),
		filepath.Join(a.exeDir, "..", "build", "libs"),
		// the launcher on the desktop next to the project folder (Desktop\dettex\build\libs)
		filepath.Join(a.exeDir, "dettex", "build", "libs"),
	}
	for _, dir := range dirs {
		m, _ := filepath.Glob(filepath.Join(dir, "RecoinDLC-*.jar"))
		for _, f := range m {
			low := strings.ToLower(filepath.Base(f))
			if strings.HasSuffix(low, "-sources.jar") || strings.HasSuffix(low, "-dev.jar") {
				continue
			}
			if !isDettexClientJar(f) {
				continue
			}
			cands = append(cands, f)
		}
	}
	if len(cands) == 0 {
		return ""
	}
	sort.Slice(cands, func(i, j int) bool { return modTime(cands[i]).After(modTime(cands[j])) })
	return cands[0]
}

// isDettexClientJar tells the real client apart from other mod jars that may lie next to
// the launcher (Baritone, Fabric API, a random test jar...). Without this check a stray
// jar could be installed as DettexClient.jar and the game would start without the client.
func isDettexClientJar(path string) bool {
	z, err := zip.OpenReader(path)
	if err != nil {
		return false
	}
	defer z.Close()
	for _, f := range z.File {
		name := strings.ToLower(f.Name)
		if strings.HasPrefix(name, "ru/recoin/") {
			return true
		}
		if name != "fabric.mod.json" {
			continue
		}
		rc, err := f.Open()
		if err != nil {
			return false
		}
		b, _ := io.ReadAll(io.LimitReader(rc, 1<<20))
		rc.Close()
		var meta struct {
			ID   string `json:"id"`
			Name string `json:"name"`
		}
		if json.Unmarshal(b, &meta) != nil {
			continue
		}
		id, modName := strings.ToLower(meta.ID), strings.ToLower(meta.Name)
		if id == "insidethesystem" || strings.Contains(id, "dettex") || strings.Contains(id, "recoin") || strings.Contains(modName, "dettex") {
			return true
		}
	}
	return false
}

func fileExists(p string) bool {
	st, err := os.Stat(p)
	return err == nil && !st.IsDir()
}

func modTime(p string) time.Time {
	st, err := os.Stat(p)
	if err != nil {
		return time.Time{}
	}
	return st.ModTime()
}

// ------------------------------------------------------------------ calls from the page

type State struct {
	Version   string        `json:"version"`
	MC        string        `json:"mc"`
	Account   *Account      `json:"account"`
	License   *LicenseState `json:"license,omitempty"`
	Settings  Settings      `json:"settings"`
	MaxRAM    int           `json:"maxRam"`
	Jar       string        `json:"jar"`
	Busy      bool          `json:"busy"`
	LoginBusy bool          `json:"loginBusy"`
	Playing   bool          `json:"playing"`
	Installed bool          `json:"installed"`
}

func (a *App) State() State {
	a.mu.Lock()
	defer a.mu.Unlock()
	st := State{Version: launcherVersion, MC: MCVersion, Settings: a.settings, MaxRAM: totalRAMMB(), Busy: a.busy, LoginBusy: a.loginBusy, Playing: a.game != nil}
	st.License = a.licenseState(time.Now())
	if a.session != nil {
		acc := a.session.Account()
		st.Account = &acc
	}
	if j := a.findClientJar(); j != "" {
		st.Jar = filepath.Base(j)
		if j == a.embeddedJar {
			st.Jar = "встроен в лаунчер"
		}
	}
	st.Installed = fileExists(filepath.Join(a.layout.Versions(), MCVersion, MCVersion+".jar"))
	return st
}

func (a *App) SetSetting(key string, value any) {
	a.mu.Lock()
	switch key {
	case "ramMB":
		if f, ok := value.(float64); ok {
			a.settings.RAMMB = int(f)
		}
	case "perfMods":
		a.settings.PerfMods, _ = value.(bool)
	case "closeOnLaunch":
		a.settings.CloseOnLaunch, _ = value.(bool)
	case "server":
		s, _ := value.(string)
		a.settings.Server = strings.TrimSpace(s)
	}
	a.saveSettings()
	a.mu.Unlock()
}

// FinishLogin is called with the URL Microsoft redirected to.
func (a *App) FinishLogin(redirect string) {
	code, err := CodeFromRedirect(redirect)
	if err != nil {
		a.emit("error", err.Error())
		return
	}
	a.mu.Lock()
	a.loginBusy = true
	a.mu.Unlock()
	a.emit("state", a.State())
	go func() {
		s, err := SignIn(context.Background(), code, "")
		a.mu.Lock()
		a.loginBusy = false
		if err == nil {
			a.session = s
			a.saveSession()
		}
		a.mu.Unlock()
		if err != nil {
			a.emit("error", "Вход не удался: "+err.Error())
		} else {
			a.emit("toast", "Добро пожаловать, "+s.Name+"!")
		}
		a.emit("state", a.State())
	}()
}

// OfflineLogin signs in with just a nickname (no licence, for offline servers).
func (a *App) OfflineLogin(name string) {
	s, err := OfflineSession(name)
	if err != nil {
		a.emit("error", err.Error())
		return
	}
	a.mu.Lock()
	a.session = s
	a.saveSession()
	a.mu.Unlock()
	a.emit("toast", "Вход без лицензии: "+s.Name)
	a.emit("state", a.State())
}

func (a *App) Logout() {
	a.mu.Lock()
	a.session = nil
	a.saveSession()
	a.mu.Unlock()
	a.emit("state", a.State())
}

func (a *App) Cancel() {
	a.mu.Lock()
	if a.cancel != nil {
		a.cancel()
	}
	a.mu.Unlock()
}

// Play: refresh the login, install / update everything, start the game.
func (a *App) Play() {
	a.mu.Lock()
	if a.busy || a.game != nil {
		a.mu.Unlock()
		return
	}
	if a.session == nil {
		a.mu.Unlock()
		a.emit("error", "Сначала войди: введи ник или войди через Microsoft")
		return
	}
	if !a.licenseActiveLocal() {
		a.mu.Unlock()
		a.emit("needKey", true)
		a.emit("error", "сначала активируй ключ (получи его в Telegram-боте @Dettexclient_bot)")
		return
	}
	a.busy = true
	ctx, cancel := context.WithCancel(context.Background())
	a.cancel = cancel
	settings := a.settings
	sess := a.session
	a.mu.Unlock()
	a.emit("state", a.State())

	go func() {
		err := a.play(ctx, settings, sess)
		a.mu.Lock()
		a.busy = false
		a.cancel = nil
		a.mu.Unlock()
		if err != nil {
			if errors.Is(err, context.Canceled) {
				a.emit("toast", "Отменено")
			} else {
				a.emit("error", err.Error())
			}
		}
		a.emit("state", a.State())
	}()
}

func (a *App) play(ctx context.Context, settings Settings, sess *Session) error {
	a.emit("progress", map[string]any{"title": "Проверяю вход", "frac": 0.0})
	fresh, err := sess.Fresh(ctx)
	if err != nil {
		return fmt.Errorf("вход устарел, войди заново (%v)", err)
	}
	if fresh != sess {
		a.mu.Lock()
		a.session = fresh
		a.saveSession()
		a.mu.Unlock()
	}
	inst, err := a.install(ctx, settings)
	if err != nil {
		return err
	}
	// after everything is downloaded: a licence key is required to launch (bot issues keys)
	if !a.licenseOK(ctx, fresh.UUID) {
		a.emit("needKey", true)
		return errors.New("введи ключ, чтобы запустить (получи его в Telegram-боте @dettexclient_bot)")
	}
	a.emit("progress", map[string]any{"title": "Запускаю Dettex...", "frac": 1.0})
	g, err := Launch(inst, fresh.Account(), LaunchOptions{RAMMB: settings.RAMMB, QuickPlay: settings.Server})
	if err != nil {
		return fmt.Errorf("запуск: %w", err)
	}
	a.mu.Lock()
	a.game = g
	a.mu.Unlock()
	started := time.Now()
	select {
	case <-g.Started:
	case <-ctx.Done():
		g.Kill()
	}
	a.emit("game", map[string]any{"state": "running"})
	if a.onGameStarted != nil {
		a.onGameStarted()
	}
	err = <-g.Exited
	a.mu.Lock()
	a.game = nil
	a.mu.Unlock()
	early := time.Since(started) < 40*time.Second
	if a.onGameExited != nil {
		a.onGameExited(err, g.Tail(25), early)
	}
	if err != nil && early {
		return fmt.Errorf("игра закрылась с ошибкой: %v\n%s", err, strings.Join(g.Tail(12), "\n"))
	}
	a.emit("game", map[string]any{"state": "exited"})
	return nil
}

// install downloads/updates the game, Java, mods and the embedded client.
func (a *App) install(ctx context.Context, settings Settings) (*Installed, error) {
	jar := a.findClientJar()
	if jar == "" {
		return nil, errors.New("не найден клиент Dettex: собери лаунчер через pack-launcher.bat (клиент встраивается в exe) или положи DettexClient.jar рядом с DettexLauncher.exe")
	}
	var last time.Time
	return Install(ctx, a.layout, Options{PerfMods: settings.PerfMods, DettexJar: jar}, func(title string, f float64, p *Progress) {
		if time.Since(last) < 80*time.Millisecond && f < 1 {
			return
		}
		last = time.Now()
		ev := map[string]any{"title": title, "frac": f}
		if p != nil {
			ev["doneBytes"], ev["totalBytes"], ev["speed"] = p.DoneBytes, p.TotalBytes, p.Speed
			ev["doneFiles"], ev["totalFiles"] = p.DoneFiles, p.TotalFiles
		}
		a.emit("progress", ev)
	})
}

// Update: refresh the game, Java, mods and the client without launching the game.
// The key check is not needed here: updating files does not start the client.
func (a *App) Update() {
	a.mu.Lock()
	if a.busy || a.game != nil {
		a.mu.Unlock()
		return
	}
	a.busy = true
	ctx, cancel := context.WithCancel(context.Background())
	a.cancel = cancel
	settings := a.settings
	a.mu.Unlock()
	a.emit("state", a.State())

	go func() {
		err := a.update(ctx, settings)
		a.mu.Lock()
		a.busy = false
		a.cancel = nil
		a.mu.Unlock()
		if err != nil {
			if errors.Is(err, context.Canceled) {
				a.emit("toast", "Обновление отменено")
			} else {
				a.emit("error", err.Error())
			}
		} else {
			a.emit("toast", "Клиент обновлён — нажми «Играть»")
		}
		a.emit("state", a.State())
	}()
}

func (a *App) update(ctx context.Context, settings Settings) error {
	a.emit("progress", map[string]any{"title": "Обновляю клиент", "frac": 0.0})
	_, err := a.install(ctx, settings)
	return err
}
