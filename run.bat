@echo off
setlocal
cd /d "%~dp0"
title IndusXCart - AI & Voice Powered E-Commerce

echo ===============================================================
echo     Compiling & Launching IndusXCart (Java 27 SE)
echo ===============================================================

if not exist bin mkdir bin

echo Compiling Java source files...
dir /s /b src\main\java\*.java > sources.txt
javac -d bin -encoding UTF-8 @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [OK] Compilation successful!
echo [INFO] Opening http://localhost:8080/ in your browser...
start http://localhost:8080/

echo [INFO] Starting IndusXCart server on port 8080...
echo [NOTE] Keep this window OPEN while using the website.
java -cp bin com.ecommerce.Main 8080

pause

