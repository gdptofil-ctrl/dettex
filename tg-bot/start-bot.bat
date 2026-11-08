@echo off
chcp 65001 >nul
title Dettex Telegram Bot
cd /d "%~dp0"
set "JAVA=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
if not exist "%JAVA%" set "JAVA=java"
"%JAVA%" -version >nul 2>&1
if errorlevel 1 (
  echo Java 21 не найдена. Установи JDK 21: https://adoptium.net
  pause
  exit /b
)
echo Запускаю бота... Не закрывай это окно, пока бот нужен.
"%JAVA%" -Djava.net.useSystemProxies=true -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 DettexBot.java
echo.
echo Бот остановлен. Лог: bot.log
pause
