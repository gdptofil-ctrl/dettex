package main

import (
	"crypto/hmac"
	"crypto/sha256"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"
)

// useTestSecret switches the package to the test bot token's secret.
func useTestSecret(t *testing.T) {
	t.Helper()
	old := licenseSecret
	licenseSecret = sha256Sum("DettexLicense/v1|test-token")
	t.Cleanup(func() { licenseSecret = old })
}

func sha256Sum(s string) []byte {
	sum := sha256.Sum256([]byte(s))
	return sum[:]
}

// mintTestKey signs a key the same way the bot does, using the new plan+rank byte.
func mintTestKey(t *testing.T, plan int, uid [4]byte) string {
	t.Helper()
	return mintTestKeyRank(t, plan, rankPlayer, uid)
}

func mintTestKeyRank(t *testing.T, plan, rank int, uid [4]byte) string {
	t.Helper()
	payload := []byte{encodePlanByte(plan, rank), uid[0], uid[1], uid[2], uid[3]}
	mac := hmac.New(sha256.New, licenseSecret)
	mac.Write(payload)
	raw := append(payload, mac.Sum(nil)[:5]...)
	s := base32Encode(raw)
	if len(s) != 16 {
		t.Fatalf("base32 length = %d", len(s))
	}
	return "DTX-" + s[0:4] + "-" + s[4:8] + "-" + s[8:12] + "-" + s[12:16]
}

// The vector was produced by tg-bot/genkey.ps1 (PowerShell), an independent
// implementation, so Go, Java and PowerShell are known to agree. It uses the old
// plan-only byte, so it must still verify and report the Player rank.
func TestVerifyKeyVectorFromGenerator(t *testing.T) {
	useTestSecret(t)
	plan, rank, uid, canonical, err := verifyLicenseKey("dtx a6ju en4e ffjb cqvu")
	if err != nil {
		t.Fatalf("verify: %v", err)
	}
	if plan != 7 {
		t.Fatalf("plan = %d", plan)
	}
	if rank != rankPlayer {
		t.Fatalf("rank = %d", rank)
	}
	if got := hexUID(uid); got != "11223344" {
		t.Fatalf("uid = %s", got)
	}
	if canonical != "DTX-A6JU-EN4E-FFJB-CQVU" {
		t.Fatalf("canonical = %s", canonical)
	}
}

// TestRankEncoding checks every rank and plan round-trips through the signed key.
func TestRankEncoding(t *testing.T) {
	useTestSecret(t)
	plans := []int{1, 7, 30, 90, planLifetime}
	ranks := []int{rankPlayer, rankMedia, rankAdministrator, rankAdmin}
	for _, plan := range plans {
		for _, rank := range ranks {
			key := mintTestKeyRank(t, plan, rank, [4]byte{0x11, 0x22, 0x33, 0x44})
			gotPlan, gotRank, uid, _, err := verifyLicenseKey(key)
			if err != nil {
				t.Fatalf("plan=%d rank=%d: %v", plan, rank, err)
			}
			if gotPlan != plan || gotRank != rank || hexUID(uid) != "11223344" {
				t.Fatalf("plan=%d rank=%d -> plan=%d rank=%d uid=%s", plan, rank, gotPlan, gotRank, hexUID(uid))
			}
			if rankText(rank) == "" {
				t.Fatalf("empty rank text for %d", rank)
			}
		}
	}
	if planText(planLifetime) != "навсегда" || rankText(rankAdmin) != "Admin" || rankText(rankAdministrator) != "Administrator" {
		t.Fatal("unexpected plan/rank text")
	}
}

func hexUID(uid [4]byte) string {
	const digits = "0123456789ABCDEF"
	out := make([]byte, 8)
	for i, b := range uid {
		out[i*2] = digits[b>>4]
		out[i*2+1] = digits[b&15]
	}
	return string(out)
}

func mintTestDETEXKey(t *testing.T, plan int, uid [4]byte) string {
	t.Helper()
	return "DETEX-" + strings.TrimPrefix(mintTestKey(t, plan, uid), "DTX-")
}

func mintTestDETEXKeyRank(t *testing.T, plan, rank int, uid [4]byte) string {
	t.Helper()
	return "DETEX-" + strings.TrimPrefix(mintTestKeyRank(t, plan, rank, uid), "DTX-")
}

// The old Python bot stored random DETEX keys in its database. These two were
// issued before the launcher supported offline signatures and stay valid.
func TestLegacyDETEXKeys(t *testing.T) {
	for _, key := range []string{"DETEX-B48C-956D-2021", "DETEX-E74D-80EA-447D"} {
		plan, rank, uid, canonical, err := verifyLicenseKey(strings.ToLower(key))
		if err != nil {
			t.Fatalf("%s: %v", key, err)
		}
		if canonical != key {
			t.Fatalf("canonical = %s, want %s", canonical, key)
		}
		if plan != planLifetime || rank != rankPlayer {
			t.Fatalf("%s plan = %d rank = %d", key, plan, rank)
		}
		if hexUID(uid) == "" {
			t.Fatalf("%s has no uid", key)
		}
	}
	if _, _, _, _, err := verifyLicenseKey("DETEX-AAAA-AAAA-AAAA"); err == nil {
		t.Fatal("unknown legacy DETEX key accepted")
	}
}

