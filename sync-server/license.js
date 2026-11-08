// Licensing for DettexClient (shared by worker.js and server.js).
//
// The client asks POST /v1/license/check {hwid, uuid, name}. If this account has a licence, the
// server returns {token, sig}: token is base64url(JSON {hwid, uuid, name, role, expires, features,
// issued}) and sig is its Ed25519 signature. The client holds only the public key, so it can check
// the signature but cannot forge a licence. The licence is bound to the first HWID that claims it
// (up to `seats`), so it cannot be shared to another computer.
//
// Admin (needs the X-Admin-Secret header = WEBHOOK_SECRET), tie these into your sales / Telegram:
//   POST /v1/license/grant  {uuid, name?, role?, days?, lifetime?, features?, seats?}
//   POST /v1/license/revoke {uuid}
//   POST /v1/license/info   {uuid}
//
// The private key lives only on the server (LICENSE_PRIVATE_KEY secret, PKCS8 DER base64). The deploy
// script makes the key pair, uploads the private key and writes the public key into the client.

const DEFAULT_DAYS = 30;

function b64url(bytes) {
  let s = Buffer.from(bytes).toString('base64');
  return s.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function esc(s) {
  return String(s);
}

// signer: async (Uint8Array) => Uint8Array (Ed25519 raw signature). Supplied by the host.
async function makeToken(signer, claims) {
  const json = JSON.stringify(claims);
  const tokenBytes = new TextEncoder().encode(json);
  const sig = await signer(tokenBytes);
  return { token: b64url(tokenBytes), sig: Buffer.from(sig).toString('base64') };
}

function normName(s) {
  return String(s || '').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16);
}

async function check(store, signer, body) {
  const uuid = String(body?.uuid || '').toLowerCase();
  const hwid = String(body?.hwid || '').slice(0, 64);
  const name = normName(body?.name);
  if (!uuid || !hwid) return { licensed: false, reason: 'нет аккаунта или HWID' };
  const rec = await store.get('lic:' + uuid);
  if (!rec || rec.revoked) return { licensed: false, reason: 'нет лицензии на этом аккаунте' };
  if (rec.expires && rec.expires < Date.now()) return { licensed: false, reason: 'срок лицензии истёк' };

  // bind to this computer: first HWIDs fill the seats, others are refused
  const seats = rec.seats || 1;
  rec.hwids = Array.isArray(rec.hwids) ? rec.hwids : [];
  if (!rec.hwids.includes(hwid)) {
    if (rec.hwids.length >= seats) {
      return { licensed: false, reason: 'лицензия уже привязана к другому компьютеру' };
    }
    rec.hwids.push(hwid);
    rec.name = name || rec.name;
    await store.put('lic:' + uuid, rec);
  }

  const claims = {
    hwid, uuid, name: name || rec.name || '',
    role: rec.role || 'User',
    expires: rec.expires || 0,
    features: rec.features || ['all'],
    issued: Date.now(),
  };
  const signed = await makeToken(signer, claims);
  return { licensed: true, role: claims.role, expires: claims.expires, ...signed };
}

async function status(store, body) {
  const uuid = String(body?.uuid || '').toLowerCase();
  if (!uuid) return { licensed: false, reason: 'нет аккаунта' };
  const rec = await store.get('lic:' + uuid);
  const licensed = !!rec && !rec.revoked && (!rec.expires || rec.expires > Date.now());
  return { licensed, role: licensed ? rec.role : undefined, expires: licensed ? rec.expires : undefined };
}

async function grant(store, body) {
  const uuid = String(body?.uuid || '').toLowerCase();
  if (!uuid) return { ok: false, error: 'нужен uuid' };
  const now = Date.now();
  const existing = (await store.get('lic:' + uuid)) || {};
  const lifetime = !!body?.lifetime;
  const days = Number(body?.days) || DEFAULT_DAYS;
  const rec = {
    role: body?.role || existing.role || 'User',
    name: normName(body?.name) || existing.name || '',
    features: Array.isArray(body?.features) ? body.features : existing.features || ['all'],
    seats: Number(body?.seats) || existing.seats || 1,
    hwids: body?.resetHwid ? [] : existing.hwids || [],
    expires: lifetime ? 0 : now + days * 24 * 3600 * 1000,
    revoked: false,
    granted: existing.granted || now,
    updated: now,
  };
  await store.put('lic:' + uuid, rec);
  return { ok: true, uuid, role: rec.role, expires: rec.expires, seats: rec.seats };
}

async function revoke(store, body) {
  const uuid = String(body?.uuid || '').toLowerCase();
  if (!uuid) return { ok: false, error: 'нужен uuid' };
  const rec = (await store.get('lic:' + uuid)) || null;
  if (!rec) return { ok: false, error: 'лицензия не найдена' };
  rec.revoked = true;
  rec.updated = Date.now();
  await store.put('lic:' + uuid, rec);
  return { ok: true };
}

