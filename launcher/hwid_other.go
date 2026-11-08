//go:build !windows

package main

// machineID is only used on Windows; tests on other systems get a stub.
func machineID() string { return "OTHER-OS" }
