@echo off
REM ========================================
REM Let's Go Biking - Launcher (Batch)
REM ========================================

echo ========================================
echo    LET'S GO BIKING - LAUNCHER
echo ========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running as Administrator...
    echo.
    
    REM Run PowerShell script
    PowerShell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all-services.ps1"
) else (
    echo This script requires Administrator privileges!
    echo Right-click and select "Run as Administrator"
    echo.
    pause
)
