package main

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// Microsoft sign-in, the same way the official launcher does it:
// login.live.com (code) -> Xbox Live -> XSTS -> Minecraft services -> profile.
const (
	msClientID  = "00000000402b5328"
	msRedirect  = "https://login.live.com/oauth20_desktop.srf"
	msScope     = "service::user.auth.xboxlive.com::MBI_SSL"
	msAuthorize = "https://login.live.com/oauth20_authorize.srf"
	msToken     = "https://login.live.com/oauth20_token.srf"
	xblAuth     = "https://user.auth.xboxlive.com/user/authenticate"
	xstsAuth    = "https://xsts.auth.xboxlive.com/xsts/authorize"
	mcLogin     = "https://api.minecraftservices.com/authentication/login_with_xbox"
	mcProfile   = "https://api.minecraftservices.com/minecraft/profile"
)

// LoginURL is opened in the launcher window; Microsoft redirects to msRedirect?code=... at the end.
func LoginURL() string {
	q := url.Values{}
	q.Set("client_id", msClientID)
	q.Set("response_type", "code")
	q.Set("redirect_uri", msRedirect)
	q.Set("scope", msScope)
	q.Set("prompt", "select_account")
	return msAuthorize + "?" + q.Encode()
}

// CodeFromRedirect pulls the code (or the error) out of the final URL.
func CodeFromRedirect(u string) (string, error) {
	p, err := url.Parse(u)
	if err != nil {
		return "", err
	}
	q := p.Query()
	if c := q.Get("code"); c != "" {
		return c, nil
	}
	if e := q.Get("error"); e != "" {
		if e == "access_denied" {
			return "", errors.New("вход отменён")
		}
		return "", fmt.Errorf("Microsoft: %s %s", e, q.Get("error_description"))
	}
	return "", errors.New("Microsoft не вернул код входа")
}

// Session is a signed-in account: either a Microsoft account with a Minecraft
// licence, or an offline (no licence) account that only has a nickname.
type Session struct {
	Name         string    `json:"name"`
	UUID         string    `json:"uuid"`
	XUID         string    `json:"xuid"`
	MCToken      string    `json:"mcToken"`
	MCExpires    time.Time `json:"mcExpires"`
	RefreshToken string    `json:"refreshToken"`
	Offline      bool      `json:"offline,omitempty"`
}

func (s *Session) Account() Account {
	acc := Account{Name: s.Name, UUID: s.UUID, AccessToken: s.MCToken, XUID: s.XUID, Offline: s.Offline}
	// Offline-mode servers ignore the token, but the game still wants one.
	if s.Offline && acc.AccessToken == "" {
		acc.AccessToken = "0"
	}
	return acc
}

func postForm(ctx context.Context, u string, form url.Values, out any) error {
	req, err := newRequest(ctx, "POST", u, strings.NewReader(form.Encode()))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	return doJSON(req, out)
}

func postJSON(ctx context.Context, u string, body any, out any) error {
	b, _ := json.Marshal(body)
	req, err := newRequest(ctx, "POST", u, bytes.NewReader(b))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Accept", "application/json")
	return doJSON(req, out)
}

type httpError struct {
	Status int
	Body   string
}

func (e *httpError) Error() string { return fmt.Sprintf("HTTP %d %s", e.Status, e.Body) }

func doJSON(req *http.Request, out any) error {
	resp, err := httpClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	b, _ := io.ReadAll(io.LimitReader(resp.Body, 4<<20))
	if resp.StatusCode/100 != 2 {
		return &httpError{resp.StatusCode, strings.TrimSpace(string(b))}
	}
	if out == nil {
		return nil
	}
	return json.Unmarshal(b, out)
}

