# Dettex Launcher

`DettexLauncher.exe` is the Dettex launcher for Windows 10/11. It is one file, and you do not need to install anything.

## How it works
1. **Sign in — licence or just a nickname.** You can log in through Microsoft (on Microsoft's own page, the launcher never sees your password), or enter any nickname to play **offline without a licence** on offline-mode servers. The login is saved in encrypted form (DPAPI), so only your Windows user can read it.
   For a nickname the launcher uses the standard offline UUID (UUIDv3 of `OfflinePlayer:<nick>`), so servers that run in offline mode recognise the same player.
2. **"PLAY" installs and updates everything** into `%APPDATA%\.dettex`:
   Minecraft 1.21.11, Java 21 from Mojang, Fabric, Dettex, Fabric API, Rinku and Baritone. If you turn the option on, it also installs an FPS pack: Lithium, FerriteCore, Entity Culling and ImmediatelyFast.
   Files download in parallel and their checksums are verified. On later launches it only downloads what changed. Once installed, the game also starts without internet.
3. **The game starts with tuned Java settings** (G1GC). You set the memory with a slider. By default the launcher closes so it does not use memory, and it stays only to report a crash.

## The client is built in
The Dettex client sits inside `DettexLauncher.exe` itself, so players get one file and no separate jar.
To embed a fresh client, run `pack-launcher.bat` in the project root. It:
1. builds the client (`gradlew build`; add `-NoBuild` to skip),
2. takes the fresh `build\libs\RecoinDLC-*.jar`,
3. builds `DettexLauncher.exe` = `launcher\DettexLauncher-base.exe` + client.

You do not need Go for this. On first launch, the launcher unpacks the client to `%APPDATA%\.dettex\cache\client`.
Fallback for development: `DettexClient.jar` next to the launcher, `mods\*.jar`, or `..\build\libs`.

## Network
It works without a VPN. If you use a VPN or a system proxy (Settings → Network → Proxy), it uses them automatically.

## Building the launcher (Go 1.24)
```
go install github.com/akavel/rsrc@latest
rsrc -manifest res/app.manifest -ico res/icon.ico -arch amd64 -o rsrc_windows_amd64.syso
GOOS=windows GOARCH=amd64 go build -trimpath -ldflags "-H windowsgui -s -w" -o DettexLauncher-base.exe .
go run ./tools/pack DettexLauncher-base.exe ..\build\libs\RecoinDLC-1.1.jar ..\DettexLauncher.exe
```
Logic tests (on any OS): `go test ./...`

## Project prompt journal
Everything the user asked for about Dettex/ReCoin is kept in `launcher/PROMPTS.md` (verbatim prompts plus the outcome), so the context survives between sessions. Append new prompts at the end.

## Build troubleshooting (Gradle)
`maven.fabricmc.net` and Maven Central are reachable on this machine only through the local proxy `127.0.0.1:10809`. If the build fails with `Plugin [id: 'fabric-loom', version: '1.17.12'] was not found`, the proxy/VPN is off — start it and rerun `gradlew build`. `settings.gradle` now checks the proxy first and prints a clear message instead of the misleading "plugin not found".


## Licence key
The launcher asks for a key right after it opens: without a valid key you cannot log in or start the game. The player gets a key in the Telegram bot **@dettexclient_bot**, pastes it into the launcher, and the launcher shows the plan, the expiry date and the rank.

Keys are minted by the bot (`/genkey`, `/genkey 7`, `/genkey 30`, `/genkey life`, `x5` for five keys), by the admin panel of `detex_bot`, or with `tg-bot/genkey.ps1`. A key is `DTX-XXXX-XXXX-XXXX-XXXX` or `DETEX-XXXX-XXXX-XXXX-XXXX` and carries:
- the plan: 1 day, 7 days, 30 days, 90 days or lifetime;
- the rank (Player, Media, Administrator or Admin) shown in the launcher next to the account name;
- a random UID (shown in the bot and in the launcher);
- an HMAC-SHA256 signature made with a secret derived from the bot token.

Keys minted before ranks existed have no rank in the payload and are shown as **Player**; they stay valid. In `detex_bot` the admin picks the duration first and then the rank, and the rank is stored in `detex.db` next to the key.

The launcher checks that signature offline, so no licence server is needed and the key works without internet. The duration starts at the first activation and the key is bound to the computer (computer name + system volume serial). If the bot token changes, rebuild the launcher and mint new keys: `licenseSecretHex` in `launcher/license_local.go` is the SHA-256 of `DettexLicense/v1|` + token.

Old unsigned DETEX keys (the 12-character `DETEX-XXXX-XXXX-XXXX` format that the previous bot stored in `detex.db`) are accepted only if they are in `legacyDETEXKeys` in `launcher/license_local.go`. New keys from `detex_bot` are signed and do not need that list.

A deployed licence server (`sync-server/`) is still supported as a fallback: the launcher reads its address from `%APPDATA%\.dettex\server.txt` or `server.txt` next to the exe.
