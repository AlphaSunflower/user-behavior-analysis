@echo off
chcp 65001 >nul
title 用户实时行为分析系统 - 全链路启动

echo ============================================================
echo   用户实时行为分析系统 - 全链路启动
echo   Group 18 - Guangdong Institute of Technology
echo ============================================================
echo.
echo 启动顺序:
echo   1. Spark Streaming (必须先启动)
echo   2. Kafka Producer
echo   3. SpringBoot API
echo   4. Vue 前端
echo.
echo 请确认:
echo   - Kafka 服务器 192.168.100.140:9092 已启动
echo   - MySQL localhost:3306 已启动
echo   - 各依赖已编译 (sbt compile / mvn compile / npm install)
echo.
pause

echo.
echo [1/4] 启动 Spark Streaming...
start "Spark Streaming" cmd /c "%~dp0scripts\start-streaming.bat"
echo 等待 Streaming 初始化 (30s)...
timeout /t 30 /nobreak >nul

echo [2/4] 启动 Kafka Producer...
start "Kafka Producer" cmd /c "%~dp0scripts\start-producer.bat"

echo [3/4] 启动 SpringBoot API...
start "SpringBoot API" cmd /c "%~dp0scripts\start-backend.bat"
echo 等待 SpringBoot 启动 (15s)...
timeout /t 15 /nobreak >nul

echo [4/4] 启动 Vue 前端...
start "Vue Frontend" cmd /c "%~dp0scripts\start-frontend.bat"
timeout /t 5 /nobreak >nul

echo.
echo ============================================================
echo   全部服务已启动！
echo   Streaming : 后台运行
echo   Producer  : 后台运行
echo   Backend   : http://localhost:8080
echo   Frontend  : http://localhost:5173
echo ============================================================
echo.
echo 浏览器打开 http://localhost:5173 查看可视化仪表盘
echo.
echo 按任意键关闭所有服务...
pause >nul

taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul
echo 所有服务已停止。
pause
