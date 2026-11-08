@echo off
setlocal EnableDelayedExpansion
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    if not exist "%JAVA_EXE%" set "JAVA_EXE=java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
