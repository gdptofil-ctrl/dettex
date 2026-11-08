package main

import (
	"crypto/md5"
	"errors"
	"fmt"
	"strings"
)

// Offline sign-in: a nickname is enough, no Microsoft account and no licence.
// The UUID is derived exactly like a vanilla offline server does it
// (UUIDv3 / MD5 of "OfflinePlayer:<nick>"), so client and server agree on the player.
func OfflineSession(name string) (*Session, error) {
	name = strings.TrimSpace(name)
	if !validNick(name) {
		return nil, errors.New("ник: 3–16 символов, только латиница, цифры и _")
	}
	return &Session{
		Name:    name,
		UUID:    offlineUUID(name),
		MCToken: "0",
		Offline: true,
	}, nil
}

func validNick(name string) bool {
	if len(name) < 3 || len(name) > 16 {
		return false
	}
	for _, r := range name {
		switch {
		case r >= 'a' && r <= 'z', r >= 'A' && r <= 'Z', r >= '0' && r <= '9', r == '_':
		default:
			return false
		}
	}
	return true
}

// offlineUUID returns 32 hex chars (no dashes), like the Mojang profile id.
func offlineUUID(name string) string {
	sum := md5.Sum([]byte("OfflinePlayer:" + name))
	sum[6] = (sum[6] & 0x0f) | 0x30 // version 3 (name based, MD5)
	sum[8] = (sum[8] & 0x3f) | 0x80 // RFC 4122 variant
	return fmt.Sprintf("%x", sum)
}
