//go:build !windows

package main

import (
	"fmt"
	"net/http"
	"net/url"
	"os"
	"os/exec"
	"path/filepath"
)

// Non-Windows builds exist only for tests.

func dataRoot() string {
	if r := os.Getenv("DETTEX_ROOT"); r != "" {
		return r
	}
	h, _ := os.UserHomeDir()
	return filepath.Join(h, ".dettex")
}

func systemProxy(req *http.Request) (*url.URL, error) { return http.ProxyFromEnvironment(req) }

func hideConsole(cmd *exec.Cmd) {}

func protect(data []byte) ([]byte, error)   { return data, nil }
func unprotect(data []byte) ([]byte, error) { return data, nil }

func totalRAMMB() int { return 16384 }

func openFolder(path string) {}

func openURL(u string) {}

func messageBox(title, text string, isError bool) { fmt.Println(title+":", text) }
