//go:build windows

package main

import (
	"crypto/sha256"
	"encoding/hex"
	"os"
	"strconv"
	"strings"
	"sync"
	"unsafe"

	"golang.org/x/sys/windows"
)

var (
	machineOnce sync.Once
	machineHash string
)

// machineID is a short, stable fingerprint of this PC (computer name + system
// volume serial). A licence key is bound to it on first activation.
func machineID() string {
	machineOnce.Do(func() {
		parts := []string{strings.ToUpper(os.Getenv("COMPUTERNAME")), volumeSerial(systemDrive())}
		sum := sha256.Sum256([]byte(strings.Join(parts, "|")))
		machineHash = strings.ToUpper(hex.EncodeToString(sum[:8]))
	})
	return machineHash
}

func systemDrive() string {
	d := os.Getenv("SystemDrive")
	if d == "" {
		d = "C:"
	}
	return d + `\`
}

func volumeSerial(root string) string {
	rootPtr, err := windows.UTF16PtrFromString(root)
	if err != nil {
		return ""
	}
	var serial, maxComp, flags uint32
	var volName [261]uint16
	var fsName [261]uint16
	r, _, _ := windows.NewLazySystemDLL("kernel32.dll").NewProc("GetVolumeInformationW").Call(
		uintptr(unsafe.Pointer(rootPtr)),
		uintptr(unsafe.Pointer(&volName[0])), uintptr(len(volName)),
		uintptr(unsafe.Pointer(&serial)),
		uintptr(unsafe.Pointer(&maxComp)), uintptr(unsafe.Pointer(&flags)),
		uintptr(unsafe.Pointer(&fsName[0])), uintptr(len(fsName)),
	)
	if r == 0 {
		return ""
	}
	return strconv.FormatUint(uint64(serial), 16)
}
