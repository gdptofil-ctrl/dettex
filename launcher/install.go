package main

import (
	"archive/zip"
	"context"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io"
	"net/url"
	"os"
	"path/filepath"
	"runtime"
	"sort"
	"strconv"
	"strings"
	"time"
)

const (
	MCVersion       = "1.21.11"
	versionManifest = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
	javaRuntimes    = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json"
	resourcesBase   = "https://resources.download.minecraft.net/"
	fabricMeta      = "https://meta.fabricmc.net/v2/versions/loader/"
	modrinthAPI     = "https://api.modrinth.com/v2/project/"
	minFabricLoader = "0.18.3" // Rinku needs it
)

// ------------------------------------------------------------------ layout

// Layout: everything lives in %APPDATA%\.dettex
type Layout struct{ Root string }

func (l Layout) Versions() string  { return filepath.Join(l.Root, "versions") }
func (l Layout) Libraries() string { return filepath.Join(l.Root, "libraries") }
func (l Layout) Assets() string    { return filepath.Join(l.Root, "assets") }
func (l Layout) Runtime() string   { return filepath.Join(l.Root, "runtime") }
func (l Layout) Game() string      { return filepath.Join(l.Root, "game") }
func (l Layout) Mods() string      { return filepath.Join(l.Game(), "mods") }
func (l Layout) Natives() string   { return filepath.Join(l.Root, "natives", MCVersion) }
func (l Layout) Cache() string     { return filepath.Join(l.Root, "cache") }

// ------------------------------------------------------------------ Mojang version JSON

type Rule struct {
	Action   string          `json:"action"`
	OS       *RuleOS         `json:"os,omitempty"`
	Features map[string]bool `json:"features,omitempty"`
}

type RuleOS struct {
	Name string `json:"name,omitempty"`
	Arch string `json:"arch,omitempty"`
}

type Artifact struct {
	Path string `json:"path"`
	SHA1 string `json:"sha1"`
	Size int64  `json:"size"`
	URL  string `json:"url"`
}

type Library struct {
	Name      string `json:"name"`
	Downloads *struct {
		Artifact *Artifact `json:"artifact"`
	} `json:"downloads,omitempty"`
	Rules []Rule `json:"rules,omitempty"`
	// Fabric style
	URL  string `json:"url,omitempty"`
	SHA1 string `json:"sha1,omitempty"`
	Size int64  `json:"size,omitempty"`
}

// Arg is a plain string or {rules, value}.
type Arg struct {
	Rules  []Rule
	Values []string
}

func (a *Arg) UnmarshalJSON(b []byte) error {
	var s string
	if json.Unmarshal(b, &s) == nil {
		a.Values = []string{s}
		return nil
	}
	var o struct {
		Rules []Rule          `json:"rules"`
		Value json.RawMessage `json:"value"`
	}
	if err := json.Unmarshal(b, &o); err != nil {
		return err
	}
	a.Rules = o.Rules
	if json.Unmarshal(o.Value, &s) == nil {
		a.Values = []string{s}
		return nil
	}
	return json.Unmarshal(o.Value, &a.Values)
}

type Version struct {
	ID           string `json:"id"`
	InheritsFrom string `json:"inheritsFrom,omitempty"`
	Type         string `json:"type"`
	MainClass    string `json:"mainClass"`
	Arguments    struct {
		Game []Arg `json:"game"`
		JVM  []Arg `json:"jvm"`
	} `json:"arguments"`
	AssetIndex struct {
		ID   string `json:"id"`
		SHA1 string `json:"sha1"`
		Size int64  `json:"size"`
		URL  string `json:"url"`
	} `json:"assetIndex"`
	Downloads struct {
		Client Artifact `json:"client"`
	} `json:"downloads"`
	JavaVersion struct {
		Component    string `json:"component"`
		MajorVersion int    `json:"majorVersion"`
	} `json:"javaVersion"`
	Libraries []Library `json:"libraries"`
	Logging   struct {
		Client struct {
			Argument string `json:"argument"`
			File     struct {
				ID   string `json:"id"`
				SHA1 string `json:"sha1"`
				Size int64  `json:"size"`
				URL  string `json:"url"`
			} `json:"file"`
		} `json:"client"`
	} `json:"logging"`
}

