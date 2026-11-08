//go:build windows

package main

import (
	"context"
	"crypto/rand"
	_ "embed"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"net"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"strings"
	"time"
	"unsafe"

	webview2 "github.com/jchv/go-webview2"
	"golang.org/x/sys/windows"
)

//go:embed ui/index.html
var indexHTML []byte

var (
	user32            = windows.NewLazySystemDLL("user32.dll")
	shell32           = windows.NewLazySystemDLL("shell32.dll")
	dwmapi            = windows.NewLazySystemDLL("dwmapi.dll")
	pSetWindowLongPtr = user32.NewProc("SetWindowLongPtrW")
	pSetWindowPos     = user32.NewProc("SetWindowPos")
	pShowWindow       = user32.NewProc("ShowWindow")
	pReleaseCapture   = user32.NewProc("ReleaseCapture")
	pSendMessage      = user32.NewProc("SendMessageW")
	pGetDpiForSystem  = user32.NewProc("GetDpiForSystem")
	pGetSystemMetrics = user32.NewProc("GetSystemMetrics")
	pSetForeground    = user32.NewProc("SetForegroundWindow")
	pPostMessage      = user32.NewProc("PostMessageW")
	pExtractIcon      = shell32.NewProc("ExtractIconW")
	pDwmSetAttr       = dwmapi.NewProc("DwmSetWindowAttribute")
	pDwmExtend        = dwmapi.NewProc("DwmExtendFrameIntoClientArea")
	pCreateMutex      = windows.NewLazySystemDLL("kernel32.dll").NewProc("CreateMutexW")
)

const (
	baseW = 1040
	baseH = 640
)

func jsString(v any) string {
	b, _ := json.Marshal(v)
	return string(b)
}

func dpiScale() float64 {
	if pGetDpiForSystem.Find() == nil {
		if d, _, _ := pGetDpiForSystem.Call(); d > 0 {
			return float64(d) / 96.0
		}
	}
	return 1
}

// frameless: no Windows title bar (the page draws its own), rounded corners and a shadow on Windows 11.
func makeFrameless(hwnd uintptr, w, h int) {
	const (
		gwlStyle      = ^uintptr(15) // -16
		wsPopup       = 0x80000000
		wsVisible     = 0x10000000
		wsClipChild   = 0x02000000
		wsMinimizeBox = 0x00020000
		wsSysMenu     = 0x00080000
	)
	pSetWindowLongPtr.Call(hwnd, gwlStyle, wsPopup|wsVisible|wsClipChild|wsMinimizeBox|wsSysMenu)
	sw, _, _ := pGetSystemMetrics.Call(0)
	sh, _, _ := pGetSystemMetrics.Call(1)
	x := (int(sw) - w) / 2
	y := (int(sh) - h) / 2
	pSetWindowPos.Call(hwnd, 0, uintptr(x), uintptr(y), uintptr(w), uintptr(h), 0x0020|0x0004|0x0040) // FRAMECHANGED|NOZORDER|SHOWWINDOW
	if pDwmSetAttr.Find() == nil {
		round := uint32(2) // DWMWCP_ROUND
		pDwmSetAttr.Call(hwnd, 33, uintptr(unsafe.Pointer(&round)), 4)
		dark := uint32(1)
		pDwmSetAttr.Call(hwnd, 20, uintptr(unsafe.Pointer(&dark)), 4) // immersive dark mode
		margins := [4]int32{0, 0, 0, 1}                               // lets DWM draw the shadow
		pDwmExtend.Call(hwnd, uintptr(unsafe.Pointer(&margins)))
	}
}

func setWindowIcon(hwnd uintptr) {
	exe, err := os.Executable()
	if err != nil {
		return
	}
	p, _ := windows.UTF16PtrFromString(exe)
	icon, _, _ := pExtractIcon.Call(0, uintptr(unsafe.Pointer(p)), 0)
	if icon > 1 {
		pSendMessage.Call(hwnd, 0x0080, 1, icon) // WM_SETICON big
		pSendMessage.Call(hwnd, 0x0080, 0, icon) // small
	}
}

// singleInstance: a second launcher just exits (the first one keeps its window).
func singleInstance() bool {
	name, _ := windows.UTF16PtrFromString("Local\\DettexLauncherSingleton")
	h, _, err := pCreateMutex.Call(0, 0, uintptr(unsafe.Pointer(name)))
	return !(h != 0 && err == windows.ERROR_ALREADY_EXISTS)
}

