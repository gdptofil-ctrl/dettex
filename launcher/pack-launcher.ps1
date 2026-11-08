# Builds DettexLauncher.exe with the Dettex client inside (no separate jar for players).
#   1) builds the client with Gradle (skip with -NoBuild)
#   2) takes the fresh build\libs\RecoinDLC-*.jar
#   3) DettexLauncher.exe = launcher\DettexLauncher-base.exe + client jar + trailer (see payload.go)
param([switch]$NoBuild)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if (-not $NoBuild) {
    Write-Host '1/3 Собираю клиент (gradlew build)...' -ForegroundColor Cyan
    & cmd /c "gradlew.bat build -x test" | Out-Host
    if ($LASTEXITCODE -ne 0) { Write-Host '   Сборка не удалась, беру последний собранный клиент.' -ForegroundColor Yellow }
} else {
    Write-Host '1/3 Сборка пропущена (-NoBuild)' -ForegroundColor Cyan
}

Write-Host '2/3 Ищу клиент...' -ForegroundColor Cyan
$jar = Get-ChildItem -Path (Join-Path $root 'build\libs') -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '-(sources|dev)\.jar$' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) { throw 'Нет build\libs\*.jar: собери клиент (gradlew build).' }
Write-Host "   $($jar.Name)  ($([math]::Round($jar.Length / 1MB, 1)) МБ, $($jar.LastWriteTime))"

Write-Host '3/3 Встраиваю клиент в лаунчер...' -ForegroundColor Cyan
$base = Join-Path $PSScriptRoot 'DettexLauncher-base.exe'
if (-not (Test-Path $base)) { throw "Нет $base" }
$b = [IO.File]::ReadAllBytes($base)
$magic = [Text.Encoding]::ASCII.GetBytes('DTXCLNT1')
$baseLen = $b.Length
if ($baseLen -ge 16 -and [Text.Encoding]::ASCII.GetString($b, $baseLen - 8, 8) -eq 'DTXCLNT1') {
    $size = [BitConverter]::ToUInt64($b, $baseLen - 16)
    $baseLen = $baseLen - 16 - [int64]$size   # base already carried a client: leave it out
}
$j = [IO.File]::ReadAllBytes($jar.FullName)
$out = Join-Path $root 'DettexLauncher.exe'
$fs = [IO.File]::Create($out)
try {
    $fs.Write($b, 0, $baseLen)
    $fs.Write($j, 0, $j.Length)
    $len = [BitConverter]::GetBytes([UInt64]$j.Length)
    $fs.Write($len, 0, 8)
    $fs.Write($magic, 0, 8)
} finally { $fs.Close() }
$desktop = [Environment]::GetFolderPath('Desktop')
if ($desktop -and (Test-Path $desktop)) {
    Copy-Item -Path $out -Destination (Join-Path $desktop 'DettexLauncher.exe') -Force
    Write-Host "   Копия на рабочем столе: $(Join-Path $desktop 'DettexLauncher.exe')"
}
Write-Host ''
Write-Host "Готово: $out ($([math]::Round((Get-Item $out).Length / 1MB, 1)) МБ). Клиент внутри, отдельный jar игрокам не нужен." -ForegroundColor Green
