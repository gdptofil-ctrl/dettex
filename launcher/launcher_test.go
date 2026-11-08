package main

import (
	"archive/zip"
	"bytes"
	"context"
	"crypto/sha1"
	"crypto/sha512"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"path/filepath"
	"runtime"
	"strings"
	"sync/atomic"
	"testing"
	"time"
)

func sha1hex(b []byte) string   { h := sha1.Sum(b); return hex.EncodeToString(h[:]) }
func sha512hex(b []byte) string { h := sha512.Sum512(b); return hex.EncodeToString(h[:]) }

// TestMain doubles as the fake game on Windows: installJava expects bin/javaw.exe there, and a
// shell script cannot be executed. The test copies its own binary to javaw.exe and starts it with
// DETTEX_FAKE_GAME=1; before any flag parsing this branch records the Java arguments and exits.
func TestMain(m *testing.M) {
	if os.Getenv("DETTEX_FAKE_GAME") == "1" {
		_ = os.WriteFile(filepath.Join(".", "args.txt"), []byte("args: "+strings.Join(os.Args[1:], " ")+"\n"), 0o644)
		fmt.Println("[Render thread/INFO]: Backend library: LWJGL version 3.3.3")
		time.Sleep(time.Second)
		os.Exit(0)
	}
	os.Exit(m.Run())
}

// realVersion loads the real 1.21.11 version JSON (from the Loom cache) when it is around.
func realVersion(t *testing.T) *Version {
	p := os.Getenv("MC_VERSION_JSON")
	if p == "" {
		t.Skip("MC_VERSION_JSON not set")
	}
	b, err := os.ReadFile(p)
	if err != nil {
		t.Skip(err)
	}
	var v Version
	if err := json.Unmarshal(b, &v); err != nil {
		t.Fatal(err)
	}
	return &v
}

func TestRealVersionWindowsCommand(t *testing.T) {
	v := realVersion(t)
	oldN, oldA := osName, osArch
	osName, osArch = "windows", "x64"
	defer func() { osName, osArch = oldN, oldA }()
	fabric := &Version{ID: "fabric-loader-0.18.4-1.21.11", MainClass: "net.fabricmc.loader.impl.launch.knot.KnotClient",
		Libraries: []Library{{Name: "net.fabricmc:fabric-loader:0.18.4", URL: "https://maven.fabricmc.net/"}, {Name: "org.ow2.asm:asm:9.9", URL: "https://maven.fabricmc.net/"}}}
	fabric.Arguments.JVM = []Arg{{Values: []string{"-DFabricMcEmu= net.minecraft.client.main.Main "}}}
	l := Layout{Root: `C:\Users\x\AppData\Roaming\.dettex`}
	cp, tasks := classpath(l, v, fabric)
	joined := strings.Join(cp, "\n")
	for _, bad := range []string{"natives-linux", "natives-macos", "java-objc-bridge", "native-epoll", "native-kqueue"} {
		if strings.Contains(joined, bad) {
			t.Errorf("classpath has %s", bad)
		}
	}
	if !strings.Contains(joined, "lwjgl-3.3.3-natives-windows.jar") {
		t.Error("windows natives missing")
	}
	if !strings.Contains(cp[0], "fabric-loader") {
		t.Error("fabric loader must come first")
	}
	in := &Installed{Layout: l, Vanilla: v, Fabric: fabric, Classpath: append(cp, "client.jar"), Java: "javaw.exe", LogConfig: "log.xml", AssetIndex: v.AssetIndex.ID}
	java, args := BuildCommand(in, Account{Name: "Dizzel", UUID: "069a79f444e94726a5befca90e38aaf5", AccessToken: "TOKEN"}, LaunchOptions{RAMMB: 4096})
	s := strings.Join(args, " ")
	t.Logf("%d libraries, %d args, %d downloads", len(cp), len(args), len(tasks))
	mustHave := []string{"-Xmx4096M", "-XX:+UseG1GC", "-Dlog4j.configurationFile=log.xml", "-Djava.library.path=" + l.Natives(),
		"-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump", "-DFabricMcEmu= net.minecraft.client.main.Main ",
		"net.fabricmc.loader.impl.launch.knot.KnotClient", "--username Dizzel", "--uuid 069a79f4-44e9-4726-a5be-fca90e38aaf5",
		"--accessToken TOKEN", "--version fabric-loader-0.18.4-1.21.11", "--assetIndex " + v.AssetIndex.ID, "--gameDir " + l.Game()}
	for _, m := range mustHave {
		if !strings.Contains(s, m) {
			t.Errorf("missing %q", m)
		}
	}
	for _, bad := range []string{"--demo", "-XstartOnFirstThread", "-Xss1M", "${", "--quickPlay", "--width"} {
		if strings.Contains(s, bad) {
			t.Errorf("must not contain %q", bad)
		}
	}
	// main class must come after all JVM args and before game args
	mi := indexOf(args, fabric.MainClass)
	if mi < 0 || indexOf(args, "-cp") > mi || indexOf(args, "--username") < mi {
		t.Error("argument order is wrong")
	}
	if java != "javaw.exe" {
		t.Error("java path")
	}
	_, args = BuildCommand(in, Account{Name: "a", UUID: "b"}, LaunchOptions{RAMMB: 4096, QuickPlay: "mc.example.net"})
	if !strings.Contains(strings.Join(args, " "), "--quickPlayMultiplayer mc.example.net") {
		t.Error("quick play")
	}
}

