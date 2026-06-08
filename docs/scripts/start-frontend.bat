@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0..\..\frontend"
echo ============================================================
echo   Vue 3 + ECharts Dashboard
echo   Open http://localhost:5173 in browser
echo ============================================================
npm run dev
pause
