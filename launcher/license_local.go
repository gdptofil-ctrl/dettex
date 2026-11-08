package main

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"
)

// Local licence keys. The Telegram bot (@Dettexclient_bot) mints keys signed with
// a secret derived from its token; the launcher verifies the signature on its own,
// so a licence server is not needed and keys work offline.
//
// Key: DTX-XXXX-XXXX-XXXX-XXXX or DETEX-XXXX-XXXX-XXXX-XXXX
// (16 chars of base32, no I/O/0/1)
//
//	payload  (5 bytes): plan+rank byte + 4-byte UID
//	signature(5 bytes): HMAC-SHA256(secret, payload) truncated to 5 bytes
//
// The plan+rank byte uses two layouts, so keys minted before ranks existed keep
// working:
//
//	old: 1/7/30/90 days, 255 = lifetime, rank is Player
//	new: 0x80 | rank<<5 | planIndex (0=1d, 1=7d, 2=30d, 3=90d, 4=lifetime)
//
// The secret is SHA-256("DettexLicense/v1|" + bot token). It is baked in as a hash,
// so the bot token itself never ships inside the exe.
//
// Older DETEX keys (DETEX-XXXX-XXXX-XXXX) were random and stored in the bot's
// database, so they cannot carry a signature. A small allowlist keeps the keys
// that were already issued usable.
const (
	licenseSecretHex = "5b905601938784ad27a25b1f643c62ebc178dff318ad222ac9196eb3f52508b9"
	licenseAlphabet  = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
	planLifetime     = 255
	licenseFileName  = "license.dat"
)

// Ranks are the same ones the Telegram bot grants: Admin, Administrator, Media,
// Player. A key carries its rank, so the launcher can show it without a server.
const (
	rankPlayer = iota
	rankMedia
	rankAdministrator
	rankAdmin
	rankCount
)

var planByIndex = [...]int{1, 7, 30, 90, planLifetime}

func rankText(rank int) string {
	switch rank {
	case rankMedia:
		return "Media"
	case rankAdministrator:
		return "Administrator"
	case rankAdmin:
		return "Admin"
	default:
		return "Player"
	}
}

func validRank(rank int) int {
	if rank < rankPlayer || rank >= rankCount {
		return rankPlayer
	}
	return rank
}

// encodePlanByte builds the new plan+rank byte.
func encodePlanByte(plan, rank int) byte {
	idx := 2 // 30 days if the plan is not a standard one
	for i, p := range planByIndex {
		if p == plan {
			idx = i
			break
		}
	}
	return byte(0x80 | validRank(rank)<<5 | idx)
}

// decodePlanByte understands both the old plan-only byte and the new plan+rank
// byte. Old keys have no rank, so they are shown as Player.
func decodePlanByte(b byte) (plan, rank int, err error) {
	if b == planLifetime {
		return planLifetime, rankPlayer, nil
	}
	if b&0x80 == 0 {
		if b < 1 {
			return 0, 0, errors.New("неизвестный срок действия ключа")
		}
		return int(b), rankPlayer, nil
	}
	rank = int((b >> 5) & 3)
	idx := int(b & 0x1f)
	if idx >= len(planByIndex) {
		return 0, 0, errors.New("неизвестный срок действия ключа")
	}
	return planByIndex[idx], rank, nil
}

type legacyDETEX struct {
	Plan int
}

// legacyDETEXKeys are unsigned keys minted by the old bot (detex_bot).
// The UID is derived from the key itself.
var legacyDETEXKeys = map[string]legacyDETEX{
	"DETEX-B48C-956D-2021": {Plan: planLifetime},
	"DETEX-E74D-80EA-447D": {Plan: planLifetime},
}

var licenseSecret = mustHex(licenseSecretHex)

func mustHex(s string) []byte {
	b, err := hex.DecodeString(s)
	if err != nil {
		panic("license secret: " + err.Error())
	}
	return b
}

// LicenseState is what the window shows about the key.
type LicenseState struct {
	Active   bool   `json:"active"`
	Plan     int    `json:"plan"`     // days, 255 = lifetime
	PlanText string `json:"planText"` // "30 дней", "навсегда"
	Lifetime bool   `json:"lifetime"`
	Rank     int    `json:"rank"`
	RankText string `json:"rankText"`
	UID      string `json:"uid"`
	Expires  int64  `json:"expires"` // unix seconds, 0 = lifetime
	DaysLeft int    `json:"daysLeft"`
	Reason   string `json:"reason,omitempty"` // why it is not active
}

