package main

import (
	"context"
	"testing"
)

func TestOfflineSession(t *testing.T) {
	s, err := OfflineSession("Notch")
	if err != nil {
		t.Fatalf("OfflineSession: %v", err)
	}
	// UUIDv3 of "OfflinePlayer:Notch" — the same value a vanilla offline server uses.
	if s.UUID != "b50ad385829d3141a2167e7d7539ba7f" {
		t.Fatalf("UUID = %s", s.UUID)
	}
	if !s.Offline || s.MCToken != "0" || s.Name != "Notch" {
		t.Fatalf("unexpected session: %+v", s)
	}
	acc := s.Account()
	if !acc.Offline || acc.AccessToken != "0" {
		t.Fatalf("unexpected account: %+v", acc)
	}
}

func TestOfflineNickValidation(t *testing.T) {
	for _, bad := range []string{"", "ab", "abcdefghijklmnopq", "bad nick", "ник", "a-b"} {
		if _, err := OfflineSession(bad); err == nil {
			t.Errorf("%q accepted", bad)
		}
	}
	for _, ok := range []string{"abc", "Player_1", "abcdefghijklmnop"} {
		if _, err := OfflineSession(ok); err != nil {
			t.Errorf("%q rejected: %v", ok, err)
		}
	}
}

func TestOfflineFreshNeverNeedsNetwork(t *testing.T) {
	s, _ := OfflineSession("Steam")
	got, err := s.Fresh(context.Background())
	if err != nil {
		t.Fatalf("Fresh: %v", err)
	}
	if got != s {
		t.Fatalf("Fresh returned a different session")
	}
}
