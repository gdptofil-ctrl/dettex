// pack: DettexLauncher.exe = base launcher exe + Dettex client jar (see ../../payload.go).
//
//	go run ./tools/pack <base.exe> <client.jar> <out.exe>
package main

import (
	"encoding/binary"
	"fmt"
	"os"
)

const magic = "DTXCLNT1"

func main() {
	if len(os.Args) != 4 {
		fmt.Println("usage: pack <base.exe> <client.jar> <out.exe>")
		os.Exit(2)
	}
	base, err := os.ReadFile(os.Args[1])
	check(err)
	if n := len(base); n >= 16 && string(base[n-8:]) == magic {
		size := int(binary.LittleEndian.Uint64(base[n-16 : n-8]))
		base = base[:n-16-size] // already packed: replace the client
	}
	jar, err := os.ReadFile(os.Args[2])
	check(err)
	if len(jar) < 2 || string(jar[:2]) != "PK" {
		check(fmt.Errorf("%s is not a jar", os.Args[2]))
	}
	t := make([]byte, 16)
	binary.LittleEndian.PutUint64(t[:8], uint64(len(jar)))
	copy(t[8:], magic)
	check(os.WriteFile(os.Args[3], append(append(base, jar...), t...), 0o755))
	fmt.Printf("%s: launcher %d KB + client %d KB\n", os.Args[3], len(base)/1024, len(jar)/1024)
}

func check(err error) {
	if err != nil {
		fmt.Println("error:", err)
		os.Exit(1)
	}
}