// osName / osArch as Mojang writes them.
var osName, osArch = func() (string, string) {
	n := runtime.GOOS
	switch n {
	case "darwin":
		n = "osx"
	}
	a := "x64"
	switch runtime.GOARCH {
	case "386":
		a = "x86"
	case "arm64":
		a = "arm64"
	}
	return n, a
}()

// allowed applies Mojang rules: the last matching rule wins, no rules = allowed.
func allowed(rules []Rule, features map[string]bool) bool {
	if len(rules) == 0 {
		return true
	}
	ok := false
	for _, r := range rules {
		match := true
		if r.OS != nil {
			if r.OS.Name != "" && r.OS.Name != osName {
				match = false
			}
			if r.OS.Arch != "" && r.OS.Arch != osArch {
				match = false
			}
		}
		for k, v := range r.Features {
			if features[k] != v {
				match = false
			}
		}
		if match {
			ok = r.Action == "allow"
		}
	}
	return ok
}

// mavenPath: "group:artifact:version[:classifier]" -> group/path/artifact/version/artifact-version[-classifier].jar
func mavenPath(name string) string {
	p := strings.Split(name, ":")
	if len(p) < 3 {
		return ""
	}
	ext := "jar"
	last := p[len(p)-1]
	if i := strings.Index(last, "@"); i >= 0 {
		ext = last[i+1:]
		p[len(p)-1] = last[:i]
	}
	file := p[1] + "-" + p[2]
	if len(p) > 3 {
		file += "-" + p[3]
	}
	return strings.ReplaceAll(p[0], ".", "/") + "/" + p[1] + "/" + p[2] + "/" + file + "." + ext
}

// libKey identifies a library regardless of version (group:artifact[:classifier]) to merge Fabric over vanilla.
func libKey(name string) string {
	p := strings.Split(name, ":")
	if len(p) < 3 {
		return name
	}
	k := p[0] + ":" + p[1]
	if len(p) > 3 {
		k += ":" + p[3]
	}
	return k
}

// ------------------------------------------------------------------ install plan

// Installed is everything needed to start the game.
type Installed struct {
	Layout     Layout
	Vanilla    *Version
	Fabric     *Version
	Classpath  []string
	ClientJar  string
	Java       string
	LogConfig  string
	AssetIndex string
}

type Step func(title string, frac float64, p *Progress)

// Mod from Modrinth to keep in the mods folder.
type Mod struct {
	Slug     string
	Title    string
	Optional bool // FPS pack
	// a mod that is not on Modrinth: Maven repository + group:artifact:version (snapshots resolved)
	Maven string
	Coord string
}

var managedMods = []Mod{
	{Slug: "fabric-api", Title: "Fabric API"},
	{Slug: "rinku", Title: "Rinku (браузер)"},
	// the client calls Baritone directly (AutoWarden, AnchorFarming...): it must be installed
	{Slug: "baritone", Title: "Baritone", Maven: "https://maven.meteordev.org/snapshots/", Coord: "meteordevelopment:baritone:" + MCVersion + "-SNAPSHOT"},
	{Slug: "lithium", Title: "Lithium", Optional: true},
	{Slug: "ferrite-core", Title: "FerriteCore", Optional: true},
	{Slug: "entityculling", Title: "Entity Culling", Optional: true},
	{Slug: "immediatelyfast", Title: "ImmediatelyFast", Optional: true},
}

type Options struct {
	PerfMods  bool
	DettexJar string // the client jar to install
	Workers   int
}