func indexOf(a []string, s string) int {
	for i, x := range a {
		if x == s {
			return i
		}
	}
	return -1
}

func TestMavenPath(t *testing.T) {
	cases := map[string]string{
		"net.fabricmc:fabric-loader:0.18.4":     "net/fabricmc/fabric-loader/0.18.4/fabric-loader-0.18.4.jar",
		"org.lwjgl:lwjgl:3.3.3:natives-windows": "org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-windows.jar",
		"net.fabricmc:intermediary:1.21.11":     "net/fabricmc/intermediary/1.21.11/intermediary-1.21.11.jar",
		"de.oceanlabs.mcp:mcp_config:1.0@zip":   "de/oceanlabs/mcp/mcp_config/1.0/mcp_config-1.0.zip",
	}
	for in, want := range cases {
		if got := mavenPath(in); got != want {
			t.Errorf("%s -> %s, want %s", in, got, want)
		}
	}
	if !versionAtLeast("0.18.4", "0.18.3") || versionAtLeast("0.18.1", "0.18.3") || !versionAtLeast("0.19.0", "0.18.3") {
		t.Error("versionAtLeast")
	}
}

// ------------------------------------------------------------------ full install + launch against fake servers

type fakeNet struct {
	srv   *httptest.Server
	files map[string][]byte // path -> body
	hits  atomic.Int32
	fail  map[string]int // path -> times to fail first
}

func (f *fakeNet) url(p string) string { return "https://fake.test" + p }

func newFakeNet(t *testing.T) *fakeNet {
	f := &fakeNet{files: map[string][]byte{}, fail: map[string]int{}}
	f.srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		f.hits.Add(1)
		key := r.URL.Path
		if r.URL.RawQuery != "" {
			key += "?" + r.URL.RawQuery
		}
		if n := f.fail[key]; n > 0 {
			f.fail[key] = n - 1
			http.Error(w, "flaky", 503)
			return
		}
		b, ok := f.files[key]
		if !ok {
			http.Error(w, "nope "+key, 404)
			return
		}
		_, _ = w.Write(b)
	}))
	// every https URL goes to the fake server, keeping path (and query)
	rewriteURL = func(u string) string {
		p, err := url.Parse(u)
		if err != nil {
			return u
		}
		host := strings.ReplaceAll(p.Host, ".", "_")
		return f.srv.URL + "/" + host + p.Path + func() string {
			if p.RawQuery != "" {
				return "?" + p.RawQuery
			}
			return ""
		}()
	}
	t.Cleanup(func() { f.srv.Close(); rewriteURL = func(u string) string { return u } })
	return f
}

// put registers a body for the URL u (as the client will ask for it).
func (f *fakeNet) put(u string, body []byte) {
	p, _ := url.Parse(u)
	key := "/" + strings.ReplaceAll(p.Host, ".", "_") + p.Path
	if p.RawQuery != "" {
		key += "?" + p.RawQuery
	}
	f.files[key] = body
}

func jarWith(files map[string]string) []byte {
	var buf bytes.Buffer
	z := zip.NewWriter(&buf)
	for n, c := range files {
		w, _ := z.Create(n)
		_, _ = io.WriteString(w, c)
	}
	_ = z.Close()
	return buf.Bytes()
}

