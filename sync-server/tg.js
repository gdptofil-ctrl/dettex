// Telegram linking for DettexClient (shared by worker.js and server.js).
//
// 1. The client asks POST /v1/tg/new -> {key: "DTX-AB12-CD34", secret, bot}.
// 2. The player sends the key to the bot (or opens t.me/<bot>?start=<key>, which sends it).
// 3. Telegram delivers the message to POST /v1/tg/webhook; the key is confirmed and the bot replies.
// 4. The client polls POST /v1/tg/status {key, secret} and sees "confirmed".
// Afterwards POST /v1/tg/send {secret, text} delivers client notifications to that chat.
// The bot token lives only on the server (secret BOT_TOKEN), never in the client.
import { sha256 } from './common.js';
import { genKeys } from './license.js';

const KEY_TTL = 15 * 60;                 // a key is valid for 15 minutes
const LINK_TTL = 365 * 24 * 3600;        // a confirmed link lives a year (renewed on use)
const ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';   // no 0/O, 1/I
const KEY_RE = /^DTX-[A-Z2-9]{4}-[A-Z2-9]{4}$/;

function randomString(n, alphabet) {
  const bytes = new Uint8Array(n);
  crypto.getRandomValues(bytes);
  let s = '';
  for (const b of bytes) s += alphabet[b % alphabet.length];
  return s;
}

function randomHex(bytes) {
  const b = new Uint8Array(bytes);
  crypto.getRandomValues(b);
  return [...b].map(x => x.toString(16).padStart(2, '0')).join('');
}

/** "dtx ab12 cd34", "/start DTX-AB12-CD34", "ab12cd34" -> "DTX-AB12-CD34" or null. */
export function normalizeKey(text) {
  let t = String(text || '').trim().replace(/^\/start(@\w+)?/i, '').toUpperCase().replace(/[^A-Z0-9]/g, '');
  if (t.startsWith('DTX')) t = t.slice(3);
  if (t.length !== 8) return null;
  const key = 'DTX-' + t.slice(0, 4) + '-' + t.slice(4);
  return KEY_RE.test(key) ? key : null;
}

