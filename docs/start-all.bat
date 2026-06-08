@echo off
chcp 65001 >nul 2>&1

echo ============================================================
echo   User Behavior Analysis System - Full Stack Launch
echo   Group 18 - Guangdong Institute of Technology
echo ============================================================
echo.
echo Startup order:
echo   1. Spark Streaming (must start first)
echo   2. Kafka Producer
echo   3. SpringBoot API
echo   4. Vue Frontend
echo.
echo Prerequisites:
echo   - Kafka server 192.168.100.140:9092 is running
echo   - MySQL localhost:3306 is running
echo.
pause

echo.
echo [1/4] Starting Spark Streaming...
start "SparkStreaming" cmd /c "%~dp0scripts\start-streaming.bat"
echo Waiting 30s for Streaming to initialize...
timeout /t 30 /nobreak >nul

echo [2/4] Starting Kafka Producer...
start "KafkaProducer" cmd /c "%~dp0scripts\start-producer.bat"

echo [3/4] Starting SpringBoot API...
start "SpringBootAPI" cmd /c "%~dp0scripts\start-backend.bat"
echo Waiting 15s for SpringBoot to start...
timeout /t 15 /nobreak >nul

echo [4/4] Starting Vue Frontend...
start "VueFrontend" cmd /c "%~dp0scripts\start-frontend.bat"
timeout /t 5 /nobreak >nul

echo.
echo ============================================================
echo   All services started!
echo   Backend  : http://localhost:8080
echo   Frontend : http://localhost:5173
echo ============================================================
echo.
echo Open http://localhost:5173 in browser to view dashboard.
echo.
echo Press any key to stop all services...
pause >nul

taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul
echo All services stopped.
pause
