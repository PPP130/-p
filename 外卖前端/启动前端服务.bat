@echo off
chcp 65001 >nul
echo ========================================
echo   苍穹外卖 - 前端服务启动脚本
echo ========================================
echo.

cd /d "%~dp0"
echo 正在启动开发服务器...
echo 启动后请访问: http://localhost:3000/
echo 按 Ctrl+C 可停止服务
echo.

npm run dev

pause
