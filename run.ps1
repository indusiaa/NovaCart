# PowerShell script to compile and run IndusXCart
$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot

Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host "     Compiling & Launching IndusXCart (Java 27 SE)" -ForegroundColor Cyan
Write-Host "===============================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory "bin" | Out-Null
}

$files = (Get-ChildItem -Recurse -Filter *.java src/main/java | ForEach-Object { $_.FullName })
Write-Host "Compiling $($files.Count) Java source files..." -ForegroundColor Yellow
javac -d bin -encoding UTF-8 $files

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Compilation succeeded! Launching browser..." -ForegroundColor Green
    Start-Process "http://localhost:8080/"
    Write-Host "[INFO] Server running on http://localhost:8080/ (Keep this window open)" -ForegroundColor Green
    java -cp bin com.ecommerce.Main 8080
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}

