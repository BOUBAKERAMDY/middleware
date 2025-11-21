@echo off
echo ========================================
echo     Let's Go Biking - Starting Services
echo ========================================
echo.

echo Starting Proxy Cache Server...
start "Proxy Cache Server" cmd /k "cd ProxyCacheServer\bin\Debug && ProxyCacheServer.exe"

timeout /t 2 /nobreak > nul

echo Starting Routing Server...
start "Routing Server" cmd /k "cd RoutingServer\bin\Debug && RoutingServer.exe"

timeout /t 2 /nobreak > nul

echo.
echo ========================================
echo     Services Started Successfully!
echo ========================================
echo.
echo Proxy Cache Server: http://localhost:9000/ProxyService
echo Routing Server:     http://localhost:8090/MyService
echo.
echo Opening web interface...
start "" "FrontendClient\index.html"

echo.
echo Press any key to stop all services...
pause > nul

echo.
echo Stopping services...
taskkill /FI "WindowTitle eq Proxy Cache Server*" /T /F > nul 2>&1
taskkill /FI "WindowTitle eq Routing Server*" /T /F > nul 2>&1

echo Services stopped.
pause
