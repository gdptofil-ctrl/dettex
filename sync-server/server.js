// Dettex cosmetics sync server for any PC/VPS with Node.js 18+. No dependencies.
//   node server.js            (port 8787, or PORT=...)
// Data is kept in memory and saved to data.json every 30 seconds.
import http from 'node:http';
import fs from 'node:fs';
import { parseAnnounce, parseLookup, sha256, KEEP_MS } from './common.js';
import { route as tgRoute } from './tg.js';
import { route as licRoute } from './license.js';
import crypto from 'node:crypto';

// Telegram linking: BOT_TOKEN, WEBHOOK_SECRET (any random text) and BOT_USERNAME in the environment.
// The server must be reachable over HTTPS for the Telegram webhook.
const TG_DATA = process.env.TG_DATA_FILE || new URL('./tg-data.json', import.meta.url).pathname;
const tgMap = new Map();   // key -> {v, until}
try { for (const [k, v] of Object.entries(JSON.parse(fs.readFileSync(TG_DATA, 'utf8')))) tgMap.set(k, v); } catch { /* first start */ }
let tgDirty = false;
const tgStore = {
  async get(k) { const e = tgMap.get(k); if (!e) return null; if (e.until && e.until < Date.now()) { tgMap.delete(k); return null; } return e.v; },
  async put(k, v, ttl) { tgMap.set(k, { v, until: ttl ? Date.now() + ttl * 1000 : 0 }); tgDirty = true; },
  async del(k) { tgMap.delete(k); tgDirty = true; },
};
setInterval(() => { if (tgDirty) { tgDirty = false; fs.writeFile(TG_DATA, JSON.stringify(Object.fromEntries(tgMap)), () => {}); } }, 10_000);
const tgEnv = { botToken: process.env.BOT_TOKEN, webhookSecret: process.env.WEBHOOK_SECRET, botUsername: process.env.BOT_USERNAME, apiBase: process.env.TG_API, ownerId: process.env.OWNER_ID };

// Ed25519 signer for licences (private key in LICENSE_PRIVATE_KEY, PKCS8 DER base64). The public key
// is embedded in the client, so only this server can issue a valid licence.
let licenseSigner = null;
if (process.env.LICENSE_PRIVATE_KEY) {
  try {
    const key = crypto.createPrivateKey({ key: Buffer.from(process.env.LICENSE_PRIVATE_KEY, 'base64'), format: 'der', type: 'pkcs8' });
    licenseSigner = async (bytes) => crypto.sign(null, Buffer.from(bytes), key);
  } catch (e) { console.error('LICENSE_PRIVATE_KEY invalid:', e.message); }
}

const PORT = Number(process.env.PORT || 8787);
const DATA = process.env.DATA_FILE || new URL('./data.json', import.meta.url).pathname;
const users = new Map();   // uuid -> {name, tokenHash, c, seen}
let dirty = false;

try {
  for (const [k, v] of Object.entries(JSON.parse(fs.readFileSync(DATA, 'utf8')))) users.set(k, v);
  console.log(`loaded ${users.size} users`);
} catch { /* first start */ }

setInterval(() => {
  const now = Date.now();
  for (const [k, v] of users) if (now - v.seen > KEEP_MS) { users.delete(k); dirty = true; }
  if (!dirty) return;
  dirty = false;
  fs.writeFile(DATA, JSON.stringify(Object.fromEntries(users)), () => {});
}, 30_000);

// simple per-IP rate limit: 120 requests per minute
const hits = new Map();
setInterval(() => hits.clear(), 60_000);
function limited(ip) {
  const n = (hits.get(ip) || 0) + 1;
  hits.set(ip, n);
  return n > 120;
}

function send(res, code, obj) {
  res.writeHead(code, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
  res.end(JSON.stringify(obj));
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let size = 0; const chunks = [];
    req.on('data', c => { size += c.length; if (size > 16384) { reject(new Error('too big')); req.destroy(); } else chunks.push(c); });
    req.on('end', () => { try { resolve(JSON.parse(Buffer.concat(chunks).toString('utf8'))); } catch { reject(new Error('bad json')); } });
    req.on('error', reject);
  });
}

http.createServer(async (req, res) => {
  const ip = req.headers['x-forwarded-for']?.split(',')[0].trim() || req.socket.remoteAddress;
  if (req.method === 'GET' && (req.url === '/' || req.url === '/health')) return send(res, 200, { ok: true, users: users.size });
  // lyrics proxy for players whose network blocks lrclib.net: /v1/lrclib/get?... and /v1/lrclib/search?...
  const lyr = req.method === 'GET' && /^\/v1\/lrclib\/(get|search)(\?.*)?$/.exec(req.url);
  if (lyr) {
    if (limited(ip)) return send(res, 429, { error: 'slow down' });
    try {
      const r = await fetch('https://lrclib.net/api/' + lyr[1] + (lyr[2] || ''), { headers: { 'User-Agent': 'DettexSyncServer/1.0 (https://github.com/sagyndyk571-droid/dettex)' } });
      res.writeHead(r.status, { 'Content-Type': 'application/json' });
      return res.end(await r.text());
    } catch (e) {
      return send(res, 502, { error: 'lrclib unreachable' });
    }
  }
  if (req.url.startsWith('/v1/tg/')) {
    if (limited(ip)) return send(res, 429, { error: 'slow down' });
    const chunks = [];
    for await (const c of req) { chunks.push(c); if (chunks.reduce((n, x) => n + x.length, 0) > 65536) return send(res, 413, { error: 'too big' }); }
    const headers = { get: h => req.headers[h.toLowerCase()] };
    const proto = req.headers['x-forwarded-proto'] || 'http';
    const r = await tgRoute(req.url.split('?')[0], req.method, headers, Buffer.concat(chunks).toString('utf8'), tgStore, tgEnv, proto + '://' + req.headers.host);
    return send(res, r.status, r.body);
  }
  if (req.url.startsWith('/v1/license/') || req.url.startsWith('/v1/key/')) {
    if (limited(ip)) return send(res, 429, { error: 'slow down' });
    const chunks = [];
    for await (const c of req) { chunks.push(c); if (chunks.reduce((n, x) => n + x.length, 0) > 65536) return send(res, 413, { error: 'too big' }); }
    const headers = { get: h => req.headers[h.toLowerCase()] };
    const r = await licRoute(req.url.split('?')[0], req.method, headers, Buffer.concat(chunks).toString('utf8'), tgStore, tgEnv, licenseSigner);
    return send(res, r.status, r.body);
  }
  if (req.method !== 'POST') return send(res, 404, { error: 'not found' });
  if (limited(ip)) return send(res, 429, { error: 'slow down' });
  try {
    const body = await readBody(req);
    if (req.url === '/v1/announce') {
      const a = parseAnnounce(body);
      const tokenHash = await sha256(a.token);
      const old = users.get(a.uuid);
      if (old && old.tokenHash !== tokenHash) return send(res, 403, { error: 'uuid owned by another client' });
      users.set(a.uuid, { name: a.name, tokenHash, c: a.c, seen: Date.now() });
      dirty = true;
      return send(res, 200, { ok: true });
    }
    if (req.url === '/v1/lookup') {
      const out = {};
      for (const id of parseLookup(body)) {
        const u = users.get(id);
        if (u) out[id] = { name: u.name, c: u.c };
      }
      return send(res, 200, { users: out });
    }
    return send(res, 404, { error: 'not found' });
  } catch (e) {
    return send(res, 400, { error: String(e.message || e) });
  }
}).listen(PORT, () => console.log(`Dettex sync server on :${PORT}`));
