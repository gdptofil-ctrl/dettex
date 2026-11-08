// Shared validation for the Dettex cosmetics sync server (used by server.js and worker.js).
export const SLOTS = ['model', 'wings', 'hat', 'backpack', 'halo', 'cape', 'mace', 'sword'];
export const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;
export const MAX_LOOKUP = 100;
export const KEEP_MS = 30 * 24 * 3600 * 1000;   // forget users who have not announced for 30 days

/** Returns {uuid, name, token, c} or throws Error(message). */
export function parseAnnounce(body) {
  if (!body || typeof body !== 'object') throw new Error('bad json');
  const uuid = String(body.uuid || '').toLowerCase();
  if (!UUID_RE.test(uuid)) throw new Error('bad uuid');
  const name = String(body.name || '').slice(0, 32);
  if (!/^[A-Za-z0-9_.*\-]{1,32}$/.test(name)) throw new Error('bad name');
  const token = String(body.token || '');
  if (!/^[0-9a-f]{32,128}$/.test(token)) throw new Error('bad token');
  const c = {};
  const src = body.c && typeof body.c === 'object' ? body.c : {};
  for (const k of SLOTS) {
    const v = src[k];
    if (typeof v === 'string' && v.length > 0 && v.length <= 64 && !/[\u0000-\u001f]/.test(v)) c[k] = v;
  }
  return { uuid, name, token, c };
}

/** Returns a list of lowercase UUIDs or throws. */
export function parseLookup(body) {
  if (!body || !Array.isArray(body.uuids)) throw new Error('bad json');
  const out = [];
  for (const u of body.uuids.slice(0, MAX_LOOKUP)) {
    const s = String(u).toLowerCase();
    if (UUID_RE.test(s) && !out.includes(s)) out.push(s);
  }
  return out;
}

export async function sha256(text) {
  const data = new TextEncoder().encode(text);
  const hash = await crypto.subtle.digest('SHA-256', data);
  return [...new Uint8Array(hash)].map(b => b.toString(16).padStart(2, '0')).join('');
}