// Install brings the whole game up to date and returns what Launch needs.
func Install(ctx context.Context, l Layout, opt Options, step Step) (*Installed, error) {
	if opt.Workers <= 0 {
		opt.Workers = 24
	}
	for _, d := range []string{l.Versions(), l.Libraries(), l.Assets(), l.Runtime(), l.Mods(), l.Natives(), l.Cache()} {
		if err := os.MkdirAll(d, 0o755); err != nil {
			return nil, err
		}
	}
	inst := &Installed{Layout: l}

	// 1. Minecraft version
	step("Проверяю Minecraft "+MCVersion, 0.01, nil)
	vanilla, err := loadVanilla(ctx, l)
	if err != nil {
		return nil, fmt.Errorf("не удалось получить Minecraft %s: %w", MCVersion, err)
	}
	inst.Vanilla = vanilla
	inst.AssetIndex = vanilla.AssetIndex.ID

	// 2. Fabric
	step("Проверяю Fabric", 0.03, nil)
	fabric, err := loadFabric(ctx, l)
	if err != nil {
		return nil, fmt.Errorf("не удалось получить Fabric: %w", err)
	}
	inst.Fabric = fabric

	// 3. libraries + client jar + log config
	var tasks []Task
	cp, libTasks := classpath(l, vanilla, fabric)
	tasks = append(tasks, libTasks...)
	inst.ClientJar = filepath.Join(l.Versions(), MCVersion, MCVersion+".jar")
	c := vanilla.Downloads.Client
	tasks = append(tasks, Task{URL: c.URL, Path: inst.ClientJar, SHA1: c.SHA1, Size: c.Size})
	inst.Classpath = append(cp, inst.ClientJar)
	if lf := vanilla.Logging.Client.File; lf.URL != "" {
		inst.LogConfig = filepath.Join(l.Assets(), "log_configs", lf.ID)
		tasks = append(tasks, Task{URL: lf.URL, Path: inst.LogConfig, SHA1: lf.SHA1, Size: lf.Size})
	}
	step("Скачиваю библиотеки", 0.05, nil)
	if err := downloadAll(ctx, tasks, opt.Workers, false, func(p Progress) { step("Скачиваю библиотеки", 0.05+0.15*frac(p), &p) }); err != nil {
		return nil, err
	}
	if err := extractNatives(l, vanilla); err != nil {
		return nil, fmt.Errorf("нативные библиотеки: %w", err)
	}

	// 4. assets (sounds, textures, languages)
	step("Проверяю ресурсы игры", 0.2, nil)
	assetTasks, err := assetTasks(ctx, l, vanilla)
	if err != nil {
		return nil, fmt.Errorf("ресурсы игры: %w", err)
	}
	if err := downloadAll(ctx, assetTasks, opt.Workers+8, false, func(p Progress) { step("Скачиваю ресурсы игры", 0.2+0.45*frac(p), &p) }); err != nil {
		return nil, err
	}

	// 5. Java
	step("Проверяю Java", 0.65, nil)
	java, err := installJava(ctx, l, vanilla, opt.Workers, func(p Progress) { step("Скачиваю Java 21", 0.65+0.2*frac(p), &p) })
	if err != nil {
		return nil, fmt.Errorf("Java: %w", err)
	}
	inst.Java = java

	// 6. mods
	step("Обновляю моды", 0.86, nil)
	if err := installMods(ctx, l, opt, func(p Progress) { step("Скачиваю моды", 0.86+0.12*frac(p), &p) }); err != nil {
		return nil, err
	}
	step("Готово", 1, nil)
	return inst, nil
}

func frac(p Progress) float64 {
	if p.TotalBytes > 0 {
		return clamp(float64(p.DoneBytes) / float64(p.TotalBytes))
	}
	if p.TotalFiles > 0 {
		return clamp(float64(p.DoneFiles) / float64(p.TotalFiles))
	}
	return 1
}

func clamp(f float64) float64 {
	if f < 0 {
		return 0
	}
	if f > 1 {
		return 1
	}
	return f
}

