@echo off
chcp 65001 >nul
title Vue 3 Dashboard - 可视化仪表盘
echo ============================================================
echo   Vue 3 + ECharts 可视化仪表盘
echo   浏览器打开 http://localhost:5173
echo ============================================================
cd /d "%~dp0..\..\frontend"
npm run dev
pause
