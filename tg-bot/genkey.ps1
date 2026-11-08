# Generates Dettex licence keys signed with the Telegram bot token.
# The launcher verifies them offline; no server is needed.
#
#   .\genkey.ps1                          # one 30-day key
#   .\genkey.ps1 -Plan 7                  # one 7-day key
#   .\genkey.ps1 -Plan 1 -Count 5         # five 1-day keys
#   .\genkey.ps1 -Plan life               # a forever key
#
# The token is read from bot-token.txt next to this script (or -Token / BOT_TOKEN).
param(
    [string]$Plan = '30',
    [int]$Count = 1,
    [string]$Token = '',
    [string]$Uid = ''   # hex, for tests only
)
$ErrorActionPreference = 'Stop'
$alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
$line = [string]([char]0x2501) * 18

if (-not $Token) {
    if ($env:BOT_TOKEN) { $Token = $env:BOT_TOKEN }
    elseif (Test-Path (Join-Path $PSScriptRoot 'bot-token.txt')) { $Token = Get-Content (Join-Path $PSScriptRoot 'bot-token.txt') -Raw }
}
if (-not $Token) { throw 'Нет токена: положи его в bot-token.txt рядом со скриптом или задай -Token' }
$Token = $Token.Trim()

$sha = [Security.Cryptography.SHA256]::Create()
$secret = $sha.ComputeHash([Text.Encoding]::UTF8.GetBytes('DettexLicense/v1|' + $Token))

function ConvertTo-Base32([byte[]]$data) {
    $sb = New-Object System.Text.StringBuilder
    [int64]$buf = 0
    [int]$bits = 0
    foreach ($x in $data) {
        $buf = ($buf -shl 8) -bor $x
        $bits += 8
        while ($bits -ge 5) {
            $bits -= 5
            [void]$sb.Append($alphabet[[int](($buf -shr $bits) -band 31)])
        }
    }
    if ($bits -gt 0) { [void]$sb.Append($alphabet[[int](($buf -shl (5 - $bits)) -band 31)]) }
    return $sb.ToString()
}

function New-Key([int]$plan, [byte[]]$uid) {
    $payload = New-Object byte[] 5
    $payload[0] = [byte]$plan
    [Array]::Copy($uid, 0, $payload, 1, 4)
    $hmac = New-Object Security.Cryptography.HMACSHA256
    $hmac.Key = $secret
    $mac = $hmac.ComputeHash($payload)
    $raw = New-Object byte[] 10
    [Array]::Copy($payload, 0, $raw, 0, 5)
    [Array]::Copy($mac, 0, $raw, 5, 5)
    $s = ConvertTo-Base32 $raw
    [pscustomobject]@{
        Key  = 'DTX-' + $s.Substring(0, 4) + '-' + $s.Substring(4, 4) + '-' + $s.Substring(8, 4) + '-' + $s.Substring(12, 4)
        Uid  = ($uid | ForEach-Object { $_.ToString('X2') }) -join ''
        Plan = $plan
    }
}

$p = $Plan.Trim().ToLower()
switch -Regex ($p) {
    '^(1|1d|1д|1day)$'      { $planCode = 1;    $planName = '1 день' }
    '^(7|7d|7д|7days)$'     { $planCode = 7;    $planName = '7 дней' }
    '^(30|30d|30д|30days)$' { $planCode = 30;   $planName = '30 дней' }
    '^(life|forever|inf|навсегда)$' { $planCode = 255; $planName = 'навсегда' }
    default { throw "Неизвестный срок '$Plan'. Можно: 1, 7, 30, life." }
}
if ($Count -lt 1) { $Count = 1 }
if ($Count -gt 50) { $Count = 50 }

Write-Host "Ключи Dettex — $planName" -ForegroundColor Cyan
Write-Host $line -ForegroundColor DarkGray
for ($i = 0; $i -lt $Count; $i++) {
    $bytes = New-Object byte[] 4
    if ($Uid) {
        $hex = $Uid.Replace('-', '').Replace(' ', '')
        if ($hex.Length -ne 8) { throw 'Uid должен быть 8 hex-символов' }
        for ($j = 0; $j -lt 4; $j++) { $bytes[$j] = [Convert]::ToByte($hex.Substring($j * 2, 2), 16) }
    } else {
        [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    }
    $k = New-Key $planCode $bytes
    Write-Host ("{0}  UID {1}" -f $k.Key, $k.Uid)
}