func TestInstallAndLaunch(t *testing.T) {
	fn := newFakeNet(t)
	root := t.TempDir()
	l := Layout{Root: root}

	// --- Mojang: manifest, version, client, libraries, assets
	lib1 := []byte("lib-one")
	libNat := jarWith(map[string]string{"linux/x64/org/lwjgl/liblwjgl.so": "x"})
	client := []byte("client-jar")
	logx := []byte("<Configuration/>")
	assetA, assetB := []byte("sound-a"), []byte("texture-b")
	idx := map[string]any{"objects": map[string]any{
		"minecraft/sounds/a.ogg":   map[string]any{"hash": sha1hex(assetA), "size": len(assetA)},
		"minecraft/textures/b.png": map[string]any{"hash": sha1hex(assetB), "size": len(assetB)},
	}}
	idxB, _ := json.Marshal(idx)
	ver := map[string]any{
		"id": MCVersion, "type": "release", "mainClass": "net.minecraft.client.main.Main",
		"assetIndex":  map[string]any{"id": "29", "sha1": sha1hex(idxB), "size": len(idxB), "url": "https://piston-meta.mojang.com/v1/packages/x/29.json"},
		"downloads":   map[string]any{"client": map[string]any{"sha1": sha1hex(client), "size": len(client), "url": "https://piston-data.mojang.com/client.jar"}},
		"javaVersion": map[string]any{"component": "java-runtime-delta", "majorVersion": 21},
		"logging":     map[string]any{"client": map[string]any{"argument": "-Dlog4j.configurationFile=${path}", "file": map[string]any{"id": "client-1.21.2.xml", "sha1": sha1hex(logx), "size": len(logx), "url": "https://piston-data.mojang.com/log.xml"}}},
		"libraries": []any{
			map[string]any{"name": "a:one:1", "downloads": map[string]any{"artifact": map[string]any{"path": "a/one/1/one-1.jar", "sha1": sha1hex(lib1), "size": len(lib1), "url": "https://libraries.minecraft.net/a/one/1/one-1.jar"}}},
			map[string]any{"name": "org.lwjgl:lwjgl:3.3.3:natives-linux", "rules": []any{map[string]any{"action": "allow", "os": map[string]any{"name": "linux"}}},
				"downloads": map[string]any{"artifact": map[string]any{"path": "org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar", "sha1": sha1hex(libNat), "size": len(libNat), "url": "https://libraries.minecraft.net/nat.jar"}}},
			map[string]any{"name": "ca.weblite:java-objc-bridge:1.1", "rules": []any{map[string]any{"action": "allow", "os": map[string]any{"name": "osx"}}},
				"downloads": map[string]any{"artifact": map[string]any{"path": "x/objc.jar", "sha1": "00", "size": 1, "url": "https://libraries.minecraft.net/objc.jar"}}},
		},
	}
	ver["arguments"] = map[string]any{
		"game": []any{"--username", "${auth_player_name}", "--version", "${version_name}", "--gameDir", "${game_directory}", "--accessToken", "${auth_access_token}",
			map[string]any{"rules": []any{map[string]any{"action": "allow", "features": map[string]any{"is_demo_user": true}}}, "value": "--demo"}},
		"jvm": []any{"-Djava.library.path=${natives_directory}", "-cp", "${classpath}"},
	}
	verB, _ := json.Marshal(ver)
	manifest, _ := json.Marshal(map[string]any{"versions": []any{map[string]any{"id": MCVersion, "url": "https://piston-meta.mojang.com/v1/packages/v/1.21.11.json"}}})
	fn.put(versionManifest, manifest)
	fn.put("https://piston-meta.mojang.com/v1/packages/v/1.21.11.json", verB)
	fn.put("https://piston-data.mojang.com/client.jar", client)
	fn.put("https://piston-data.mojang.com/log.xml", logx)
	fn.put("https://libraries.minecraft.net/a/one/1/one-1.jar", lib1)
	fn.put("https://libraries.minecraft.net/nat.jar", libNat)
	fn.put("https://piston-meta.mojang.com/v1/packages/x/29.json", idxB)
	fn.put(resourcesBase+sha1hex(assetA)[:2]+"/"+sha1hex(assetA), assetA)
	fn.put(resourcesBase+sha1hex(assetB)[:2]+"/"+sha1hex(assetB), assetB)
	fn.fail["/resources_download_minecraft_net/"+sha1hex(assetA)[:2]+"/"+sha1hex(assetA)] = 2 // flaky CDN: must retry

	// --- Java runtime: "java" is a script that acts like the game
	javaScript := []byte("#!/bin/sh\necho \"args: $*\" > \"$PWD/args.txt\"\necho '[Render thread/INFO]: Backend library: LWJGL version 3.3.3'\nsleep 1\nexit 0\n")
	rtFiles := map[string]any{
		"bin":      map[string]any{"type": "directory"},
		"bin/java": map[string]any{"type": "file", "executable": true, "downloads": map[string]any{"raw": map[string]any{"sha1": sha1hex(javaScript), "size": len(javaScript), "url": "https://piston-data.mojang.com/java"}}},
	}
	// on Windows the launcher looks for bin/javaw.exe, so the fake game is a copy of this
	// test binary that switches to game mode through DETTEX_FAKE_GAME (see TestMain)
	runtimes := map[string]any{}
	if runtime.GOOS == "windows" {
		self, err := os.Executable()
		if err != nil {
			t.Fatal(err)
		}
		fakeExe, err := os.ReadFile(self)
		if err != nil {
			t.Fatal(err)
		}
		fn.put("https://piston-data.mojang.com/javaw.exe", fakeExe)
		rtFiles["bin/javaw.exe"] = map[string]any{"type": "file", "executable": true, "downloads": map[string]any{"raw": map[string]any{"sha1": sha1hex(fakeExe), "size": len(fakeExe), "url": "https://piston-data.mojang.com/javaw.exe"}}}
	}
	rtMan, _ := json.Marshal(map[string]any{"files": rtFiles})
	manifestRef := func() []any {
		return []any{map[string]any{"manifest": map[string]any{"sha1": sha1hex(rtMan), "size": len(rtMan), "url": "https://piston-meta.mojang.com/rt.json"}}}
	}
	runtimes["linux"] = map[string]any{"java-runtime-delta": manifestRef()}
	if runtime.GOOS == "windows" {
		runtimes["windows-x64"] = map[string]any{"java-runtime-delta": manifestRef()}
	}
	all, _ := json.Marshal(runtimes)
	fn.put(javaRuntimes, all)
	fn.put("https://piston-meta.mojang.com/rt.json", rtMan)
	fn.put("https://piston-data.mojang.com/java", javaScript)

	// --- Fabric
	floader := []byte("fabric-loader")
	loaders, _ := json.Marshal([]any{
		map[string]any{"loader": map[string]any{"version": "0.19.0-beta", "stable": false}},
		map[string]any{"loader": map[string]any{"version": "0.18.4", "stable": true}},
		map[string]any{"loader": map[string]any{"version": "0.18.1", "stable": true}},
	})
	prof, _ := json.Marshal(map[string]any{"id": "fabric-loader-0.18.4-1.21.11", "inheritsFrom": MCVersion, "mainClass": "net.fabricmc.loader.impl.launch.knot.KnotClient",
		"arguments": map[string]any{"game": []any{}, "jvm": []any{"-DFabricMcEmu= net.minecraft.client.main.Main "}},
		"libraries": []any{map[string]any{"name": "net.fabricmc:fabric-loader:0.18.4", "url": "https://maven.fabricmc.net/", "sha1": sha1hex(floader), "size": len(floader)}}})
	fn.put(fabricMeta+MCVersion, loaders)
	fn.put(fabricMeta+MCVersion+"/0.18.4/profile/json", prof)
	fn.put("https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.18.4/fabric-loader-0.18.4.jar", floader)

	// --- Modrinth
	q := "?loaders=" + url.QueryEscape(`["fabric"]`) + "&game_versions=" + url.QueryEscape(`["`+MCVersion+`"]`)
	for _, m := range managedMods {
		if m.Maven != "" {
			continue
		}
		body := []byte("mod-" + m.Slug)
		list, _ := json.Marshal([]any{map[string]any{"files": []any{map[string]any{"url": "https://cdn.modrinth.com/" + m.Slug + ".jar", "filename": m.Slug + "-1.jar", "primary": true, "size": len(body), "hashes": map[string]any{"sha512": sha512hex(body)}}}}})
		fn.put(modrinthAPI+m.Slug+"/version"+q, list)
		fn.put("https://cdn.modrinth.com/"+m.Slug+".jar", body)
	}

	// --- Baritone from the Meteor maven (snapshot)
	bar := []byte("baritone-jar")
	mb := "https://maven.meteordev.org/snapshots/meteordevelopment/baritone/" + MCVersion + "-SNAPSHOT/"
	fn.put(mb+"maven-metadata.xml", []byte(`<metadata><versioning><snapshot><timestamp>20251201.101010</timestamp><buildNumber>7</buildNumber></snapshot><snapshotVersions><snapshotVersion><classifier>sources</classifier><extension>jar</extension><value>1.21.11-20251201.101010-7</value></snapshotVersion><snapshotVersion><extension>jar</extension><value>1.21.11-20251201.101010-7</value></snapshotVersion></snapshotVersions></versioning></metadata>`))
	fn.put(mb+"baritone-1.21.11-20251201.101010-7.jar", bar)
	fn.put(mb+"baritone-1.21.11-20251201.101010-7.jar.sha1", []byte(sha1hex(bar)+"  baritone.jar"))

	// --- the Dettex jar (with Rinku nested inside, like the Gradle build makes it)
	dettex := filepath.Join(t.TempDir(), "RecoinDLC-1.1.jar")
	_ = os.WriteFile(dettex, jarWith(map[string]string{"fabric.mod.json": "{}", "META-INF/jars/rinku-3.0.5.jar": "x"}), 0o644)

	var steps []string
	inst, err := Install(context.Background(), l, Options{PerfMods: true, DettexJar: dettex}, func(title string, f float64, p *Progress) {
		if len(steps) == 0 || steps[len(steps)-1] != title {
			steps = append(steps, title)
		}
	})
	if err != nil {
		t.Fatalf("install: %v", err)
	}
	t.Logf("steps: %s", strings.Join(steps, " | "))
	for _, p := range []string{
		filepath.Join(l.Versions(), MCVersion, MCVersion+".jar"),
		filepath.Join(l.Libraries(), "a/one/1/one-1.jar"),
		filepath.Join(l.Libraries(), "net/fabricmc/fabric-loader/0.18.4/fabric-loader-0.18.4.jar"),
		filepath.Join(l.Assets(), "objects", sha1hex(assetA)[:2], sha1hex(assetA)),
		filepath.Join(l.Assets(), "log_configs", "client-1.21.2.xml"),
		filepath.Join(l.Mods(), "DettexClient.jar"),
		filepath.Join(l.Mods(), "fabric-api-1.jar"),
		filepath.Join(l.Mods(), "lithium-1.jar"),
		filepath.Join(l.Mods(), "baritone-1.21.11-20251201.101010-7.jar"),
	} {
		if !fileExists(p) {
			t.Errorf("missing %s", p)
		}
	}
	if fileExists(filepath.Join(l.Mods(), "rinku-1.jar")) {
		t.Error("rinku is nested in the client jar, must not be downloaded again")
	}
	if fileExists(filepath.Join(l.Libraries(), "x/objc.jar")) {
		t.Error("osx library downloaded")
	}

	// second run: nothing to download, fast
	hits := fn.hits.Load()
	start := time.Now()
	if _, err := Install(context.Background(), l, Options{PerfMods: false, DettexJar: dettex}, func(string, float64, *Progress) {}); err != nil {
		t.Fatalf("second install: %v", err)
	}
	t.Logf("second run: %d requests, %v", fn.hits.Load()-hits, time.Since(start))
	if fileExists(filepath.Join(l.Mods(), "lithium-1.jar")) {
		t.Error("FPS pack switched off: lithium must be removed")
	}
	if !fileExists(filepath.Join(l.Mods(), "fabric-api-1.jar")) {
		t.Error("fabric api removed")
	}

	// corrupted library is fixed on the next run? (size differs -> re-download)
	_ = os.WriteFile(filepath.Join(l.Libraries(), "a/one/1/one-1.jar"), []byte("broken!!"), 0o644)
	if _, err := Install(context.Background(), l, Options{DettexJar: dettex}, func(string, float64, *Progress) {}); err != nil {
		t.Fatal(err)
	}
	if b, _ := os.ReadFile(filepath.Join(l.Libraries(), "a/one/1/one-1.jar")); string(b) != "lib-one" {
		t.Error("broken library not repaired")
	}

	// offline: the cached metadata is enough
	fn.srv.Close()
	inst2, err := Install(context.Background(), l, Options{DettexJar: dettex}, func(string, float64, *Progress) {})
	if err != nil {
		t.Fatalf("offline install should work from cache: %v", err)
	}

	// launch
	t.Setenv("DETTEX_FAKE_GAME", "1")
	g, err := Launch(inst2, Account{Name: "Dizzel", UUID: "069a79f444e94726a5befca90e38aaf5", AccessToken: "TOK"}, LaunchOptions{RAMMB: 3072})
	if err != nil {
		t.Fatal(err)
	}
	select {
	case <-g.Started:
	case <-time.After(10 * time.Second):
		t.Fatal("game did not report start")
	}
	if err := <-g.Exited; err != nil {
		t.Fatalf("game exit: %v", err)
	}
	args, _ := os.ReadFile(filepath.Join(l.Game(), "args.txt"))
	a := string(args)
	for _, m := range []string{"-Xmx3072M", "net.fabricmc.loader.impl.launch.knot.KnotClient", "--username Dizzel", "--accessToken TOK", "fabric-loader-0.18.4.jar", "1.21.11.jar"} {
		if !strings.Contains(a, m) {
			t.Errorf("launch args miss %q", m)
		}
	}
	if strings.Contains(a, "--demo") {
		t.Error("demo flag")
	}
	_ = inst
}

