//go:build windows

package main

import (
	"net"
	"net/http"
	"net/url"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"sync"
	"syscall"
	"time"
	"unsafe"

	"golang.org/x/sys/windows"
	"golang.org/x/sys/windows/registry"
)

func dataRoot() string {
	if a := os.Getenv("APPDATA"); a != "" {
		return filepath.Join(a, ".dettex")
	}
	h, _ := os.UserHomeDir()
	return filepath.Join(h, ".dettex")
}

// systemProxy: HTTPS_PROXY if set, else the Windows proxy (Settings -> Network -> Proxy, which most
// VPN / proxy apps fill in). A VPN that routes everything needs nothing here.
var (
	proxyMu   sync.Mutex
	proxyURL  *url.URL
	proxyRead time.Time
)

func systemProxy(req *http.Request) (*url.URL, error) {
	if u, err := http.ProxyFromEnvironment(req); u != nil || err != nil {
		return u, err
	}
	host := req.URL.Hostname()
	if host == "localhost" || strings.HasPrefix(host, "127.") {
		return nil, nil
	}
	proxyMu.Lock()
	defer proxyMu.Unlock()
	if time.Since(proxyRead) > 30*time.Second {
		proxyRead = time.Now()
		proxyURL = readWindowsProxy()
		// Windows keeps ProxyEnable=1 after sing-box/xray/clash exits. Every request
		// would then fail with "connection refused"; fall back to a direct connection
		// while the proxy app is off.
		if proxyURL != nil && !proxyReachable(proxyURL) {
			proxyURL = nil
		}
	}
	return proxyURL, nil
}

// proxyReachable reports whether the local proxy app is actually listening.
func proxyReachable(u *url.URL) bool {
	host := u.Host
	if u.Port() == "" {
		port := "80"
		switch u.Scheme {
		case "socks5", "socks5h":
			port = "1080"
		}
		host = net.JoinHostPort(u.Hostname(), port)
	}
	c, err := net.DialTimeout("tcp", host, 800*time.Millisecond)
	if err != nil {
		return false
	}
	_ = c.Close()
	return true
}

func readWindowsProxy() *url.URL {
	k, err := registry.OpenKey(registry.CURRENT_USER, `Software\Microsoft\Windows\CurrentVersion\Internet Settings`, registry.QUERY_VALUE)
	if err != nil {
		return nil
	}
	defer k.Close()
	if on, _, err := k.GetIntegerValue("ProxyEnable"); err != nil || on == 0 {
		return nil
	}
	s, _, err := k.GetStringValue("ProxyServer")
	if err != nil || s == "" {
		return nil
	}
	// "host:port" or "http=h:p;https=h:p;socks=h:p"
	if strings.Contains(s, "=") {
		var pick string
		for _, part := range strings.Split(s, ";") {
			kv := strings.SplitN(part, "=", 2)
			if len(kv) != 2 {
				continue
			}
			switch strings.ToLower(strings.TrimSpace(kv[0])) {
			case "https":
				pick = "http://" + kv[1]
			case "http":
				if pick == "" {
					pick = "http://" + kv[1]
				}
			case "socks":
				if pick == "" {
					pick = "socks5://" + kv[1]
				}
			}
		}
		s = pick
	} else if !strings.Contains(s, "://") {
		s = "http://" + s
	}
	u, err := url.Parse(s)
	if err != nil {
		return nil
	}
	return u
}

func hideConsole(cmd *exec.Cmd) {
	cmd.SysProcAttr = &syscall.SysProcAttr{HideWindow: true, CreationFlags: 0x08000000} // CREATE_NO_WINDOW
}

// protect / unprotect: Windows DPAPI, only this Windows user can read the saved login.
func protect(data []byte) ([]byte, error) {
	if len(data) == 0 {
		return nil, nil
	}
	in := windows.DataBlob{Size: uint32(len(data)), Data: &data[0]}
	var out windows.DataBlob
	if err := windows.CryptProtectData(&in, nil, nil, 0, nil, windows.CRYPTPROTECT_UI_FORBIDDEN, &out); err != nil {
		return nil, err
	}
	defer windows.LocalFree(windows.Handle(unsafe.Pointer(out.Data)))
	return append([]byte(nil), unsafe.Slice(out.Data, out.Size)...), nil
}

func unprotect(data []byte) ([]byte, error) {
	if len(data) == 0 {
		return nil, nil
	}
	in := windows.DataBlob{Size: uint32(len(data)), Data: &data[0]}
	var out windows.DataBlob
	if err := windows.CryptUnprotectData(&in, nil, nil, 0, nil, windows.CRYPTPROTECT_UI_FORBIDDEN, &out); err != nil {
		return nil, err
	}
	defer windows.LocalFree(windows.Handle(unsafe.Pointer(out.Data)))
	return append([]byte(nil), unsafe.Slice(out.Data, out.Size)...), nil
}

type memoryStatusEx struct {
	Length               uint32
	MemoryLoad           uint32
	TotalPhys            uint64
	AvailPhys            uint64
	TotalPageFile        uint64
	AvailPageFile        uint64
	TotalVirtual         uint64
	AvailVirtual         uint64
	AvailExtendedVirtual uint64
}

func totalRAMMB() int {
	var m memoryStatusEx
	m.Length = uint32(unsafe.Sizeof(m))
	proc := windows.NewLazySystemDLL("kernel32.dll").NewProc("GlobalMemoryStatusEx")
	if r, _, _ := proc.Call(uintptr(unsafe.Pointer(&m))); r == 0 {
		return 8192
	}
	return int(m.TotalPhys / (1024 * 1024))
}

func openFolder(path string) {
	_ = os.MkdirAll(path, 0o755)
	_ = exec.Command("explorer.exe", path).Start()
}

func openURL(u string) {
	_ = exec.Command("rundll32.exe", "url.dll,FileProtocolHandler", u).Start()
}

func messageBox(title, text string, isError bool) {
	t, _ := windows.UTF16PtrFromString(title)
	m, _ := windows.UTF16PtrFromString(text)
	flags := uint32(0x40) // MB_ICONINFORMATION
	if isError {
		flags = 0x10 // MB_ICONERROR
	}
	_, _ = windows.MessageBox(0, m, t, flags)
}
