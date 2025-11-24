# ========================================
# Let's Go Biking - Start All Services
# ========================================
# This script launches all required services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   LET'S GO BIKING - SERVICE LAUNCHER" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ========================================
# CONFIGURATION - UPDATE THESE PATHS!
# ========================================

# ActiveMQ Path
$ACTIVEMQ_HOME = "C:\Users\user\Documents\apache-activemq-5.18.3"
$ACTIVEMQ_BIN = "C:\Users\user\Documents\apache-activemq-5.18.3\bin\win64\activemq.bat"

# ProxyCache Server Path
$PROXY_EXE = "C:\Users\user\Desktop\projectMidd\LetsGoBiking\ProxyCacheServer\bin\Debug\ProxyCacheServer.exe"

# Routing Server Path
$ROUTING_EXE = "C:\Users\user\Desktop\projectMidd\LetsGoBiking\RoutingServer\bin\Debug\RoutingServer.exe"

# Frontend Path
$FRONTEND_HTML = "C:\Users\user\Desktop\projectMidd\LetsGoBiking\FrontendClient\index.html"

# ========================================
# HELPER FUNCTIONS
# ========================================

function Test-Administrator {
    $currentUser = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($currentUser)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Start-Service {
    param(
        [string]$Name,
        [string]$Path,
        [string]$Type = "exe"
    )
    
    Write-Host "[$Name] Starting..." -ForegroundColor Yellow
    
    if (-not (Test-Path $Path)) {
        Write-Host "[$Name] ERROR: File not found at $Path" -ForegroundColor Red
        return $false
    }
    
    try {
        if ($Type -eq "bat") {
            Start-Process cmd -ArgumentList "/c `"$Path`" start" -WindowStyle Normal
        } elseif ($Type -eq "exe") {
            Start-Process $Path -WindowStyle Normal
        } elseif ($Type -eq "html") {
            Start-Process $Path
        }
        
        Write-Host "[$Name] Started successfully!" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "[$Name] ERROR: $_" -ForegroundColor Red
        return $false
    }
}

function Wait-Seconds {
    param([int]$Seconds)
    Write-Host "Waiting $Seconds seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds $Seconds
}

# ========================================
# CHECK ADMINISTRATOR PRIVILEGES
# ========================================

if (-not (Test-Administrator)) {
    Write-Host "ERROR: This script requires Administrator privileges!" -ForegroundColor Red
    Write-Host "Right-click on PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit
}

Write-Host "✓ Running as Administrator" -ForegroundColor Green
Write-Host ""

# ========================================
# START SERVICES
# ========================================

Write-Host "Starting services in sequence..." -ForegroundColor Cyan
Write-Host ""

# 1. Start ActiveMQ
$success = Start-Service -Name "ActiveMQ" -Path $ACTIVEMQ_BIN -Type "bat"
if ($success) {
    Write-Host "Waiting for ActiveMQ to initialize (30 seconds)..." -ForegroundColor Gray
    Wait-Seconds 30
    
    # Check if ActiveMQ is running
    Write-Host "Checking ActiveMQ status..." -ForegroundColor Gray
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8161" -UseBasicParsing -TimeoutSec 5
        Write-Host "✓ ActiveMQ is running!" -ForegroundColor Green
    } catch {
        Write-Host "⚠ ActiveMQ may not be fully started yet" -ForegroundColor Yellow
    }
    Write-Host ""
} else {
    Write-Host "Failed to start ActiveMQ. Continuing anyway..." -ForegroundColor Yellow
    Write-Host ""
}

# 2. Start ProxyCache Server
$success = Start-Service -Name "ProxyCache Server" -Path $PROXY_EXE -Type "exe"
if ($success) {
    Wait-Seconds 3
} else {
    Write-Host "Failed to start ProxyCache Server. Continuing anyway..." -ForegroundColor Yellow
}
Write-Host ""

# 3. Start Routing Server
$success = Start-Service -Name "Routing Server" -Path $ROUTING_EXE -Type "exe"
if ($success) {
    Wait-Seconds 3
} else {
    Write-Host "Failed to start Routing Server. Continuing anyway..." -ForegroundColor Yellow
}
Write-Host ""

# 4. Start Frontend (Browser)
Write-Host "Opening Frontend in browser..." -ForegroundColor Yellow
$success = Start-Service -Name "Frontend" -Path $FRONTEND_HTML -Type "html"
if ($success) {
    Wait-Seconds 2
}
Write-Host ""

# ========================================
# SUMMARY
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   STARTUP COMPLETE!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Services Status:" -ForegroundColor White
Write-Host "  • ActiveMQ:        http://localhost:8161/admin" -ForegroundColor Gray
Write-Host "  • ProxyCache:      http://localhost:9000/ProxyService" -ForegroundColor Gray
Write-Host "  • Routing Server:  http://localhost:8090/MyService" -ForegroundColor Gray
Write-Host "  • Frontend:        Opened in browser" -ForegroundColor Gray
Write-Host ""

Write-Host "Login Credentials:" -ForegroundColor White
Write-Host "  • ActiveMQ Console: admin / admin" -ForegroundColor Gray
Write-Host ""

Write-Host "Quick Test:" -ForegroundColor White
Write-Host "  1. Open the frontend (should be in your browser)" -ForegroundColor Gray
Write-Host "  2. Enter route: 'Place Bellecour, Lyon' → 'Gare Part-Dieu, Lyon'" -ForegroundColor Gray
Write-Host "  3. Click 'Calculate Route'" -ForegroundColor Gray
Write-Host "  4. Click 'Force This Station Unavailable'" -ForegroundColor Gray
Write-Host "  5. Wait for alert popup" -ForegroundColor Gray
Write-Host "  6. Click 'Recalculate Route'" -ForegroundColor Gray
Write-Host ""

Write-Host "To stop all services:" -ForegroundColor Yellow
Write-Host "  Run: stop-all-services.ps1" -ForegroundColor Gray
Write-Host ""

Write-Host "Press Enter to keep windows open, or Ctrl+C to exit..." -ForegroundColor Cyan
Read-Host