func loadVanilla(ctx context.Context, l Layout) (*Version, error) {
	path := filepath.Join(l.Versions(), MCVersion, MCVersion+".json")
	var v Version
	// an installed copy is enough: Mojang never changes a released version
	if b, err := os.ReadFile(path); err == nil && json.Unmarshal(b, &v) == nil && v.ID == MCVersion {
		return &v, nil
	}
	var manifest struct {
		Versions []struct {
			ID   string `json:"id"`
			URL  string `json:"url"`
			SHA1 string `json:"sha1"`
		} `json:"versions"`
	}
	if err := getJSON(ctx, versionManifest, filepath.Join(l.Cache(), "version_manifest_v2.json"), &manifest); err != nil {
		return nil, err
	}
	for _, e := range manifest.Versions {
		if e.ID == MCVersion {
			if err := getJSON(ctx, e.URL, path, &v); err != nil {
				return nil, err
			}
			return &v, nil
		}
	}
	return nil, fmt.Errorf("версия %s не найдена у Mojang", MCVersion)
}

func versionAtLeast(v, min string) bool {
	a := strings.Split(strings.SplitN(v, "+", 2)[0], ".")
	b := strings.Split(min, ".")
	for i := 0; i < len(a) || i < len(b); i++ {
		var x, y int
		if i < len(a) {
			x, _ = strconv.Atoi(a[i])
		}
		if i < len(b) {
			y, _ = strconv.Atoi(b[i])
		}
		if x != y {
			return x > y
		}
	}
	return true
}

func loadFabric(ctx context.Context, l Layout) (*Version, error) {
	var loaders []struct {
		Loader struct {
			Version string `json:"version"`
			Stable  bool   `json:"stable"`
		} `json:"loader"`
	}
	cacheList := filepath.Join(l.Cache(), "fabric-loaders-"+MCVersion+".json")
	if err := getJSON(ctx, fabricMeta+MCVersion, cacheList, &loaders); err != nil {
		return nil, err
	}
	ver := ""
	for _, e := range loaders {
		if e.Loader.Stable && versionAtLeast(e.Loader.Version, minFabricLoader) {
			ver = e.Loader.Version
			break
		}
	}
	if ver == "" && len(loaders) > 0 {
		ver = loaders[0].Loader.Version
	}
	if ver == "" {
		return nil, fmt.Errorf("нет Fabric для %s", MCVersion)
	}
	id := "fabric-loader-" + ver + "-" + MCVersion
	var v Version
	path := filepath.Join(l.Versions(), id, id+".json")
	if b, err := os.ReadFile(path); err == nil && json.Unmarshal(b, &v) == nil && v.MainClass != "" {
		return &v, nil
	}
	if err := getJSON(ctx, fabricMeta+MCVersion+"/"+url.PathEscape(ver)+"/profile/json", path, &v); err != nil {
		return nil, err
	}
	return &v, nil
}

// classpath: Fabric libraries first (they win over vanilla ones with the same group:artifact), then vanilla.
func classpath(l Layout, vanilla, fabric *Version) ([]string, []Task) {
	var cp []string
	var tasks []Task
	taken := map[string]bool{}
	add := func(lib Library, fromFabric bool) {
		if !allowed(lib.Rules, nil) {
			return
		}
		key := libKey(lib.Name)
		if taken[key] {
			return
		}
		taken[key] = true
		var t Task
		if !fromFabric && lib.Downloads != nil && lib.Downloads.Artifact != nil {
			a := lib.Downloads.Artifact
			t = Task{URL: a.URL, Path: filepath.Join(l.Libraries(), filepath.FromSlash(a.Path)), SHA1: a.SHA1, Size: a.Size}
		} else {
			p := mavenPath(lib.Name)
			if p == "" {
				return
			}
			base := lib.URL
			if base == "" {
				base = "https://libraries.minecraft.net/"
			}
			if !strings.HasSuffix(base, "/") {
				base += "/"
			}
			t = Task{URL: base + p, Path: filepath.Join(l.Libraries(), filepath.FromSlash(p)), SHA1: lib.SHA1, Size: lib.Size}
		}
		if t.URL == "" {
			return
		}
		tasks = append(tasks, t)
		cp = append(cp, t.Path)
	}
	if fabric != nil {
		for _, lib := range fabric.Libraries {
			add(lib, true)
		}
	}
	for _, lib := range vanilla.Libraries {
		add(lib, false)
	}
	return cp, tasks
}