// ------------------------------------------------------------------ Microsoft sign-in against fakes

func TestSignIn(t *testing.T) {
	var xblTries atomic.Int32
	mux := http.NewServeMux()
	mux.HandleFunc("/login_live_com/oauth20_token.srf", func(w http.ResponseWriter, r *http.Request) {
		_ = r.ParseForm()
		if r.Form.Get("client_id") != msClientID || (r.Form.Get("code") != "CODE" && r.Form.Get("refresh_token") != "R1") {
			http.Error(w, `{"error":"invalid_grant"}`, 400)
			return
		}
		fmt.Fprint(w, `{"access_token":"MSA","refresh_token":"R2"}`)
	})
	mux.HandleFunc("/user_auth_xboxlive_com/user/authenticate", func(w http.ResponseWriter, r *http.Request) {
		xblTries.Add(1)
		var b map[string]map[string]any
		_ = json.NewDecoder(r.Body).Decode(&b)
		if b["Properties"]["RpsTicket"] != "t=MSA" { // only one prefix works on this fake: the client must try them
			http.Error(w, "", 400)
			return
		}
		fmt.Fprint(w, `{"Token":"XBL","DisplayClaims":{"xui":[{"uhs":"UHS"}]}}`)
	})
	xstsMode := "ok"
	mux.HandleFunc("/xsts_auth_xboxlive_com/xsts/authorize", func(w http.ResponseWriter, r *http.Request) {
		if xstsMode == "child" {
			w.WriteHeader(401)
			fmt.Fprint(w, `{"XErr":2148916238}`)
			return
		}
		fmt.Fprint(w, `{"Token":"XSTS","DisplayClaims":{"xui":[{"xid":"123"}]}}`)
	})
	mux.HandleFunc("/api_minecraftservices_com/authentication/login_with_xbox", func(w http.ResponseWriter, r *http.Request) {
		var b map[string]string
		_ = json.NewDecoder(r.Body).Decode(&b)
		if b["identityToken"] != "XBL3.0 x=UHS;XSTS" {
			http.Error(w, "bad", 400)
			return
		}
		fmt.Fprint(w, `{"access_token":"MC","expires_in":86400}`)
	})
	owns := true
	mux.HandleFunc("/api_minecraftservices_com/minecraft/profile", func(w http.ResponseWriter, r *http.Request) {
		if r.Header.Get("Authorization") != "Bearer MC" || !owns {
			http.Error(w, `{"error":"NOT_FOUND"}`, 404)
			return
		}
		fmt.Fprint(w, `{"id":"069a79f444e94726a5befca90e38aaf5","name":"Dizzel"}`)
	})
	srv := httptest.NewServer(mux)
	defer srv.Close()
	rewriteURL = func(u string) string {
		p, _ := url.Parse(u)
		return srv.URL + "/" + strings.ReplaceAll(p.Host, ".", "_") + p.Path
	}
	defer func() { rewriteURL = func(u string) string { return u } }()

	code, err := CodeFromRedirect("https://login.live.com/oauth20_desktop.srf?code=CODE&lc=1033")
	if err != nil || code != "CODE" {
		t.Fatal("code", code, err)
	}
	if _, err := CodeFromRedirect("https://login.live.com/oauth20_desktop.srf?error=access_denied"); err == nil || !strings.Contains(err.Error(), "отменён") {
		t.Error("cancel not detected", err)
	}
	s, err := SignIn(context.Background(), code, "")
	if err != nil {
		t.Fatal(err)
	}
	if s.Name != "Dizzel" || s.MCToken != "MC" || s.RefreshToken != "R2" || s.XUID != "123" {
		t.Errorf("session %+v", s)
	}
	t.Logf("xbl tries: %d (prefix fallback)", xblTries.Load())
	// refresh path
	s.MCExpires = time.Now()
	s.RefreshToken = "R1"
	s2, err := s.Fresh(context.Background())
	if err != nil || s2.MCToken != "MC" {
		t.Error("refresh", err)
	}
	xstsMode = "child"
	if _, err := SignIn(context.Background(), "CODE", ""); err == nil || !strings.Contains(err.Error(), "детский") {
		t.Error("child account message", err)
	}
	xstsMode = "ok"
	owns = false
	if _, err := SignIn(context.Background(), "CODE", ""); err == nil || !strings.Contains(err.Error(), "нет Minecraft") {
		t.Error("no licence message", err)
	}
	if !strings.Contains(LoginURL(), "client_id=00000000402b5328") || !strings.Contains(LoginURL(), "redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf") {
		t.Error("login url", LoginURL())
	}
}