// licenseRecord is stored (DPAPI-encrypted) in license.dat.
type licenseRecord struct {
	Key       string `json:"key"`
	UID       string `json:"uid"`
	Plan      int    `json:"plan"`
	Rank      int    `json:"rank"`
	Activated int64  `json:"activated"`
	Expires   int64  `json:"expires"`
	HWID      string `json:"hwid"`
}

func planText(plan int) string {
	switch plan {
	case 1:
		return "1 день"
	case 7:
		return "7 дней"
	case 30:
		return "30 дней"
	case 90:
		return "90 дней"
	case planLifetime:
		return "навсегда"
	default:
		return fmt.Sprintf("%d дн.", plan)
	}
}

func base32Encode(data []byte) string {
	var sb strings.Builder
	buf, bits := 0, 0
	for _, x := range data {
		buf = buf<<8 | int(x)
		bits += 8
		for bits >= 5 {
			bits -= 5
			sb.WriteByte(licenseAlphabet[(buf>>uint(bits))&31])
		}
	}
	if bits > 0 {
		sb.WriteByte(licenseAlphabet[(buf<<uint(5-bits))&31])
	}
	return sb.String()
}

func base32Decode(s string) ([]byte, error) {
	var out []byte
	buf, bits := 0, 0
	for _, c := range s {
		i := strings.IndexRune(licenseAlphabet, c)
		if i < 0 {
			return nil, fmt.Errorf("в ключе недопустимый символ %q", c)
		}
		buf = buf<<5 | i
		bits += 5
		if bits >= 8 {
			bits -= 8
			out = append(out, byte(buf>>uint(bits)))
		}
	}
	if bits != 0 {
		return nil, errors.New("неверная длина ключа")
	}
	return out, nil
}

// compactLicenseKey uppercases a key and drops spaces/dashes.
func compactLicenseKey(raw string) string {
	var sb strings.Builder
	for _, c := range strings.ToUpper(raw) {
		if (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') {
			sb.WriteRune(c)
		}
	}
	return sb.String()
}

func formatLicenseKey(prefix, body string) string {
	var sb strings.Builder
	sb.WriteString(prefix)
	for i := 0; i+4 <= len(body); i += 4 {
		sb.WriteByte('-')
		sb.WriteString(body[i : i+4])
	}
	return sb.String()
}

func splitLicenseKey(canonical string) (prefix, body string, ok bool) {
	i := strings.IndexByte(canonical, '-')
	if i < 0 {
		return "", "", false
	}
	return canonical[:i], strings.ReplaceAll(canonical[i+1:], "-", ""), true
}

// normalizeLicenseKey accepts "dtx xxxx xxxx..." / "detex-..." and returns the
// canonical text. DETEX keys with 12 characters are the unsigned format used by
// the old bot and are accepted only if they are in legacyDETEXKeys.
func normalizeLicenseKey(raw string) (string, error) {
	t := compactLicenseKey(raw)
	var prefix string
	switch {
	case strings.HasPrefix(t, "DETEX"):
		prefix, t = "DETEX", t[5:]
	case strings.HasPrefix(t, "DTX"):
		prefix, t = "DTX", t[3:]
	default:
		return "", errors.New("ключ должен начинаться с DTX- или DETEX-")
	}
	switch {
	case prefix == "DETEX" && len(t) == 12:
		canonical := formatLicenseKey(prefix, t)
		if _, ok := legacyDETEXKeys[canonical]; !ok {
			return "", errors.New("старый ключ DETEX не найден: такие ключи принимаются только из списка выданных")
		}
		return canonical, nil
	case len(t) == 16:
		for _, c := range t {
			if !strings.ContainsRune(licenseAlphabet, c) {
				return "", fmt.Errorf("в ключе недопустимый символ %q", c)
			}
		}
		return formatLicenseKey(prefix, t), nil
	default:
		return "", errors.New("ключ должен выглядеть так: DTX-XXXX-XXXX-XXXX-XXXX или DETEX-XXXX-XXXX-XXXX-XXXX")
	}
}