// extractNatives unpacks the x64 Windows .dll files of LWJGL & co. into the natives folder.
func extractNatives(l Layout, v *Version) error {
	if osName != "windows" {
		return nil
	}
	want := "natives-" + osName
	for _, lib := range v.Libraries {
		if !allowed(lib.Rules, nil) || !strings.HasSuffix(lib.Name, ":"+want) || lib.Downloads == nil || lib.Downloads.Artifact == nil {
			continue
		}
		jar := filepath.Join(l.Libraries(), filepath.FromSlash(lib.Downloads.Artifact.Path))
		if err := unzipDLLs(jar, l.Natives()); err != nil {
			return err
		}
	}
	return nil
}

func unzipDLLs(jar, dir string) error {
	z, err := zip.OpenReader(jar)
	if err != nil {
		return err
	}
	defer z.Close()
	for _, f := range z.File {
		name := strings.ToLower(f.Name)
		if !strings.HasSuffix(name, ".dll") || strings.HasPrefix(name, "meta-inf/") {
			continue
		}
		// LWJGL jars keep arch folders (windows/x64/...); take only ours
		if strings.Contains(name, "/x86/") || strings.Contains(name, "arm64") {
			continue
		}
		dst := filepath.Join(dir, filepath.Base(f.Name))
		if st, err := os.Stat(dst); err == nil && st.Size() == int64(f.UncompressedSize64) {
			continue
		}
		if err := func() error {
			r, err := f.Open()
			if err != nil {
				return err
			}
			defer r.Close()
			w, err := os.Create(dst)
			if err != nil {
				return err
			}
			defer w.Close()
			_, err = io.Copy(w, r)
			return err
		}(); err != nil {
			return err
		}
	}
	return nil
}

func assetTasks(ctx context.Context, l Layout, v *Version) ([]Task, error) {
	idx := v.AssetIndex
	path := filepath.Join(l.Assets(), "indexes", idx.ID+".json")
	var index struct {
		Objects map[string]struct {
			Hash string `json:"hash"`
			Size int64  `json:"size"`
		} `json:"objects"`
	}
	if !fileOK(Task{Path: path, SHA1: idx.SHA1, Size: idx.Size}, true) {
		if err := downloadAll(ctx, []Task{{URL: idx.URL, Path: path, SHA1: idx.SHA1, Size: idx.Size}}, 1, false, nil); err != nil {
			return nil, err
		}
	}
	b, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}
	if err := json.Unmarshal(b, &index); err != nil {
		return nil, err
	}
	tasks := make([]Task, 0, len(index.Objects))
	for _, o := range index.Objects {
		if len(o.Hash) < 2 {
			continue
		}
		rel := o.Hash[:2] + "/" + o.Hash
		tasks = append(tasks, Task{URL: resourcesBase + rel, Path: filepath.Join(l.Assets(), "objects", o.Hash[:2], o.Hash), SHA1: o.Hash, Size: o.Size})
	}
	sort.Slice(tasks, func(i, j int) bool { return tasks[i].Size > tasks[j].Size }) // big ones first: smoother bar
	return tasks, nil
}