function esc(s) {
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

async function tgCall(env, method, payload) {
  if (!env.botToken) return { ok: false, description: 'BOT_TOKEN is not set on the server' };
  const r = await fetch(`${env.apiBase || 'https://api.telegram.org'}/bot${env.botToken}/${method}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  try { return await r.json(); } catch { return { ok: false, description: 'HTTP ' + r.status }; }
}

function reply(env, chat, html, extra = {}) {
  return tgCall(env, 'sendMessage', { chat_id: chat, text: html, parse_mode: 'HTML', disable_web_page_preview: true, ...extra });
}

// ------------------------------------------------------------------ client side

export async function newKey(store, env, body) {
  const name = String(body?.name || '').replace(/[^A-Za-z0-9_]/g, '').slice(0, 16) || 'player';
  let key;
  for (let i = 0; i < 5; i++) {
    key = 'DTX-' + randomString(4, ALPHABET) + '-' + randomString(4, ALPHABET);
    if (!(await store.get('tg:key:' + key))) break;
  }
  const secret = randomHex(24);
  await store.put('tg:key:' + key, { secretHash: await sha256(secret), name, status: 'pending', created: Date.now() }, KEY_TTL);
  return { key, secret, bot: env.botUsername || '', expiresIn: KEY_TTL };
}

export async function keyStatus(store, body) {
  const key = normalizeKey(body?.key);
  if (!key) throw new Error('bad key');
  const rec = await store.get('tg:key:' + key);
  if (!rec) return { status: 'expired' };
  if (rec.secretHash !== (await sha256(String(body?.secret || '')))) throw new Error('wrong secret');
  return rec.status === 'confirmed' ? { status: 'confirmed', username: rec.username || '', firstName: rec.firstName || '' } : { status: 'pending' };
}

export async function linkInfo(store, body) {
  const rec = await store.get('tg:chat:' + (await sha256(String(body?.secret || ''))));
  return rec ? { linked: true, username: rec.username || '', firstName: rec.firstName || '' } : { linked: false };
}

export async function sendToLinked(store, env, body) {
  const text = String(body?.text || '').slice(0, 3500);
  if (!text.trim()) throw new Error('empty text');
  const id = 'tg:chat:' + (await sha256(String(body?.secret || '')));
  const rec = await store.get(id);
  if (!rec) return { ok: false, linked: false };
  const r = await reply(env, rec.chat, '⚡ <b>DettexClient</b>\n' + esc(text));
  return { ok: !!r.ok, linked: true };
}

export async function unlink(store, body) {
  await store.del('tg:chat:' + (await sha256(String(body?.secret || ''))));
  return { ok: true };
}

// ------------------------------------------------------------------ bot side

const HELLO = '⚡ <b>DETTEX CLIENT</b>\n━━━━━━━━━━━━━━━━━━\n'
  + 'Привет! Здесь привязывается Telegram к клиенту.\n\n'
  + '1️⃣ Включи в клиенте модуль <b>Telegram API</b>\n'
  + '2️⃣ Скопируй ключ из чата игры (вида <code>DTX-XXXX-XXXX</code>)\n'
  + '3️⃣ Пришли его сюда — я подтвержу ✅';

/** Telegram update -> reply. Always resolves (Telegram must get 200). */
export async function webhook(store, env, update) {
  const msg = update?.message;
  if (!msg || !msg.chat || typeof msg.text !== 'string') return;
  const chat = msg.chat.id;
  const text = msg.text.trim();
  // owner-only: mint licence keys for the launcher
  if (/^\/genkey(@\w+)?\b/i.test(text)) {
    if (!env.ownerId || String(chat) !== String(env.ownerId)) {
      return reply(env, chat, '\u26d4 \u042d\u0442\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0434\u043b\u044f \u0432\u043b\u0430\u0434\u0435\u043b\u044c\u0446\u0430.');
    }
    const parts = text.split(/\s+/).slice(1);
    let lifetime = false, days = 30, count = 1;
    for (const p of parts) {
      const low = p.toLowerCase();
      if (low === 'life' || low === 'lifetime' || low === '\u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430') lifetime = true;
      else if (/^\d+d$/.test(low)) days = parseInt(low);
      else if (/^x?\d+$/.test(low)) count = parseInt(low.replace('x', ''));
    }
    count = Math.min(Math.max(count, 1), 20);
    const keys = await genKeys(store, { lifetime, days, count, role: 'Premium' });
    const plan = lifetime ? '\u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430' : days + ' \u0434\u043d.';
    return reply(env, chat, '\ud83d\udd11 <b>\u041a\u043b\u044e\u0447\u0438 (' + plan + '):</b>\n' + keys.map(k => '<code>' + k + '</code>').join('\n') + '\n\n\u0418\u0433\u0440\u043e\u043a \u0432\u0432\u043e\u0434\u0438\u0442 \u043a\u043b\u044e\u0447 \u0432 \u043b\u0430\u0443\u043d\u0447\u0435\u0440\u0435 \u043f\u043e\u0441\u043b\u0435 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438.');
  }
  const key = normalizeKey(text);
  if (!key) {
    if (/^\/start(@\w+)?$/i.test(text) || /^\/help/i.test(text)) return reply(env, chat, HELLO);
    return reply(env, chat, '🔑 Пришли ключ из клиента — он выглядит так: <code>DTX-XXXX-XXXX</code>.\n\nКлюч появляется в чате игры, когда включаешь модуль <b>Telegram API</b>.');
  }
  const rec = await store.get('tg:key:' + key);
  if (!rec) {
    return reply(env, chat, '❌ <b>Ключ не найден или устарел.</b>\n\nКлюч действует 15 минут. Выключи и снова включи модуль <b>Telegram API</b> в клиенте, чтобы получить новый.');
  }
  if (rec.status === 'confirmed') {
    return reply(env, chat, rec.chat === chat ? '✅ Этот ключ уже подтверждён. Всё ок!' : '⛔ Этот ключ уже использован другим аккаунтом Telegram.');
  }
  const from = msg.from || {};
  rec.status = 'confirmed';
  rec.chat = chat;
  rec.username = from.username || '';
  rec.firstName = from.first_name || '';
  rec.confirmed = Date.now();
  await store.put('tg:key:' + key, rec, KEY_TTL);
  await store.put('tg:chat:' + rec.secretHash, { chat, username: rec.username, firstName: rec.firstName, name: rec.name, linked: rec.confirmed }, LINK_TTL);
  return reply(env, chat, '✅ <b>Ключ подтверждён!</b>\n━━━━━━━━━━━━━━━━━━\n'
    + 'DettexClient (ник <b>' + esc(rec.name) + '</b>) привязан к этому Telegram.\n\n'
    + 'Возвращайся в игру — всё ок 👌');
}

/** Points the bot's webhook at this server. Called by deploy.ps1 with the admin secret. */
export async function setup(env, origin) {
  const me = await tgCall(env, 'getMe', {});
  if (!me.ok) return { ok: false, error: 'getMe: ' + me.description };
  const wh = await tgCall(env, 'setWebhook', {
    url: origin + '/v1/tg/webhook',
    secret_token: env.webhookSecret,
    allowed_updates: ['message'],
    drop_pending_updates: true,
  });
  await tgCall(env, 'setMyCommands', { commands: [{ command: 'start', description: 'Как привязать клиент' }] });
  return { ok: !!wh.ok, bot: me.result.username, webhook: wh.description || '' };
}

/** Routes /v1/tg/* . Returns {status, body} or null when the path is not ours. */
export async function route(path, method, headers, bodyText, store, env, origin) {
  if (!path.startsWith('/v1/tg/')) return null;
  if (method !== 'POST') return { status: 405, body: { error: 'POST only' } };
  if (path === '/v1/tg/webhook') {
    if (!env.webhookSecret || headers.get('x-telegram-bot-api-secret-token') !== env.webhookSecret) return { status: 401, body: { error: 'bad secret' } };
    try { await webhook(store, env, JSON.parse(bodyText)); } catch (e) { console.log('webhook error', e); }
    return { status: 200, body: { ok: true } };
  }
  if (path === '/v1/tg/setup') {
    if (!env.webhookSecret || headers.get('x-admin-secret') !== env.webhookSecret) return { status: 401, body: { error: 'bad secret' } };
    return { status: 200, body: await setup(env, origin) };
  }
  let body;
  try { body = JSON.parse(bodyText || '{}'); } catch { return { status: 400, body: { error: 'bad json' } }; }
  try {
    switch (path) {
      case '/v1/tg/new': return { status: 200, body: await newKey(store, env, body) };
      case '/v1/tg/status': return { status: 200, body: await keyStatus(store, body) };
      case '/v1/tg/me': return { status: 200, body: await linkInfo(store, body) };
      case '/v1/tg/send': return { status: 200, body: await sendToLinked(store, env, body) };
      case '/v1/tg/unlink': return { status: 200, body: await unlink(store, body) };
      default: return { status: 404, body: { error: 'not found' } };
    }
  } catch (e) {
    return { status: 400, body: { error: String(e.message || e) } };
  }
}
