// Dettex cosmetics sync server as a Cloudflare Worker (free plan) with a KV namespace bound as USERS.
// KV free limits: 100k reads and 1k writes per day, so a record is rewritten only when the
// cosmetics change or once every 6 hours.
import { parseAnnounce, parseLookup, sha256, KEEP_MS } from './common.js';
import { route as tgRoute } from './tg.js';
import { route as licRoute } from './license.js';

const REFRESH_MS = 6 * 3600 * 1000;

// Ed25519 signer for licences on Cloudflare (LICENSE_PRIVATE_KEY secret, PKCS8 DER base64).
let licenseKeyPromise = null;
function licenseSignerFor(env) {
  if (!env.LICENSE_PRIVATE_KEY) return null;
  if (!licenseKeyPromise) {
    const der = Uint8Array.from(atob(env.LICENSE_PRIVATE_KEY), c => c.charCodeAt(0));
    licenseKeyPromise = crypto.subtle.importKey('pkcs8', der, { name: 'Ed25519' }, false, ['sign']);
  }
  return async (bytes) => new Uint8Array(await crypto.subtle.sign({ name: 'Ed25519' }, await licenseKeyPromise, bytes));
}

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), { status, headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' } });
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === 'GET' && (url.pathname === '/' || url.pathname === '/health')) return json({ ok: true });
    // lyrics proxy for players whose network blocks lrclib.net
    const lyr = request.method === 'GET' && /^\/v1\/lrclib\/(get|search)$/.exec(url.pathname);
    if (lyr) {
      const r = await fetch('https://lrclib.net/api/' + lyr[1] + url.search, {
        headers: { 'User-Agent': 'DettexSyncServer/1.0 (https://github.com/sagyndyk571-droid/dettex)' },
        cf: { cacheTtl: 86400, cacheEverything: true },
      });
      return new Response(await r.text(), { status: r.status, headers: { 'Content-Type': 'application/json' } });
    }
    // Telegram linking (bot token in the BOT_TOKEN secret, see deploy.ps1)
    if (url.pathname.startsWith('/v1/tg/')) {
      const text = request.method === 'POST' ? await request.text() : '';
      if (text.length > 65536) return json({ error: 'too big' }, 413);
      const store = {
        get: k => env.USERS.get(k, 'json'),
        put: (k, v, ttl) => env.USERS.put(k, JSON.stringify(v), ttl ? { expirationTtl: Math.max(60, ttl) } : undefined),
        del: k => env.USERS.delete(k),
      };
      const tgEnv = { botToken: env.BOT_TOKEN, webhookSecret: env.WEBHOOK_SECRET, botUsername: env.BOT_USERNAME, ownerId: env.OWNER_ID };
      const r = await tgRoute(url.pathname, request.method, request.headers, text, store, tgEnv, url.origin);
      return json(r.body, r.status);
    }
    // Licensing (private key in LICENSE_PRIVATE_KEY, see deploy.ps1)
    if (url.pathname.startsWith('/v1/license/') || url.pathname.startsWith('/v1/key/')) {
      const text = request.method === 'POST' ? await request.text() : '';
      if (text.length > 65536) return json({ error: 'too big' }, 413);
      const store = {
        get: k => env.USERS.get(k, 'json'),
        put: (k, v, ttl) => env.USERS.put(k, JSON.stringify(v), ttl ? { expirationTtl: Math.max(60, ttl) } : undefined),
        del: k => env.USERS.delete(k),
      };
      const licEnv = { webhookSecret: env.WEBHOOK_SECRET };
      const r = await licRoute(url.pathname, request.method, request.headers, text, store, licEnv, licenseSignerFor(env));
      return json(r.body, r.status);
    }
    if (request.method !== 'POST') return json({ error: 'not found' }, 404);
    let body;
    try {
      const text = await request.text();
      if (text.length > 16384) return json({ error: 'too big' }, 413);
      body = JSON.parse(text);
    } catch {
      return json({ error: 'bad json' }, 400);
    }
    try {
      if (url.pathname === '/v1/announce') {
        const a = parseAnnounce(body);
        const tokenHash = await sha256(a.token);
        const old = await env.USERS.get(a.uuid, 'json');
        if (old && old.tokenHash !== tokenHash) return json({ error: 'uuid owned by another client' }, 403);
        const now = Date.now();
        const same = old && old.name === a.name && JSON.stringify(old.c) === JSON.stringify(a.c);
        if (!same || now - old.seen > REFRESH_MS) {
          await env.USERS.put(a.uuid, JSON.stringify({ name: a.name, tokenHash, c: a.c, seen: now }),
            { expirationTtl: Math.floor(KEEP_MS / 1000) });
        }
        return json({ ok: true });
      }
      if (url.pathname === '/v1/lookup') {
        const ids = parseLookup(body);
        const found = await Promise.all(ids.map(id => env.USERS.get(id, 'json')));
        const out = {};
        ids.forEach((id, i) => { if (found[i]) out[id] = { name: found[i].name, c: found[i].c }; });
        return json({ users: out });
      }
      return json({ error: 'not found' }, 404);
    } catch (e) {
      return json({ error: String(e.message || e) }, 400);
    }
  },
};