// installJava gets Mojang's own Java build for this version (java-runtime-delta = Java 21).
func installJava(ctx context.Context, l Layout, v *Version, workers int, onProgress func(Progress)) (string, error) {
	comp := v.JavaVersion.Component
	if comp == "" {
		comp = "java-runtime-delta"
	}
	dir := filepath.Join(l.Runtime(), comp)
	javaw := filepath.Join(dir, "bin", "javaw.exe")
	if osName != "windows" {
		javaw = filepath.Join(dir, "bin", "java")
	}
	marker := filepath.Join(dir, ".dettex-complete")
	if _, err := os.Stat(marker); err == nil {
		if _, err := os.Stat(javaw); err == nil {
			return javaw, nil
		}
	}
	var all map[string]map[string][]struct {
		Manifest struct {
			SHA1 string `json:"sha1"`
			Size int64  `json:"size"`
			URL  string `json:"url"`
		} `json:"manifest"`
	}
	if err := getJSON(ctx, javaRuntimes, filepath.Join(l.Cache(), "java-runtimes.json"), &all); err != nil {
		return "", err
	}
	platform := "windows-x64"
	switch {
	case osName == "windows" && osArch == "x86":
		platform = "windows-x86"
	case osName == "windows" && osArch == "arm64":
		platform = "windows-arm64"
	case osName == "linux":
		platform = "linux"
	}
	list := all[platform][comp]
	if len(list) == 0 {
		return "", fmt.Errorf("Mojang не даёт %s для %s", comp, platform)
	}
	var man struct {
		Files map[string]struct {
			Type       string `json:"type"`
			Executable bool   `json:"executable"`
			Downloads  struct {
				Raw Artifact `json:"raw"`
			} `json:"downloads"`
		} `json:"files"`
	}
	if err := getJSON(ctx, list[0].Manifest.URL, filepath.Join(l.Cache(), comp+"-"+platform+".json"), &man); err != nil {
		return "", err
	}
	var tasks []Task
	for name, f := range man.Files {
		p := filepath.Join(dir, filepath.FromSlash(name))
		switch f.Type {
		case "directory":
			_ = os.MkdirAll(p, 0o755)
		case "file":
			r := f.Downloads.Raw
			tasks = append(tasks, Task{URL: r.URL, Path: p, SHA1: r.SHA1, Size: r.Size})
		}
	}
	if err := downloadAll(ctx, tasks, workers, false, onProgress); err != nil {
		return "", err
	}
	if osName != "windows" {
		for name, f := range man.Files {
			if f.Executable {
				_ = os.Chmod(filepath.Join(dir, filepath.FromSlash(name)), 0o755)
			}
		}
	}
	_ = os.WriteFile(marker, []byte(list[0].Manifest.SHA1), 0o644)
	return javaw, nil
}

// ------------------------------------------------------------------ mods

type managedState struct {
	Files map[string]string `json:"files"` // slug -> file name we put there
}

