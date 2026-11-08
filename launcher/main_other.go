//go:build !windows

package main

import "fmt"

// The launcher is a Windows program; this stub only lets the logic be tested elsewhere.
func main() {
	fmt.Println("Dettex Launcher runs on Windows. Logic tests: go test ./...")
}
