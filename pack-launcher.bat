@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0launcher\pack-launcher.ps1" %*
pause