func installMods(ctx context.Context, l Layout, opt Options, onProgress func(Progress)) error {
	statePath := filepath.Join(l.Mods(), ".dettex-managed.json")
	var st managedState
	if b, err := os.ReadFile(statePath); err == nil {
		_ = json.Unmarshal(b, &st)
	}
	if st.Files == nil {
		st.Files = map[string]string{}
	}
	// the Dettex client itself
	if opt.DettexJar == "" {
		return fmt.Errorf("в лаунчер не встроен клиент Dettex: собери его через pack-launcher.bat")
	}
	dst := filepath.Join(l.Mods(), "DettexClient.jar")
	if err := copyIfChanged(opt.DettexJar, dst); err != nil {
		return fmt.Errorf("копирование клиента: %w", err)
	}
	nestedRinku := jarHasNested(opt.DettexJar, "rinku")

	var tasks []Task
	want := map[string]string{}       // slug -> file name the manifest points at now
	optional := map[string]bool{}     // failed optional mods are fine, required ones are reported
	var missing []string
	for _, m := range managedMods {
		if m.Optional && !opt.PerfMods {
			continue
		}
		if m.Slug == "rinku" && nestedRinku {
			continue // already inside the client jar
		}
		var f *modFile
		var err error
		if m.Maven != "" {
			f, err = mavenFile(ctx, l, m)
		} else {
			f, err = modrinthFile(ctx, l, m.Slug)
		}
		if err != nil {
			if m.Optional {
				continue // an FPS mod that is not out for this version yet
			}
			return fmt.Errorf("%s: %w", m.Title, err)
		}
		want[m.Slug] = f.Filename
		optional[m.Slug] = m.Optional
		tasks = append(tasks, Task{URL: f.URL, Path: filepath.Join(l.Mods(), f.Filename), SHA512: f.Hashes.SHA512, SHA1: f.Hashes.SHA1, Size: f.Size})
	}
	if err := downloadAllTries(ctx, tasks, 6, false, onProgress, 2); err != nil {
		if ctx.Err() != nil {
			return ctx.Err()
		}
		// A single slow / blocked mod host must not block the game (and the client).
		// Keep what is already on disk, skip the files that failed, and retry them on
		// the next update. Fabric API failures are still visible in the game log.
	}
	// Only remember files that really landed on disk. Previously a failed baritone
	// download was still written to the state file, the old working jar was deleted,
	// and the next launch crashed with NoClassDefFoundError: baritone/.../Goal.
	ready := map[string]bool{}
	for _, t := range tasks {
		if fi, err := os.Stat(t.Path); err == nil && (t.Size <= 0 || fi.Size() == t.Size) {
			ready[t.Path] = true
		}
	}
	final := map[string]string{}
	for slug, file := range want {
		if ready[filepath.Join(l.Mods(), file)] {
			final[slug] = file
			continue
		}
		// keep the previously installed version until the new one downloads
		if old := st.Files[slug]; old != "" && fileExists(filepath.Join(l.Mods(), old)) {
			final[slug] = old
			continue
		}
		if !optional[slug] {
			missing = append(missing, slug)
		}
	}
	// remove old versions of our mods and mods switched off, but never the fallback we just kept
	for slug, file := range st.Files {
		if final[slug] != file {
			_ = os.Remove(filepath.Join(l.Mods(), file))
		}
	}
	if len(missing) > 0 {
		appendModLog(l, fmt.Sprintf("required mods not downloaded: %s", strings.Join(missing, ", ")))
	}
	st.Files = final
	b, _ := json.MarshalIndent(st, "", "  ")
	return os.WriteFile(statePath, b, 0o644)
}

// appendModLog records mod install problems next to the game logs (the launcher window
// is not always open, and a hidden --play run has no console at all).
func appendModLog(l Layout, line string) {
	dir := filepath.Join(l.Game(), "logs")
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return
	}
	f, err := os.OpenFile(filepath.Join(dir, "launcher-mods.log"), os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0o644)
	if err != nil {
		return
	}
	defer f.Close()
	fmt.Fprintf(f, "%s %s\n", time.Now().Format("2006-01-02 15:04:05"), line)
}

type modFile struct {
	URL      string `json:"url"`
	Filename string `json:"filename"`
	Primary  bool   `json:"primary"`
	Size     int64  `json:"size"`
	Hashes   struct {
		SHA512 string `json:"sha512"`
		SHA1   string `json:"sha1"`
	} `json:"hashes"`
}

func modrinthFile(ctx context.Context, l Layout, slug string) (*modFile, error) {
	q := "?loaders=" + url.QueryEscape(`["fabric"]`) + "&game_versions=" + url.QueryEscape(`["`+MCVersion+`"]`)
	var versions []struct {
		Files []modFile `json:"files"`
	}
	if err := getJSON(ctx, modrinthAPI+slug+"/version"+q, filepath.Join(l.Cache(), "modrinth-"+slug+".json"), &versions); err != nil {
		return nil, err
	}
	if len(versions) == 0 || len(versions[0].Files) == 0 {
		return nil, fmt.Errorf("нет версии для %s", MCVersion)
	}
	files := versions[0].Files
	for i := range files {
		if files[i].Primary {
			return &files[i], nil
		}
	}
	return &files[0], nil
}

// getText downloads a small text file (cached for offline starts like getJSON).
func getText(ctx context.Context, u, cache string) (string, error) {
	ctx2, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()
	body, err := func() ([]byte, error) {
		req, err := newRequest(ctx2, "GET", u, nil)
		if err != nil {
			return nil, err
		}
		resp, err := httpClient.Do(req)
		if err != nil {
			return nil, err
		}
		defer resp.Body.Close()
		if resp.StatusCode != 200 {
			return nil, fmt.Errorf("HTTP %d: %s", resp.StatusCode, u)
		}
		return io.ReadAll(io.LimitReader(resp.Body, 4<<20))
	}()
	if err == nil {
		if cache != "" {
			_ = os.MkdirAll(filepath.Dir(cache), 0o755)
			_ = os.WriteFile(cache, body, 0o644)
		}
		return string(body), nil
	}
	if cache != "" {
		if b, rerr := os.ReadFile(cache); rerr == nil {
			return string(b), nil
		}
	}
	return "", err
}