// verifyLicenseKey checks the signature and returns the plan, the rank, the key UID and the canonical text.
func verifyLicenseKey(raw string) (plan, rank int, uid [4]byte, canonical string, err error) {
	canonical, err = normalizeLicenseKey(raw)
	if err != nil {
		return 0, 0, uid, "", err
	}
	prefix, body, ok := splitLicenseKey(canonical)
	if !ok {
		return 0, 0, uid, "", errors.New("неверный формат ключа")
	}
	if prefix == "DETEX" && len(body) == 12 {
		lk := legacyDETEXKeys[canonical]
		sum := sha256.Sum256([]byte(canonical))
		copy(uid[:], sum[:4])
		return lk.Plan, rankPlayer, uid, canonical, nil
	}
	rawBytes, err := base32Decode(body)
	if err != nil {
		return 0, 0, uid, "", err
	}
	if len(rawBytes) != 10 {
		return 0, 0, uid, "", errors.New("неверная длина ключа")
	}
	payload, sig := rawBytes[:5], rawBytes[5:]
	mac := hmac.New(sha256.New, licenseSecret)
	mac.Write(payload)
	if !hmac.Equal(sig, mac.Sum(nil)[:5]) {
		return 0, 0, uid, "", errors.New("ключ неверный или подделан")
	}
	plan, rank, err = decodePlanByte(payload[0])
	if err != nil {
		return 0, 0, uid, "", err
	}
	copy(uid[:], payload[1:])
	return plan, rank, uid, canonical, nil
}

func (r *licenseRecord) expiredAt(now time.Time) bool {
	return r.Expires != 0 && !now.Before(time.Unix(r.Expires, 0))
}

func (r *licenseRecord) state(now time.Time) LicenseState {
	rank := validRank(r.Rank)
	st := LicenseState{
		Plan:     r.Plan,
		PlanText: planText(r.Plan),
		Lifetime: r.Plan == planLifetime,
		Rank:     rank,
		RankText: rankText(rank),
		UID:      r.UID,
		Expires:  r.Expires,
	}
	switch {
	case r.HWID != "" && r.HWID != machineID():
		st.Reason = "ключ привязан к другому компьютеру"
	case r.expiredAt(now):
		st.Reason = "срок действия ключа истёк"
	default:
		st.Active = true
	}
	if r.Expires != 0 {
		if left := time.Unix(r.Expires, 0).Sub(now); left > 0 {
			st.DaysLeft = int((left + 24*time.Hour - 1) / (24 * time.Hour))
		}
	}
	return st
}

func (a *App) licensePath() string { return filepath.Join(a.layout.Root, licenseFileName) }

func (a *App) loadLicense() {
	b, err := os.ReadFile(a.licensePath())
	if err != nil {
		return
	}
	plain, err := unprotect(b)
	if err != nil {
		return
	}
	var rec licenseRecord
	if json.Unmarshal(plain, &rec) == nil && rec.Key != "" {
		a.license = &rec
	}
}

func (a *App) saveLicense(rec *licenseRecord) {
	_ = os.MkdirAll(a.layout.Root, 0o755)
	b, _ := json.Marshal(rec)
	if enc, err := protect(b); err == nil {
		_ = os.WriteFile(a.licensePath(), enc, 0o600)
	}
}

func (a *App) licenseState(now time.Time) *LicenseState {
	if a.license == nil {
		return nil
	}
	st := a.license.state(now)
	return &st
}

func (a *App) licenseActiveLocal() bool {
	st := a.licenseState(time.Now())
	return st != nil && st.Active
}

// activateLicenseKey verifies a key and remembers the activation on this computer.
// The duration starts at the first activation and is bound to the machine.
func (a *App) activateLicenseKey(raw string) (*LicenseState, error) {
	plan, rank, uid, canonical, err := verifyLicenseKey(raw)
	if err != nil {
		return nil, err
	}
	hw := machineID()
	now := time.Now()
	if a.license != nil && a.license.Key == canonical {
		if a.license.HWID != "" && a.license.HWID != hw {
			return nil, errors.New("ключ привязан к другому компьютеру")
		}
		if a.license.expiredAt(now) {
			return nil, errors.New("срок действия ключа истёк")
		}
		if a.license.Rank != rank {
			a.license.Rank = rank
			a.saveLicense(a.license)
		}
		st := a.license.state(now)
		return &st, nil
	}
	rec := &licenseRecord{
		Key:       canonical,
		UID:       strings.ToUpper(hex.EncodeToString(uid[:])),
		Plan:      plan,
		Rank:      rank,
		Activated: now.Unix(),
		HWID:      hw,
	}
	if plan != planLifetime {
		rec.Expires = now.Add(time.Duration(plan) * 24 * time.Hour).Unix()
	}
	a.license = rec
	a.saveLicense(rec)
	st := rec.state(now)
	return &st, nil
}