async function info(store, body) {
  const uuid = String(body?.uuid || '').toLowerCase();
  const rec = await store.get('lic:' + uuid);
  return rec ? { ok: true, license: rec } : { ok: false, error: 'лицензия не найдена' };
}

const KEY_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

function randKeyPart(n) {
  let out = '';
  const b = new Uint8Array(n);
  (globalThis.crypto || require('node:crypto').webcrypto).getRandomValues(b);
  for (const x of b) out += KEY_ALPHABET[x % KEY_ALPHABET.length];
  return out;
}

/** A licence key looks like DTX-XXXX-XXXX-XXXX. */
function makeKeyString() {
  return 'DTX-' + randKeyPart(4) + '-' + randKeyPart(4) + '-' + randKeyPart(4);
}

export function normalizeLicenseKey(raw) {
  let t = String(raw || '').toUpperCase().replace(/[^A-Z0-9]/g, '');
  if (t.startsWith('DTX')) t = t.slice(3);
  if (t.length !== 12) return null;
  return 'DTX-' + t.slice(0, 4) + '-' + t.slice(4, 8) + '-' + t.slice(8, 12);
}

/** Creates `count` keys (admin / owner bot). Each carries the plan a redeem will grant. */
export async function genKeys(store, opts = {}) {
  const count = Math.min(Math.max(Number(opts.count) || 1, 1), 50);
  const plan = {
    role: opts.role || 'Premium',
    lifetime: !!opts.lifetime,
    days: Number(opts.days) || DEFAULT_DAYS,
    features: Array.isArray(opts.features) ? opts.features : ['all'],
    seats: Number(opts.seats) || 1,
  };
  const out = [];
  for (let i = 0; i < count; i++) {
    let key;
    for (let tries = 0; tries < 6; tries++) {
      key = makeKeyString();
      if (!(await store.get('key:' + key))) break;
    }
    await store.put('key:' + key, { plan, used: false, created: Date.now() });
    out.push(key);
  }
  return out;
}

/** Redeems a key for an account: grants the licence to that uuid, marks the key used. */
export async function redeemKey(store, body) {
  const key = normalizeLicenseKey(body?.key);
  const uuid = String(body?.uuid || '').toLowerCase();
  const name = normName(body?.name);
  if (!key) return { ok: false, reason: 'неверный формат ключа' };
  if (!uuid) return { ok: false, reason: 'нет аккаунта' };
  const rec = await store.get('key:' + key);
  if (!rec) return { ok: false, reason: 'ключ не найден' };
  if (rec.used) {
    return rec.usedBy === uuid
      ? { ok: true, already: true, role: rec.plan.role }
      : { ok: false, reason: 'ключ уже использован другим аккаунтом' };
  }
  const g = await grant(store, {
    uuid, name,
    role: rec.plan.role,
    lifetime: rec.plan.lifetime,
    days: rec.plan.days,
    features: rec.plan.features,
    seats: rec.plan.seats,
    resetHwid: true,
  });
  rec.used = true;
  rec.usedBy = uuid;
  rec.usedAt = Date.now();
  await store.put('key:' + key, rec);
  return { ok: true, role: g.role, expires: g.expires };
}

/** Routes /v1/license/* . Returns {status, body} or null when the path is not ours. */
export async function route(path, method, headers, bodyText, store, env, signer) {
  if (!path.startsWith('/v1/license/') && !path.startsWith('/v1/key/')) return null;
  if (method !== 'POST') return { status: 405, body: { error: 'POST only' } };
  let body;
  try {
    body = JSON.parse(bodyText || '{}');
  } catch {
    return { status: 400, body: { error: 'bad json' } };
  }
  if (path === '/v1/license/status') {
    return { status: 200, body: await status(store, body) };
  }
  if (path === '/v1/key/redeem') {
    try {
      return { status: 200, body: await redeemKey(store, body) };
    } catch (e) {
      return { status: 500, body: { ok: false, reason: 'ошибка сервера: ' + (e.message || e) } };
    }
  }
  if (path === '/v1/license/check') {
    if (!signer) return { status: 200, body: { licensed: false, reason: 'на сервере не настроен ключ лицензий' } };
    try {
      return { status: 200, body: await check(store, signer, body) };
    } catch (e) {
      return { status: 500, body: { licensed: false, reason: 'ошибка сервера: ' + (e.message || e) } };
    }
  }
  // admin
  const admin = env.webhookSecret && headers.get('x-admin-secret') === env.webhookSecret;
  if (!admin) return { status: 401, body: { error: 'нужен X-Admin-Secret' } };
  try {
    switch (path) {
      case '/v1/key/gen': return { status: 200, body: { ok: true, keys: await genKeys(store, body) } };
      case '/v1/license/grant': return { status: 200, body: await grant(store, body) };
      case '/v1/license/revoke': return { status: 200, body: await revoke(store, body) };
      case '/v1/license/info': return { status: 200, body: await info(store, body) };
      default: return { status: 404, body: { error: 'not found' } };
    }
  } catch (e) {
    return { status: 400, body: { error: String(e.message || e) } };
  }
}