// SignIn finishes a login from the code (first time) or from a saved refresh token.
func SignIn(ctx context.Context, code, refresh string) (*Session, error) {
	ctx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()
	form := url.Values{}
	form.Set("client_id", msClientID)
	form.Set("redirect_uri", msRedirect)
	form.Set("scope", msScope)
	if code != "" {
		form.Set("grant_type", "authorization_code")
		form.Set("code", code)
	} else {
		form.Set("grant_type", "refresh_token")
		form.Set("refresh_token", refresh)
	}
	var ms struct {
		AccessToken  string `json:"access_token"`
		RefreshToken string `json:"refresh_token"`
	}
	if err := postForm(ctx, msToken, form, &ms); err != nil {
		return nil, fmt.Errorf("Microsoft: %w", err)
	}

	// Xbox Live: the ticket format differs between token kinds, so try the known ones
	var xbl struct {
		Token         string `json:"Token"`
		DisplayClaims struct {
			XUI []struct {
				UHS string `json:"uhs"`
			} `json:"xui"`
		} `json:"DisplayClaims"`
	}
	var xerr error
	for _, prefix := range []string{"d=", "t=", ""} {
		xerr = postJSON(ctx, xblAuth, map[string]any{
			"Properties":   map[string]any{"AuthMethod": "RPS", "SiteName": "user.auth.xboxlive.com", "RpsTicket": prefix + ms.AccessToken},
			"RelyingParty": "http://auth.xboxlive.com",
			"TokenType":    "JWT",
		}, &xbl)
		if xerr == nil && xbl.Token != "" {
			break
		}
	}
	if xerr != nil || xbl.Token == "" || len(xbl.DisplayClaims.XUI) == 0 {
		return nil, fmt.Errorf("Xbox Live: %v", xerr)
	}
	uhs := xbl.DisplayClaims.XUI[0].UHS

	var xsts struct {
		Token         string `json:"Token"`
		DisplayClaims struct {
			XUI []struct {
				XID string `json:"xid"`
			} `json:"xui"`
		} `json:"DisplayClaims"`
	}
	if err := postJSON(ctx, xstsAuth, map[string]any{
		"Properties":   map[string]any{"SandboxId": "RETAIL", "UserTokens": []string{xbl.Token}},
		"RelyingParty": "rp://api.minecraftservices.com/",
		"TokenType":    "JWT",
	}, &xsts); err != nil {
		var he *httpError
		if errors.As(err, &he) && he.Status == 401 {
			switch {
			case strings.Contains(he.Body, "2148916233"):
				return nil, errors.New("у этого аккаунта Microsoft нет профиля Xbox. Зайди один раз на minecraft.net и создай его")
			case strings.Contains(he.Body, "2148916235"):
				return nil, errors.New("Xbox Live недоступен в твоей стране")
			case strings.Contains(he.Body, "2148916236"), strings.Contains(he.Body, "2148916237"):
				return nil, errors.New("нужно подтвердить возраст на xbox.com")
			case strings.Contains(he.Body, "2148916238"):
				return nil, errors.New("детский аккаунт: взрослый должен добавить его в семью Microsoft")
			}
		}
		return nil, fmt.Errorf("XSTS: %w", err)
	}
	xuid := ""
	if len(xsts.DisplayClaims.XUI) > 0 {
		xuid = xsts.DisplayClaims.XUI[0].XID
	}

	var mc struct {
		AccessToken string `json:"access_token"`
		ExpiresIn   int    `json:"expires_in"`
	}
	if err := postJSON(ctx, mcLogin, map[string]any{"identityToken": "XBL3.0 x=" + uhs + ";" + xsts.Token}, &mc); err != nil {
		return nil, fmt.Errorf("Minecraft: %w", err)
	}

	req, _ := newRequest(ctx, "GET", mcProfile, nil)
	req.Header.Set("Authorization", "Bearer "+mc.AccessToken)
	var prof struct {
		ID   string `json:"id"`
		Name string `json:"name"`
	}
	if err := doJSON(req, &prof); err != nil {
		var he *httpError
		if errors.As(err, &he) && he.Status == 404 {
			return nil, errors.New("на этом аккаунте нет Minecraft: Java Edition")
		}
		return nil, fmt.Errorf("профиль: %w", err)
	}
	if ms.RefreshToken == "" {
		ms.RefreshToken = refresh
	}
	return &Session{
		Name: prof.Name, UUID: prof.ID, XUID: xuid,
		MCToken: mc.AccessToken, MCExpires: time.Now().Add(time.Duration(mc.ExpiresIn) * time.Second),
		RefreshToken: ms.RefreshToken,
	}, nil
}

// Fresh returns a session with a valid Minecraft token (refreshing it when it is old).
func (s *Session) Fresh(ctx context.Context) (*Session, error) {
	// Offline accounts have no token to refresh and never expire.
	if s.Offline {
		return s, nil
	}
	if s.MCToken != "" && time.Until(s.MCExpires) > 10*time.Minute {
		return s, nil
	}
	if s.RefreshToken == "" {
		return nil, errors.New("нужно войти заново")
	}
	return SignIn(ctx, "", s.RefreshToken)
}