func TestFindClientJar(t *testing.T) {
	dir := t.TempDir()
	exe := filepath.Join(dir, "launcher")
	libs := filepath.Join(dir, "build", "libs")
	_ = os.MkdirAll(libs, 0o755)
	_ = os.MkdirAll(exe, 0o755)
	_ = os.WriteFile(filepath.Join(libs, "RecoinDLC-1.1-sources.jar"), []byte("s"), 0o644)
	_ = os.WriteFile(filepath.Join(libs, "RecoinDLC-1.1.jar"), []byte(jarWith(map[string]string{"fabric.mod.json": `{"id":"insidethesystem","name":"DettexClient"}`})), 0o644)
	// a random mod jar next to the launcher must never be taken for the client
	_ = os.WriteFile(filepath.Join(exe, "baritone-test.jar"), []byte(jarWith(map[string]string{"fabric.mod.json": `{"id":"baritone"}`})), 0o644)
	a := &App{exeDir: exe, layout: Layout{Root: t.TempDir()}}
	if got := a.findClientJar(); filepath.Base(got) != "RecoinDLC-1.1.jar" {
		t.Errorf("project build jar not found: %q", got)
	}
	_ = os.WriteFile(filepath.Join(exe, "DettexClient.jar"), []byte(jarWith(map[string]string{"fabric.mod.json": `{"id":"insidethesystem","name":"DettexClient"}`})), 0o644)
	if got := a.findClientJar(); filepath.Base(got) != "DettexClient.jar" {
		t.Errorf("DettexClient.jar next to the launcher must win: %q", got)
	}
}

