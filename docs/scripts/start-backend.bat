@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0..\..\backend"
echo ============================================================
echo   SpringBoot REST API - Port 8080
echo   MySQL: localhost:3306 / user_behavior_db
echo ============================================================
mvn spring-boot:run
pause