// authScript runs on every page: catches the end of the Microsoft login and adds
// a back button + a drag strip to Microsoft's pages (the window has no title bar).
const authScript = `(function(){
  var href = location.href;
  if (href.indexOf('https://login.live.com/oauth20_desktop.srf') === 0) { window.dtxAuthDone && window.dtxAuthDone(href); return; }
  if (!/(^|\.)(live|microsoft|microsoftonline|xboxlive|msauth|msftauth)\.(com|net)$/.test(location.hostname)) return;
  document.addEventListener('DOMContentLoaded', function(){
    var bar = document.createElement('div');
    bar.style.cssText = 'position:fixed;left:0;top:0;right:0;height:34px;z-index:2147483647;background:linear-gradient(90deg,#0b0b14,#16112a);display:flex;align-items:center;gap:10px;padding:0 10px;font:600 13px Segoe UI,sans-serif;color:#cfd3ff;box-shadow:0 2px 12px rgba(0,0,0,.4)';
    bar.innerHTML = '<span style="cursor:pointer;padding:4px 10px;border-radius:8px;background:rgba(255,255,255,.08)" id="dtxBack">← Назад в Dettex</span><span style="opacity:.6">Вход через Microsoft · безопасно, на сайте Microsoft</span>';
    bar.addEventListener('mousedown', function(e){ if (e.target.id !== 'dtxBack') window.dtxDrag && window.dtxDrag(); });
    document.body.appendChild(bar);
    document.body.style.paddingTop = '34px';
    document.getElementById('dtxBack').onclick = function(){ window.dtxAuthCancel && window.dtxAuthCancel(); };
  });
})();`

func dbg(format string, args ...any) {
	if os.Getenv("DETTEX_DEBUG") == "" {
		return
	}
	f, err := os.OpenFile(filepath.Join(os.TempDir(), "dettex-debug.log"), os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0o644)
	if err != nil {
		return
	}
	defer f.Close()
	_, _ = f.WriteString(time.Now().Format("15:04:05.000 ") + fmt.Sprintf(format, args...) + "\n")
}

