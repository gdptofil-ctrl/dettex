package main

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"os"
	"path/filepath"
	"strings"
	"time"
)

// Licensing from the launcher: after install the player pastes a key (from the Telegram bot). The
// launcher redeems it against the Dettex server, which grants a licence to the player's account. The
// in-game client then verifies that licence (server-signed, HWID-bound) on its own.
//
// licenseServer is baked in by the deploy script; otherwise it is read from server.txt in the data
// folder (also written by deploy) or next to the launcher.
var licenseServer = ""

func (a *App) serverURL() string {
	if s := normalizeServer(licenseServer); s != "" {
		return s
	}
	for _, p := range []string{filepath.Join(a.layout.Root, "server.txt"), filepath.Join(a.exeDir, "server.txt")} {
		if b, err := os.ReadFile(p); err == nil {
			if s := normalizeServer(string(b)); s != "" {
				return s
			}
		}
	}
	return ""
}

func normalizeServer(u string) string {
	u = strings.TrimSpace(u)
	for strings.HasSuffix(u, "/") {
		u = strings.TrimSuffix(u, "/")
	}
	if u == "" {
		return ""
	}
	if !strings.HasPrefix(u, "http://") && !strings.HasPrefix(u, "https://") {
		u = "https://" + u
	}
	return u
}

func (a *App) licenseJSON(ctx context.Context, path string, body map[string]any, out any) error {
	server := a.serverURL()
	if server == "" {
		return errors.New("сервер лицензий не настроен")
	}
	b, _ := json.Marshal(body)
	req, err := newRequest(ctx, "POST", server+path, bytes.NewReader(b))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := httpClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	return json.NewDecoder(resp.Body).Decode(out)
}

// licenseActive reports whether this account already has a valid licence.
func (a *App) licenseActive(ctx context.Context, uuid string) (bool, error) {
	if a.serverURL() == "" {
		return true, nil // no server configured: licensing is off, do not block
	}
	var r struct {
		Licensed bool `json:"licensed"`
	}
	ctx, cancel := context.WithTimeout(ctx, 20*time.Second)
	defer cancel()
	if err := a.licenseJSON(ctx, "/v1/license/status", map[string]any{"uuid": uuid}, &r); err != nil {
		return false, err
	}
	return r.Licensed, nil
}

// licenseOK: a local bot key first, then a deployed licence server (if configured).
func (a *App) licenseOK(ctx context.Context, uuid string) bool {
	a.mu.Lock()
	local := a.licenseActiveLocal()
	a.mu.Unlock()
	if local {
		return true
	}
	if a.serverURL() != "" {
		if ok, err := a.licenseActive(ctx, uuid); err == nil && ok {
			return true
		}
	}
	return false
}

// RedeemKey activates a key. Bot keys are verified locally (offline); a deployed
// licence server, when configured, is used as a fallback for server-issued keys.
func (a *App) RedeemKey(key string) {
	a.mu.Lock()
	st, lerr := a.activateLicenseKey(key)
	a.mu.Unlock()
	if lerr == nil {
		a.emit("keyResult", map[string]any{
			"ok": true, "plan": st.Plan, "planText": st.PlanText, "lifetime": st.Lifetime,
			"rank": st.Rank, "rankText": st.RankText,
			"expires": st.Expires, "daysLeft": st.DaysLeft, "uid": st.UID,
		})
		a.emit("state", a.State())
		return
	}
	a.mu.Lock()
	sess := a.session
	a.mu.Unlock()
	if a.serverURL() == "" || sess == nil {
		a.emit("keyResult", map[string]any{"ok": false, "reason": lerr.Error()})
		return
	}
	go func() {
		ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
		defer cancel()
		var r struct {
			OK     bool   `json:"ok"`
			Reason string `json:"reason"`
			Role   string `json:"role"`
		}
		err := a.licenseJSON(ctx, "/v1/key/redeem", map[string]any{
			"key": key, "uuid": sess.UUID, "name": sess.Name,
		}, &r)
		if err != nil {
			a.emit("keyResult", map[string]any{"ok": false, "reason": "нет связи с сервером: " + err.Error()})
			return
		}
		if !r.OK {
			a.emit("keyResult", map[string]any{"ok": false, "reason": r.Reason})
			return
		}
		a.emit("keyResult", map[string]any{"ok": true, "role": r.Role})
		a.emit("state", a.State())
	}()
}
