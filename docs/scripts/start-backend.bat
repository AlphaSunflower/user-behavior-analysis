@echo off
chcp 65001 >nul
title SpringBoot API - 数据服务
echo ============================================================
echo   SpringBoot REST API - 端口 8080
echo   MySQL: localhost:3306 / user_behavior_db
echo ============================================================
cd /d "%~dp0..\..\backend"
mvn spring-boot:run
pause
