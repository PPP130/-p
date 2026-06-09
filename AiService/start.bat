@echo off
chcp 65001 >nul
echo ========================================
echo        AI客服服务启动脚本
echo ========================================
echo.

cd /d C:\Users\17928\Desktop\AiService

echo [1/2] 激活虚拟环境...
call venv\Scripts\activate

echo [2/2] 启动服务...
echo.
echo 服务启动后请访问: http://localhost:8000/docs
echo 按 Ctrl+C 可停止服务
echo ========================================
echo.

python -m app.main

pause