func main() {
	dbg("main start")
	exe, _ := os.Executable()
	if len(os.Args) > 1 && os.Args[1] == "--update" {
		a := NewApp(filepath.Dir(exe))
		_, err := a.install(context.Background(), a.settings)
		if err != nil {
			fmt.Println("UPDATE ERROR:", err)
			dbg("update error: %v", err)
		} else {
			fmt.Println("UPDATE OK")
			dbg("update ok")
		}
		return
	}
	// --play [nick]: headless launch for testing / when the window is not needed.
	// Uses the saved session (Microsoft or offline); if there is none, signs in offline
	// with the given nick (default "Dettex"). The usual licence check still applies.
	if len(os.Args) > 1 && os.Args[1] == "--play" {
		a := NewApp(filepath.Dir(exe))
		sess := a.session
		if sess == nil {
			name := "Dettex"
			if len(os.Args) > 2 {
				name = os.Args[2]
			}
			s, err := OfflineSession(name)
			if err != nil {
				fmt.Println("PLAY ERROR:", err)
				return
			}
			a.mu.Lock()
			a.session = s
			a.saveSession()
			a.mu.Unlock()
			sess = s
		}
		if !a.licenseActiveLocal() {
			fmt.Println("PLAY ERROR: ключ не активирован (получи его в Telegram-боте @Dettexclient_bot)")
			return
		}
		fmt.Println("PLAY: запускаю", sess.Name)
		if err := a.play(context.Background(), a.settings, sess); err != nil {
			fmt.Println("PLAY ERROR:", err)
			dbg("play error: %v", err)
			return
		}
		fmt.Println("PLAY OK")
		return
	}
	runtime.LockOSThread()
	if !singleInstance() {
		dbg("another instance, exit")
		return
	}
	dbg("single instance ok")
	app := NewApp(filepath.Dir(exe))
	dbg("app created")

	// the page is served from 127.0.0.1 with a random path (other local pages cannot reach it)
	tok := make([]byte, 12)
	_, _ = rand.Read(tok)
	secret := hex.EncodeToString(tok)
	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		messageBox("Dettex Launcher", "Не удалось запустить интерфейс: "+err.Error(), true)
		return
	}
	mux := http.NewServeMux()
	mux.HandleFunc("/"+secret+"/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		w.Header().Set("Cache-Control", "no-store")
		_, _ = w.Write(indexHTML)
	})
	go http.Serve(ln, mux)
	uiURL := "http://" + ln.Addr().String() + "/" + secret + "/"
	dbg("http on %s", uiURL)

	scale := dpiScale()
	W, H := int(float64(baseW)*scale), int(float64(baseH)*scale)
	w := webview2.NewWithOptions(webview2.WebViewOptions{
		Debug:     os.Getenv("DETTEX_DEBUG") != "",
		AutoFocus: true,
		DataPath:  filepath.Join(app.layout.Root, "webview"),
		WindowOptions: webview2.WindowOptions{
			Title: "Dettex Launcher", Width: uint(W), Height: uint(H), Center: true,
		},
	})
	if w == nil {
		dbg("webview2 nil")
		messageBox("Dettex Launcher", "Для лаунчера нужен Microsoft Edge WebView2 Runtime (обычно уже есть в Windows 10/11).\nСейчас откроется страница загрузки: установи его и запусти лаунчер снова.", true)
		openURL("https://go.microsoft.com/fwlink/p/?LinkId=2124703")
		return
	}
	dbg("webview created")
	defer w.Destroy()
	hwnd := uintptr(w.Window())
	makeFrameless(hwnd, W, H)
	setWindowIcon(hwnd)

	windowGone := false
	app.emit = func(ev string, payload any) {
		if windowGone {
			if ev == "error" {
				if s, ok := payload.(string); ok {
					messageBox("Dettex", s, true)
				}
			}
			return
		}
		js := "window.dtx&&window.dtx.on(" + jsString(ev) + "," + jsString(payload) + ")"
		w.Dispatch(func() { w.Eval(js) })
	}
	app.onGameStarted = func() {
		w.Dispatch(func() {
			if app.settings.CloseOnLaunch {
				// free the launcher's memory while you play; the small Go part stays to report crashes
				windowGone = true
				pPostMessage.Call(hwnd, 0x0010, 0, 0) // WM_CLOSE
			} else {
				pShowWindow.Call(hwnd, 6) // minimize
			}
		})
	}
	app.onGameExited = func(err error, tail []string, early bool) {
		if windowGone {
			return
		}
		w.Dispatch(func() {
			pShowWindow.Call(hwnd, 9) // restore
			pSetForeground.Call(hwnd)
		})
	}

	_ = w.Bind("dtxState", func() State { return app.State() })
	_ = w.Bind("dtxLogin", func() {
		w.Dispatch(func() { w.Navigate(LoginURL()) })
	})
	_ = w.Bind("dtxAuthDone", func(u string) {
		w.Dispatch(func() { w.Navigate(uiURL) })
		app.FinishLogin(u)
	})
	_ = w.Bind("dtxAuthCancel", func() {
		w.Dispatch(func() { w.Navigate(uiURL) })
	})
	_ = w.Bind("dtxLogout", func() { app.Logout() })
	_ = w.Bind("dtxOfflineLogin", func(name string) { app.OfflineLogin(name) })
	_ = w.Bind("dtxPlay", func() { app.Play() })
	_ = w.Bind("dtxUpdate", func() { app.Update() })
	_ = w.Bind("dtxRedeemKey", func(key string) { app.RedeemKey(key) })
	_ = w.Bind("dtxCancel", func() { app.Cancel() })
	_ = w.Bind("dtxSet", func(key string, value any) { app.SetSetting(key, value) })
	_ = w.Bind("dtxOpen", func(what string) {
		switch what {
		case "game":
			openFolder(app.layout.Game())
		case "mods":
			openFolder(app.layout.Mods())
		case "logs":
			openFolder(filepath.Join(app.layout.Game(), "logs"))
		case "launcher":
			openFolder(app.exeDir)
		case "bot":
			openURL("https://t.me/dettexclient_bot")
		default:
			if strings.HasPrefix(what, "https://") {
				openURL(what)
			}
		}
	})
	_ = w.Bind("dtxMinimize", func() { w.Dispatch(func() { pShowWindow.Call(hwnd, 6) }) })
	_ = w.Bind("dtxClose", func() { w.Dispatch(func() { pPostMessage.Call(hwnd, 0x0010, 0, 0) }) })
	_ = w.Bind("dtxDrag", func() {
		w.Dispatch(func() {
			pReleaseCapture.Call()
			pSendMessage.Call(hwnd, 0x00A1, 2, 0) // WM_NCLBUTTONDOWN, HTCAPTION
		})
	})
	w.Init(authScript)
	w.Navigate(uiURL)
	dbg("navigated, entering run")
	w.Run()
	dbg("run returned")

	// window closed: if the game is starting / running, stay (quietly) to report a crash
	windowGone = true
	for {
		app.mu.Lock()
		busy := app.busy
		app.mu.Unlock()
		if !busy {
			break
		}
		time.Sleep(500 * time.Millisecond)
	}
}