func TestEmbeddedClientPayload(t *testing.T) {
	dir := t.TempDir()
	base := filepath.Join(dir, "base.exe")
	jar := filepath.Join(dir, "client.jar")
	out := filepath.Join(dir, "DettexLauncher.exe")
	_ = os.WriteFile(base, []byte("MZ fake exe body"), 0o644)
	_ = os.WriteFile(jar, append([]byte("PK\x03\x04"), make([]byte, 5000)...), 0o644)
	if _, _, err := embeddedPayload(base); err == nil {
		t.Fatal("plain exe must have no payload")
	}
	if err := appendPayload(base, jar, out); err != nil {
		t.Fatal(err)
	}
	// packing an already packed exe replaces the client instead of stacking a second one
	if err := appendPayload(out, jar, out); err != nil {
		t.Fatal(err)
	}
	st, _ := os.Stat(out)
	if st.Size() != int64(len("MZ fake exe body"))+5004+16 {
		t.Fatalf("size %d", st.Size())
	}
	cache := filepath.Join(dir, "cache")
	got, err := extractEmbeddedClient(out, cache)
	if err != nil {
		t.Fatal(err)
	}
	a, _ := os.ReadFile(jar)
	b, _ := os.ReadFile(got)
	if string(a) != string(b) {
		t.Fatal("extracted jar differs")
	}
	again, _ := extractEmbeddedClient(out, cache)
	if again != got {
		t.Fatal("second extract should reuse the file")
	}
}

