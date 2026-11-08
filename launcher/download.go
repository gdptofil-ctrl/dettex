package main

import (
	"context"
	"crypto/sha1"
	"crypto/sha512"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"hash"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"sync/atomic"
	"time"
)

// Task is one file to have on disk.
type Task struct {
	URL    string
	Path   string
	SHA1   string // checked when set
	SHA512 string // checked when set (Modrinth)
	Size   int64  // 0 = unknown
}

// rewriteURL lets tests point downloads at a local server.
var rewriteURL = func(u string) string { return u }

var httpClient = &http.Client{
	Timeout: 0, // per request contexts instead (big files)
	Transport: &http.Transport{
		Proxy:               systemProxy,
		MaxIdleConnsPerHost: 32,
		IdleConnTimeout:     60 * time.Second,
		TLSHandshakeTimeout: 20 * time.Second,
		ForceAttemptHTTP2:   true,
	},
}

const userAgent = "DettexLauncher/1.0 (+https://github.com/sagyndyk571-droid/dettex)"

func newRequest(ctx context.Context, method, url string, body io.Reader) (*http.Request, error) {
	req, err := http.NewRequestWithContext(ctx, method, rewriteURL(url), body)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", userAgent)
	return req, nil
}

// getJSON downloads JSON into v. When cache is set, a good answer is saved there and used when
// the network is down (so the game still starts offline once installed).
func getJSON(ctx context.Context, url, cache string, v any) error {
	ctx2, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()
	var data []byte
	err := func() error {
		req, err := newRequest(ctx2, "GET", url, nil)
		if err != nil {
			return err
		}
		resp, err := httpClient.Do(req)
		if err != nil {
			return err
		}
		defer resp.Body.Close()
		if resp.StatusCode != 200 {
			return fmt.Errorf("HTTP %d: %s", resp.StatusCode, url)
		}
		data, err = io.ReadAll(io.LimitReader(resp.Body, 64<<20))
		return err
	}()
	if err == nil {
		if jerr := json.Unmarshal(data, v); jerr != nil {
			return fmt.Errorf("bad JSON from %s: %w", url, jerr)
		}
		if cache != "" {
			_ = os.MkdirAll(filepath.Dir(cache), 0o755)
			_ = os.WriteFile(cache, data, 0o644)
		}
		return nil
	}
	if cache != "" {
		if old, rerr := os.ReadFile(cache); rerr == nil && json.Unmarshal(old, v) == nil {
			return nil // offline: last known copy
		}
	}
	return err
}

// fileOK: the file exists with the right size, and (when checkHash) the right hash.
func fileOK(t Task, checkHash bool) bool {
	st, err := os.Stat(t.Path)
	if err != nil || st.IsDir() {
		return false
	}
	if t.Size > 0 && st.Size() != t.Size {
		return false
	}
	if !checkHash || (t.SHA1 == "" && t.SHA512 == "") {
		return true
	}
	return hashFile(t.Path, t) == nil
}

func hashFile(path string, t Task) error {
	f, err := os.Open(path)
	if err != nil {
		return err
	}
	defer f.Close()
	var h hash.Hash
	want := ""
	if t.SHA512 != "" {
		h, want = sha512.New(), t.SHA512
	} else {
		h, want = sha1.New(), t.SHA1
	}
	if _, err := io.Copy(h, f); err != nil {
		return err
	}
	if got := hex.EncodeToString(h.Sum(nil)); !strings.EqualFold(got, want) {
		return fmt.Errorf("checksum mismatch for %s", filepath.Base(path))
	}
	return nil
}

// Progress of a batch.
type Progress struct {
	DoneBytes, TotalBytes int64
	DoneFiles, TotalFiles int
	Speed                 float64 // bytes per second
}

// downloadAll gets every missing / broken file with `workers` parallel connections.
// Files already on disk with the right size are skipped (hashes are checked right after download),
// which makes a second launch nearly instant.
func downloadAll(ctx context.Context, tasks []Task, workers int, verifyExisting bool, onProgress func(Progress)) error {
	return downloadAllTries(ctx, tasks, workers, verifyExisting, onProgress, 4)
}