// mavenFile resolves a Maven artifact (also -SNAPSHOT: newest build from maven-metadata.xml).
func mavenFile(ctx context.Context, l Layout, m Mod) (*modFile, error) {
	p := strings.Split(m.Coord, ":")
	if len(p) != 3 {
		return nil, fmt.Errorf("bad maven coordinate %s", m.Coord)
	}
	group, artifact, version := strings.ReplaceAll(p[0], ".", "/"), p[1], p[2]
	base := strings.TrimSuffix(m.Maven, "/") + "/" + group + "/" + artifact + "/" + version + "/"
	value := version
	if strings.HasSuffix(version, "-SNAPSHOT") {
		xmlText, err := getText(ctx, base+"maven-metadata.xml", filepath.Join(l.Cache(), "maven-"+artifact+"-"+version+".xml"))
		if err != nil {
			return nil, err
		}
		var meta struct {
			Versioning struct {
				Snapshot struct {
					Timestamp   string `xml:"timestamp"`
					BuildNumber string `xml:"buildNumber"`
				} `xml:"snapshot"`
				SnapshotVersions []struct {
					Classifier string `xml:"classifier"`
					Extension  string `xml:"extension"`
					Value      string `xml:"value"`
				} `xml:"snapshotVersions>snapshotVersion"`
			} `xml:"versioning"`
		}
		if err := xml.Unmarshal([]byte(xmlText), &meta); err != nil {
			return nil, err
		}
		value = ""
		for _, sv := range meta.Versioning.SnapshotVersions {
			if sv.Extension == "jar" && sv.Classifier == "" {
				value = sv.Value
			}
		}
		if value == "" && meta.Versioning.Snapshot.Timestamp != "" {
			value = strings.TrimSuffix(version, "-SNAPSHOT") + "-" + meta.Versioning.Snapshot.Timestamp + "-" + meta.Versioning.Snapshot.BuildNumber
		}
		if value == "" {
			return nil, fmt.Errorf("no build of %s", m.Coord)
		}
	}
	name := artifact + "-" + value + ".jar"
	f := &modFile{URL: base + name, Filename: name}
	if sum, err := getText(ctx, base+name+".sha1", filepath.Join(l.Cache(), name+".sha1")); err == nil {
		if fields := strings.Fields(sum); len(fields) > 0 && len(fields[0]) == 40 {
			f.Hashes.SHA1 = fields[0]
		}
	}
	return f, nil
}

func copyIfChanged(src, dst string) error {
	a, err := os.Stat(src)
	if err != nil {
		return err
	}
	if b, err := os.Stat(dst); err == nil && b.Size() == a.Size() && !b.ModTime().Before(a.ModTime()) {
		return nil
	}
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer in.Close()
	tmp := dst + ".part"
	out, err := os.Create(tmp)
	if err != nil {
		return err
	}
	if _, err := io.Copy(out, in); err != nil {
		out.Close()
		return err
	}
	if err := out.Close(); err != nil {
		return err
	}
	_ = os.Remove(dst)
	if err := os.Rename(tmp, dst); err != nil {
		return err
	}
	return os.Chtimes(dst, a.ModTime(), a.ModTime())
}

func jarHasNested(jar, prefix string) bool {
	z, err := zip.OpenReader(jar)
	if err != nil {
		return false
	}
	defer z.Close()
	for _, f := range z.File {
		n := strings.ToLower(f.Name)
		if strings.HasPrefix(n, "meta-inf/jars/"+prefix) {
			return true
		}
	}
	return false
}