func TestDETEXSignedKeys(t *testing.T) {
	useTestSecret(t)
	key := mintTestDETEXKeyRank(t, 90, rankAdministrator, [4]byte{0xAB, 0xCD, 0xEF, 0x01})
	plan, rank, uid, canonical, err := verifyLicenseKey(key)
	if err != nil {
		t.Fatalf("verify: %v", err)
	}
	if plan != 90 || rank != rankAdministrator || canonical != key || hexUID(uid) != "ABCDEF01" {
		t.Fatalf("plan=%d rank=%d canonical=%s uid=%s", plan, rank, canonical, hexUID(uid))
	}
	if planText(plan) != "90 дней" {
		t.Fatalf("plan text = %q", planText(plan))
	}
	// a signed DETEX key with a broken signature must be rejected
	last := key[len(key)-1]
	repl := byte('A')
	if last == 'A' {
		repl = 'B'
	}
	bad := key[:len(key)-1] + string(repl)
	if _, _, _, _, err := verifyLicenseKey(bad); err == nil {
		t.Fatal("tampered DETEX key accepted")
	}
}

func TestRejectsTamperedKeys(t *testing.T) {
	useTestSecret(t)
	key := mintTestKey(t, 30, [4]byte{1, 2, 3, 4})
	if _, _, _, _, err := verifyLicenseKey(key); err != nil {
		t.Fatalf("own key rejected: %v", err)
	}
	// flip one character in the payload (first group)
	bad := "DTX-Z" + key[5:]
	if _, _, _, _, err := verifyLicenseKey(bad); err == nil {
		t.Fatal("tampered key accepted")
	}
	for _, raw := range []string{"", "DTX", "DTX-AAAA-AAAA-AAAA-AAAA", "DTX-GOOD-GOOD-GOOD"} {
		if _, _, _, _, err := verifyLicenseKey(raw); err == nil {
			t.Errorf("%q accepted", raw)
		}
	}
}

func TestActivatePlansAndExpiry(t *testing.T) {
	useTestSecret(t)
	dir := t.TempDir()
	a := &App{layout: Layout{Root: dir}, emit: func(string, any) {}}

	// 30 days with the Media rank
	key30 := mintTestKeyRank(t, 30, rankMedia, [4]byte{9, 9, 9, 9})
	st, err := a.activateLicenseKey(key30)
	if err != nil {
		t.Fatalf("activate 30d: %v", err)
	}
	if !st.Active || st.Plan != 30 || st.PlanText != "30 дней" || st.DaysLeft != 30 || st.UID != "09090909" {
		t.Fatalf("30d state: %+v", st)
	}
	if st.Rank != rankMedia || st.RankText != "Media" {
		t.Fatalf("30d rank: %+v", st)
	}
	if st.Expires == 0 || st.Lifetime {
		t.Fatalf("30d should expire: %+v", st)
	}

	// the same key again keeps the original activation time
	firstExpires := st.Expires
	again, err := a.activateLicenseKey(key30)
	if err != nil {
		t.Fatalf("reactivate: %v", err)
	}
	if again.Expires != firstExpires {
		t.Fatalf("expiry moved: %d -> %d", firstExpires, again.Expires)
	}

	// lifetime with the Admin rank
	lifeKey := mintTestKeyRank(t, planLifetime, rankAdmin, [4]byte{7, 7, 7, 7})
	life, err := a.activateLicenseKey(lifeKey)
	if err != nil {
		t.Fatalf("activate life: %v", err)
	}
	if !life.Active || !life.Lifetime || life.Expires != 0 || life.PlanText != "навсегда" {
		t.Fatalf("life state: %+v", life)
	}
	if life.Rank != rankAdmin || life.RankText != "Admin" {
		t.Fatalf("life rank: %+v", life)
	}

	// expired record is not active and cannot be restarted with the same key
	a.license.Expires = time.Now().Add(-time.Hour).Unix()
	if got := a.license.state(time.Now()); got.Active || got.Reason == "" {
		t.Fatalf("expired state: %+v", got)
	}
	if _, err := a.activateLicenseKey(lifeKey); err == nil || err.Error() != "срок действия ключа истёк" {
		t.Fatalf("expired key re-activated: %v", err)
	}
}

func TestLicenseBindToMachineAndPersistence(t *testing.T) {
	useTestSecret(t)
	dir := t.TempDir()
	a := &App{layout: Layout{Root: dir}, emit: func(string, any) {}}
	key := mintTestKeyRank(t, 7, rankAdministrator, [4]byte{5, 5, 5, 5})
	if _, err := a.activateLicenseKey(key); err != nil {
		t.Fatalf("activate: %v", err)
	}
	if _, err := os.Stat(filepath.Join(dir, licenseFileName)); err != nil {
		t.Fatalf("license.dat not written: %v", err)
	}

	// another App (a restart) loads the same licence and its rank
	b := &App{layout: Layout{Root: dir}, emit: func(string, any) {}}
	b.loadLicense()
	if !b.licenseActiveLocal() {
		t.Fatal("licence not restored after restart")
	}
	if b.license == nil || b.license.Rank != rankAdministrator {
		t.Fatalf("rank not restored: %+v", b.license)
	}

	// a copied licence file on another PC is refused
	b.license.HWID = "SOME-OTHER-PC"
	st := b.license.state(time.Now())
	if st.Active || st.Reason != "ключ привязан к другому компьютеру" {
		t.Fatalf("hwid check: %+v", st)
	}
}
