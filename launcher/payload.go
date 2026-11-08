package main

import (
	"crypto/sha256"
	"encoding/binary"
	"encoding/hex"
	"errors"
	"io"
	"os"
	"path/filepath"
)

// The Dettex client is carried inside the launcher exe: the jar is appended to the end of the file,
// followed by a 16-byte trailer [8 bytes little-endian jar size]["DTXCLNT1"]. Windows ignores
// bytes after the program image, so the exe still runs normally. pack-launcher.bat writes it.
const payloadMagic = "DTXCLNT1"

// embeddedPayload returns where the embedded client jar sits inside the exe.
func embeddedPayload(exe string) (offset, size int64, err error) {
	f, err := os.Open(exe)
	if err != nil {
		return 0, 0, err
	}
	defer f.Close()
	st, err := f.Stat()
	if err != nil {
		return 0, 0, err
	}
	if st.Size() < 16 {
		return 0, 0, errors.New("no payload")
	}
	trailer := make([]byte, 16)
	if _, err := f.ReadAt(trailer, st.Size()-16); err != nil {
		return 0, 0, err
	}
	if string(trailer[8:]) != payloadMagic {
		return 0, 0, errors.New("no payload")
	}
	size = int64(binary.LittleEndian.Uint64(trailer[:8]))
	offset = st.Size() - 16 - size
	if size <= 0 || offset < 0 {
		return 0, 0, errors.New("broken payload")
	}
	// a jar is a zip: it starts with "PK"
	head := make([]byte, 2)
	if _, err := f.ReadAt(head, offset); err != nil || string(head) != "PK" {
		return 0, 0, errors.New("broken payload")
	}
	return offset, size, nil
}

// extractEmbeddedClient writes the client jar carried in exe to dir (once per version of it) and
// returns its path. The file name holds a hash, so a new launcher build replaces the old client.
func extractEmbeddedClient(exe, dir string) (string, error) {
	offset, size, err := embeddedPayload(exe)
	if err != nil {
		return "", err
	}
	f, err := os.Open(exe)
	if err != nil {
		return "", err
	}
	defer f.Close()
	h := sha256.New()
	if _, err := io.Copy(h, io.NewSectionReader(f, offset, size)); err != nil {
		return "", err
	}
	name := "DettexClient-" + hex.EncodeToString(h.Sum(nil))[:12] + ".jar"
	dst := filepath.Join(dir, name)
	if st, err := os.Stat(dst); err == nil && st.Size() == size {
		return dst, nil
	}
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return "", err
	}
	// old extracted versions are no longer needed
	if old, _ := filepath.Glob(filepath.Join(dir, "DettexClient-*.jar")); old != nil {
		for _, o := range old {
			_ = os.Remove(o)
		}
	}
	tmp := dst + ".part"
	out, err := os.Create(tmp)
	if err != nil {
		return "", err
	}
	if _, err := io.Copy(out, io.NewSectionReader(f, offset, size)); err != nil {
		out.Close()
		_ = os.Remove(tmp)
		return "", err
	}
	if err := out.Close(); err != nil {
		return "", err
	}
	return dst, os.Rename(tmp, dst)
}

// appendPayload makes out = base exe + client jar + trailer (used by tools/pack and the tests).
func appendPayload(baseExe, jar, out string) error {
	b, err := os.ReadFile(baseExe)
	if err != nil {
		return err
	}
	// a base that already carries a client: cut it off first
	if off, _, err := embeddedPayload(baseExe); err == nil {
		b = b[:off]
	}
	j, err := os.ReadFile(jar)
	if err != nil {
		return err
	}
	if len(j) < 2 || string(j[:2]) != "PK" {
		return errors.New(jar + " is not a jar")
	}
	trailer := make([]byte, 16)
	binary.LittleEndian.PutUint64(trailer[:8], uint64(len(j)))
	copy(trailer[8:], payloadMagic)
	data := make([]byte, 0, len(b)+len(j)+16)
	data = append(append(append(data, b...), j...), trailer...)
	return os.WriteFile(out, data, 0o755)
}
