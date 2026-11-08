# One-click deploy of the Dettex sync server to Cloudflare Workers (free plan).
# Needs Node.js 18+ (https://nodejs.org). Run: right click -> "Run with PowerShell", or deploy.bat.
# It logs in to Cloudflare (a browser window opens), creates the KV storage, deploys the worker
# and writes the server address into Cosmetics.java so every build of the client uses it.
$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
    Write-Host 'Node.js is not installed. Install it from https://nodejs.org and run this script again.' -ForegroundColor Red
    exit 1
}

Write-Host '1/5 Logging in to Cloudflare (a browser window will open)...' -ForegroundColor Cyan
npx --yes wrangler login
if ($LASTEXITCODE -ne 0) { throw 'wrangler login failed' }

$toml = Get-Content -Raw -Path 'wrangler.toml'
if ($toml -match 'PASTE_KV_NAMESPACE_ID_HERE') {
    Write-Host '2/5 Creating KV storage USERS...' -ForegroundColor Cyan
    $out = (npx --yes wrangler kv namespace create USERS 2>&1 | Out-String)
    Write-Host $out
    $m = [regex]::Match($out, '[0-9a-f]{32}')
    if (-not $m.Success) {
        $list = (npx --yes wrangler kv namespace list 2>&1 | Out-String)
        $m = [regex]::Match($list, '"id"\s*:\s*"([0-9a-f]{32})"[^}]*"title"\s*:\s*"[^"]*USERS')
        if ($m.Success) { $id = $m.Groups[1].Value } else { throw 'Could not find the KV namespace id in the wrangler output above.' }
    } else {
        $id = $m.Value
    }
    $toml = $toml -replace 'PASTE_KV_NAMESPACE_ID_HERE', $id
    Set-Content -Path 'wrangler.toml' -Value $toml -NoNewline
    Write-Host "   KV id: $id"
} else {
    Write-Host '2/5 KV storage already set in wrangler.toml' -ForegroundColor Cyan
}

Write-Host '3/5 Deploying the worker...' -ForegroundColor Cyan
$deploy = (npx --yes wrangler deploy 2>&1 | Out-String)
Write-Host $deploy
$u = [regex]::Match($deploy, 'https://[A-Za-z0-9.-]+\.workers\.dev')
if (-not $u.Success) { throw 'Deploy finished, but the workers.dev address was not found in the output above.' }
$url = $u.Value

Write-Host '4/5 Telegram-бот (привязка клиента по ключу)...' -ForegroundColor Cyan
$whSecret = $null
$tgMarker = Join-Path $PSScriptRoot '.telegram-configured'
$askToken = $true
if (Test-Path $tgMarker) {
    $again = Read-Host '   Бот уже настроен. Настроить заново? (y/N)'
    $askToken = $again -match '^[yYдД]'
}
if ($askToken) {
    $botToken = (Read-Host '   Вставь токен бота @dettexclient_bot от @BotFather (Enter - пропустить)').Trim()
    if ($botToken) {
        $whSecret = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
        Write-Host '   Сохраняю токен в секретах Cloudflare (в клиент он не попадает)...'
        $botToken | npx --yes wrangler secret put BOT_TOKEN | Out-Host
        $whSecret | npx --yes wrangler secret put WEBHOOK_SECRET | Out-Host
        # Cloudflare itself calls Telegram, so this works even where Telegram is blocked
        $setup = $null
        for ($i = 0; $i -lt 6 -and -not ($setup -and $setup.ok); $i++) {
            Start-Sleep -Seconds 5
            try { $setup = Invoke-RestMethod -Method Post -Uri "$url/v1/tg/setup" -Headers @{ 'X-Admin-Secret' = $whSecret } -ContentType 'application/json' -Body '{}' -TimeoutSec 30 } catch { Write-Host "   ...жду сервер ($($_.Exception.Message))" }
        }
        if ($setup -and $setup.ok) {
            $setup.bot | npx --yes wrangler secret put BOT_USERNAME | Out-Host
            Set-Content -Path $tgMarker -Value $setup.bot
            Write-Host "   Бот @$($setup.bot) подключён к серверу." -ForegroundColor Green
            $ownerId = (Read-Host '   Твой Telegram ID (число) для команды /genkey (узнать: @userinfobot; Enter - пропустить)').Trim()
            if ($ownerId -match '^\d+$') { $ownerId | npx --yes wrangler secret put OWNER_ID | Out-Host; Write-Host '   OWNER_ID установлен: команда /genkey в боте доступна тебе.' -ForegroundColor Green }
        } else {
            Write-Host "   Не удалось подключить бота: $($setup | ConvertTo-Json -Compress). Проверь токен и запусти deploy.bat ещё раз." -ForegroundColor Yellow
        }
    } else {
        Write-Host '   Пропущено: привязка Telegram работать не будет, пока не укажешь токен.'
    }
}

