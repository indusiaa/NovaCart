@echo off
setlocal
cd /d "C:\Users\Lenovo\Desktop\NovaCart"
title Deploy NovaCart to GitHub

echo ===============================================================
echo     Pushing NovaCart to GitHub (https://github.com/indusiaa/NovaCart)
echo ===============================================================

git status
git add .
git commit -m "Update NovaCart production build for Render" 2>nul
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===============================================================
    echo [SUCCESS] Code pushed to GitHub successfully!
    echo Now open Render.com and connect the NovaCart repository!
    echo ===============================================================
) else (
    echo.
    echo [NOTE] If repository does not exist yet:
    echo 1. Open: https://github.com/new
    echo 2. Repository name: NovaCart
    echo 3. Click 'Create repository'
    echo 4. Then double-click this script again!
)

pause