func TestNormalizeServer(t *testing.T) {
	cases := map[string]string{
		"dettex.workers.dev/": "https://dettex.workers.dev",
		"  https://a.b/  ":    "https://a.b",
		"http://1.2.3.4:8787": "http://1.2.3.4:8787",
		"":                    "",
	}
	for in, want := range cases {
		if got := normalizeServer(in); got != want {
			t.Errorf("normalizeServer(%q)=%q want %q", in, got, want)
		}
	}
}

func TestServerURLFromFile(t *testing.T) {
	dir := t.TempDir()
	a := &App{layout: Layout{Root: dir}, exeDir: dir}
	if a.serverURL() != "" {
		t.Fatal("no file -> empty")
	}
	_ = os.WriteFile(filepath.Join(dir, "server.txt"), []byte("my.server.dev\n"), 0o644)
	if got := a.serverURL(); got != "https://my.server.dev" {
		t.Fatalf("serverURL from file = %q", got)
	}
}

func TestRedeemAndStatus(t *testing.T) {
	var gotRedeem map[string]any
	mux := http.NewServeMux()
	mux.HandleFunc("/v1/license/status", func(w http.ResponseWriter, r *http.Request) {
		var b map[string]any
		_ = json.NewDecoder(r.Body).Decode(&b)
		licensed := b["uuid"] == "licensed-uuid"
		_ = json.NewEncoder(w).Encode(map[string]any{"licensed": licensed})
	})
	mux.HandleFunc("/v1/key/redeem", func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewDecoder(r.Body).Decode(&gotRedeem)
		if gotRedeem["key"] == "DTX-GOOD-GOOD-GOOD" {
			_ = json.NewEncoder(w).Encode(map[string]any{"ok": true, "role": "Premium"})
		} else {
			_ = json.NewEncoder(w).Encode(map[string]any{"ok": false, "reason": "ключ не найден"})
		}
	})
	srv := httptest.NewServer(mux)
	defer srv.Close()
	rewriteURL = func(u string) string {
		// redirect our server calls to the test server
		for _, p := range []string{"/v1/license/status", "/v1/key/redeem"} {
			if strings.HasSuffix(u, p) {
				return srv.URL + p
			}
		}
		return u
	}
	defer func() { rewriteURL = func(u string) string { return u } }()

	dir := t.TempDir()
	_ = os.WriteFile(filepath.Join(dir, "server.txt"), []byte(srv.URL), 0o644)
	a := &App{layout: Layout{Root: dir}, exeDir: dir}

	ok, err := a.licenseActive(context.Background(), "licensed-uuid")
	if err != nil || !ok {
		t.Fatalf("licensed-uuid should be active: ok=%v err=%v", ok, err)
	}
	ok, err = a.licenseActive(context.Background(), "other-uuid")
	if err != nil || ok {
		t.Fatalf("other-uuid should be inactive: ok=%v err=%v", ok, err)
	}

	// redeem path
	results := make(chan map[string]any, 1)
	a.session = &Session{Name: "Steve", UUID: "u-1"}
	a.emit = func(ev string, p any) {
		if ev == "keyResult" {
			results <- p.(map[string]any)
		}
	}
	a.RedeemKey("DTX-GOOD-GOOD-GOOD")
	select {
	case r := <-results:
		if r["ok"] != true {
			t.Fatalf("redeem good key: %v", r)
		}
	case <-time.After(5 * time.Second):
		t.Fatal("redeem timed out")
	}
	if gotRedeem["uuid"] != "u-1" || gotRedeem["key"] != "DTX-GOOD-GOOD-GOOD" {
		t.Fatalf("redeem payload: %v", gotRedeem)
	}
}
