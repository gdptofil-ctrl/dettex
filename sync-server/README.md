# Dettex Sync Server

A small server that shares cosmetics between DettexClient users. Each client sends it the cosmetics it has picked and asks it about the players in the tab list. Players the server knows are Dettex users: their cosmetics are drawn and a Dettex badge appears next to their name.

All clients must use the **same** server address. Without a server, sync is off and each player sees only their own cosmetics.

## Option 1. Cloudflare Workers (free, no PC or VPS needed)

**Quickest way (Windows):** install Node.js 18+, then double-click `deploy.bat` in this folder. It logs you in to Cloudflare, creates the storage, deploys the server and writes its address into `Cosmetics.java` (`DEFAULT_SYNC_SERVER`). Rebuild the client after that.

Manual way:

1. Create a free account at https://dash.cloudflare.com and install Node.js 18+.
2. Run in the `sync-server` folder:
   ```
   npx wrangler login
   npx wrangler kv namespace create USERS
   ```
3. Paste the `id` printed by the second command into `wrangler.toml` in place of `PASTE_KV_NAMESPACE_ID_HERE`.
4. Deploy:
   ```
   npx wrangler deploy
   ```
   Wrangler prints an address like `https://dettex-sync.<your-name>.workers.dev`. That is your server.

The free KV plan allows 100,000 reads and 1,000 writes per day. That is enough for dozens of players: a record is only rewritten when someone changes their cosmetics, or once every 6 hours.

## Option 2. Your own PC or VPS

You need Node.js 18+ and no other dependencies:
```
cd sync-server
node server.js            # port 8787; another one: PORT=9000 node server.js
```
Data is saved to `data.json` next to the server. The server must be reachable from the internet: put a domain with HTTPS in front of it (nginx/Caddy), or use the `http://ip:8787` address directly.

You can check that it works by opening `https://<address>/health` in a browser. It should return `{"ok":true}`.

## Client setup

Open Cosmetics → **Sync** and fill in:
- **Sync Server**: the server address (the same one for everyone).
- **Share My Cosmetics**: others see your cosmetics.
- **Show Dettex Users**: you see other users' cosmetics.
- **Dettex Badge**: the logo next to Dettex users' names (above the head and in the tab list).

To avoid making every player type the address by hand, put it into `DEFAULT_SYNC_SERVER` in `Cosmetics.java` before building.

## Lyrics proxy

If lrclib.net is blocked in a player's network, the client automatically fetches Music HUD lyrics through the same server (`GET /v1/lrclib/get|search`). That works only when a Sync Server is configured.

## How it works

- `POST /v1/announce {uuid, name, token, c:{model,wings,hat,backpack,halo,cape,mace,sword}}` — publish your cosmetics.
- `POST /v1/lookup {uuids:[...]}` → `{users:{uuid:{name,c}}}` — up to 100 players at a time.
- On first launch the client creates a random key in `config/dettex/sync_token.txt`. The first key to register a UUID becomes its owner, and nobody else can change that player's cosmetics. If the key is lost, the record is cleared automatically after 30 days.
- The client polls the server in a background thread every few seconds, so the game does not lag. If the server is down, the client waits a minute and tries again.

## Telegram linking (@dettexclient_bot)

The **Telegram API** module links the client to a player's Telegram with a key:

1. When the module is turned on, the client asks the server for a key like `DTX-AB12-CD34` and shows it in the game chat. Clicking the key copies it, and the link opens the bot with the key already filled in.
2. The player sends the key to the bot, and the bot replies "✅ Key confirmed".
3. The client sees the confirmation and writes "Telegram linked to @name". The link is saved in `config/dettex/telegram.json`. After that, client notifications (AutoSell reports) arrive in that chat.

The bot token is stored only on the server and never in the client. `deploy.bat` asks for it at step 4 (get it from @BotFather), stores it as a Cloudflare secret and points the bot's webhook at the server. Cloudflare makes that call itself, so it works even where Telegram is blocked.

Endpoints: `POST /v1/tg/new`, `/v1/tg/status`, `/v1/tg/me`, `/v1/tg/send`, `/v1/tg/unlink`, `/v1/tg/webhook` (only Telegram, checked by secret), `/v1/tg/setup` (only deploy, checked by secret).

On your own server (`server.js`), set the variables `BOT_TOKEN`, `WEBHOOK_SECRET` (any random string) and `BOT_USERNAME`. Then call `/v1/tg/setup` with the `X-Admin-Secret: <WEBHOOK_SECRET>` header. The server must be reachable over HTTPS.

## Licensing (anti-crack)

The **Telegram API** / purchases flow can gate the client behind a server licence:

- The client sends its HWID + Minecraft account to `POST /v1/license/check`. If this account has a licence, the server returns a token signed with **Ed25519** and bound to that computer. The client holds only the **public key** (baked into the jar at `assets/recoin/license_pub.txt`), so it can verify a licence but cannot forge one — the private key never leaves the server.
- Without a valid licence, modules refuse to turn on. A cached licence keeps working offline for 5 days. A licence is bound to the first HWID that claims it (`seats`), so it cannot be shared to another computer.

`deploy.bat` sets this up (step "Защита клиента"): it makes the Ed25519 key pair, uploads the private key as the `LICENSE_PRIVATE_KEY` secret, and writes the public key into the client. Protection turns on after you rebuild the client (`pack-launcher.bat`). Until you run this, the client is unprotected, so development builds are not bricked.

Grant and manage licences (needs the `X-Admin-Secret: <WEBHOOK_SECRET>` header), tie these into your sales:

```
POST /v1/license/grant   {"uuid":"<player-uuid>","role":"Premium","days":30}
POST /v1/license/grant   {"uuid":"<player-uuid>","lifetime":true}
POST /v1/license/grant   {"uuid":"<player-uuid>","seats":2,"resetHwid":true}
POST /v1/license/revoke  {"uuid":"<player-uuid>"}
POST /v1/license/info    {"uuid":"<player-uuid>"}
```

The player's UUID is their Minecraft account UUID (from the launcher or namemc.com). On your own `server.js`, set `LICENSE_PRIVATE_KEY` and `WEBHOOK_SECRET` in the environment.


### Keys and the bot

Players activate a licence with a **key** instead of you calling the admin API for each one:

- You (the owner) mint keys in the bot: send `/genkey` (30 days), `/genkey life` (forever), `/genkey 7d x5` (five 7-day keys). Only your Telegram works — set `OWNER_ID` in `deploy.bat` (your numeric id from @userinfobot).
- The player pastes the key into the launcher after it downloads; the launcher calls `POST /v1/key/redeem {key, uuid, name}`, which grants the licence to that account. A key works once and is then bound to that account.
- `POST /v1/license/status {uuid}` is a public yes/no the launcher uses to decide whether to ask for a key.