// downloadAllTries is downloadAll with a custom number of attempts per file.
func downloadAllTries(ctx context.Context, tasks []Task, workers int, verifyExisting bool, onProgress func(Progress), tries int) error {
	if tries < 1 {
		tries = 1
	}
	var todo []Task
	var total int64
	seen := map[string]bool{}
	for _, t := range tasks {
		key := strings.ToLower(filepath.Clean(t.Path))
		if seen[key] {
			continue
		}
		seen[key] = true
		if fileOK(t, verifyExisting) {
			continue
		}
		todo = append(todo, t)
		total += t.Size
	}
	if len(todo) == 0 {
		if onProgress != nil {
			onProgress(Progress{TotalFiles: 0})
		}
		return nil
	}
	var doneBytes atomic.Int64
	var doneFiles atomic.Int32
	start := time.Now()
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()
	jobs := make(chan Task)
	var firstErr error
	var errOnce sync.Once
	var wg sync.WaitGroup
	stopTicker := make(chan struct{})
	if onProgress != nil {
		go func() {
			tk := time.NewTicker(120 * time.Millisecond)
			defer tk.Stop()
			for {
				select {
				case <-stopTicker:
					return
				case <-tk.C:
					d := doneBytes.Load()
					onProgress(Progress{DoneBytes: d, TotalBytes: total, DoneFiles: int(doneFiles.Load()), TotalFiles: len(todo), Speed: float64(d) / time.Since(start).Seconds()})
				}
			}
		}()
	}
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for t := range jobs {
				var err error
				for attempt := 0; attempt < tries; attempt++ {
					if attempt > 0 {
						select {
						case <-ctx.Done():
							return
						case <-time.After(time.Duration(attempt*attempt) * 700 * time.Millisecond):
						}
					}
					err = downloadOne(ctx, t, &doneBytes)
					if err == nil || ctx.Err() != nil {
						break
					}
				}
				if err != nil {
					errOnce.Do(func() { firstErr = err; cancel() })
					return
				}
				doneFiles.Add(1)
			}
		}()
	}
loop:
	for _, t := range todo {
		select {
		case jobs <- t:
		case <-ctx.Done():
			break loop
		}
	}
	close(jobs)
	wg.Wait()
	close(stopTicker)
	if firstErr != nil {
		return firstErr
	}
	if err := ctx.Err(); err != nil && !errors.Is(err, context.Canceled) {
		return err
	}
	if onProgress != nil {
		onProgress(Progress{DoneBytes: total, TotalBytes: total, DoneFiles: len(todo), TotalFiles: len(todo), Speed: float64(total) / time.Since(start).Seconds()})
	}
	return nil
}

func downloadOne(ctx context.Context, t Task, counter *atomic.Int64) error {
	if err := os.MkdirAll(filepath.Dir(t.Path), 0o755); err != nil {
		return err
	}
	ctx2, cancel := context.WithTimeout(ctx, 10*time.Minute)
	defer cancel()
	// Some hosts (Cloudflare edges, throttled maven mirrors) accept the request and then
	// trickle a few KB and stop. Abort after 20 s without data instead of waiting 10 min.
	stallCtx, stallCancel := context.WithCancel(ctx2)
	defer stallCancel()
	ctx2 = stallCtx
	stall := time.AfterFunc(20*time.Second, stallCancel)
	defer stall.Stop()
	req, err := newRequest(ctx2, "GET", t.URL, nil)
	if err != nil {
		return err
	}
	resp, err := httpClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode != 200 {
		return fmt.Errorf("HTTP %d: %s", resp.StatusCode, t.URL)
	}
	tmp := t.Path + ".part"
	f, err := os.Create(tmp)
	if err != nil {
		return err
	}
	var written int64
	buf := make([]byte, 64<<10)
	for {
		n, rerr := resp.Body.Read(buf)
		if n > 0 {
			stall.Reset(20 * time.Second)
			if _, werr := f.Write(buf[:n]); werr != nil {
				f.Close()
				os.Remove(tmp)
				return werr
			}
			written += int64(n)
			counter.Add(int64(n))
		}
		if rerr == io.EOF {
			break
		}
		if rerr != nil {
			f.Close()
			os.Remove(tmp)
			counter.Add(-written)
			return rerr
		}
	}
	if err := f.Close(); err != nil {
		return err
	}
	if t.SHA1 != "" || t.SHA512 != "" {
		if err := hashFile(tmp, t); err != nil {
			os.Remove(tmp)
			counter.Add(-written)
			return err
		}
	}
	if t.Size > 0 && written != t.Size {
		// size in the manifest was a hint; keep the counter honest for the bar
		counter.Add(t.Size - written)
	}
	_ = os.Remove(t.Path) // Windows cannot rename over an existing file
	return os.Rename(tmp, t.Path)
}