Write-Host '5/6 Защита клиента (лицензия)...' -ForegroundColor Cyan
$pubFile = Join-Path $PSScriptRoot '..\src\main\resources\assets\recoin\license_pub.txt'
$licMarker = Join-Path $PSScriptRoot '.license-configured'
$doLicense = $true
if (Test-Path $licMarker) {
    $again = Read-Host '   Защита уже настроена. Сгенерировать новый ключ лицензий? Старые лицензии перестанут работать (y/N)'
    $doLicense = $again -match '^[yYдД]'
}
if ($doLicense) {
    if (-not $whSecret) { $whSecret = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N'); $whSecret | npx --yes wrangler secret put WEBHOOK_SECRET | Out-Host; Write-Host '   Создан админ-секрет (WEBHOOK_SECRET) для выдачи лицензий.' }
    Write-Host '   Генерирую пару ключей Ed25519 (приватный остаётся на сервере, публичный - в клиент)...'
    $keyJson = & node -e "const c=require('crypto');const k=c.generateKeyPairSync('ed25519');console.log(JSON.stringify({priv:k.privateKey.export({type:'pkcs8',format:'der'}).toString('base64'),pub:k.publicKey.export({type:'spki',format:'der'}).toString('base64')}))"
    if ($LASTEXITCODE -ne 0 -or -not $keyJson) { throw 'Не удалось сгенерировать ключ (нужен Node.js).' }
    $keys = $keyJson | ConvertFrom-Json
    $keys.priv | npx --yes wrangler secret put LICENSE_PRIVATE_KEY | Out-Host
    New-Item -ItemType Directory -Force -Path (Split-Path $pubFile) | Out-Null
    [System.IO.File]::WriteAllText($pubFile, $keys.pub, (New-Object System.Text.UTF8Encoding $false))
    Set-Content -Path $licMarker -Value (Get-Date -Format o)
    Write-Host "   Публичный ключ записан в клиент: $pubFile" -ForegroundColor Green
    Write-Host '   Защита включится после пересборки клиента (pack-launcher.bat).' -ForegroundColor Green
    Write-Host '   ВАЖНО: выдай лицензию себе и покупателям:' -ForegroundColor Yellow
    Write-Host "     одному аккаунту на 30 дней:  Invoke-RestMethod -Method Post -Uri '$url/v1/license/grant' -Headers @{'X-Admin-Secret'=$whSecret} -ContentType 'application/json' -Body '{\"uuid\":\"<uuid-игрока>\",\"role\":\"Premium\",\"days\":30}'" -ForegroundColor Yellow
    Write-Host "     навсегда:  ... -Body '{\"uuid\":\"<uuid>\",\"lifetime\":true}'" -ForegroundColor Yellow
    Write-Host '   UUID игрока - это UUID его аккаунта Minecraft (из лаунчера или namemc.com).' -ForegroundColor Yellow
} else {
    Write-Host '   Пропущено: ключ лицензий оставлен прежним.'
}

Write-Host '6/6 Writing the address into the client...' -ForegroundColor Cyan
$java = Join-Path $PSScriptRoot '..\ru\recoin\implement\features\modules\render\cosmetics\Cosmetics.java'
if (Test-Path $java) {
    $src = Get-Content -Raw -Path $java
    $src = [regex]::Replace($src, 'DEFAULT_SYNC_SERVER = "[^"]*"', "DEFAULT_SYNC_SERVER = `"$url`"")
    [System.IO.File]::WriteAllText((Resolve-Path $java), $src, (New-Object System.Text.UTF8Encoding $false))
    Write-Host "   Cosmetics.java -> DEFAULT_SYNC_SERVER = $url"
}
# the launcher reads the server address from here (no rebuild needed)
try {
    $dataDir = Join-Path $env:APPDATA '.dettex'
    New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $dataDir 'server.txt'), $url, (New-Object System.Text.UTF8Encoding $false))
    Write-Host "   Адрес сервера записан для лаунчера: $dataDir\server.txt"
} catch {
    Write-Host "   Не удалось записать server.txt: $_" -ForegroundColor Yellow
}
try {
    $health = Invoke-RestMethod -Uri "$url/health" -TimeoutSec 20
    Write-Host "   Health check: ok=$($health.ok)"
} catch {
    Write-Host "   Health check failed right after deploy (a new workers.dev address can take a minute): $_" -ForegroundColor Yellow
}
Write-Host ''
Write-Host "Done. Sync server: $url" -ForegroundColor Green
Write-Host 'Rebuild the client and give this build to your friends. In game you can also paste the address into Cosmetics -> Sync -> Sync Server.'
