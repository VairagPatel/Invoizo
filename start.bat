@echo off
REM Quick Start Script for Invoizo (Windows)

echo.
echo ========================================
echo   Invoizo - Docker Quick Start
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo ✅ Docker is running
echo.

REM Check if .env file exists
if not exist .env (
    echo ⚠️  .env file not found
    echo Creating .env from .env.example...
    copy .env.example .env
    echo.
    echo ⚠️  Please edit .env file with your configuration
    echo Then run this script again.
    pause
    exit /b 1
)

echo ✅ Environment file found
echo.

REM Start services
echo 🚀 Starting all services...
echo.
docker-compose up -d

if errorlevel 1 (
    echo.
    echo ❌ Failed to start services
    echo Check the error messages above
    pause
    exit /b 1
)

echo.
echo ========================================
echo   ✅ Services Started Successfully!
echo ========================================
echo.
echo 🌐 Frontend:  http://localhost:5173
echo 🔧 Backend:   http://localhost:8080
echo 💾 MongoDB:   localhost:27017
echo.
echo 📝 View logs:     docker-compose logs -f
echo 🛑 Stop services: docker-compose down
echo.
echo Opening frontend in browser...
timeout /t 3 /nobreak >nul
start http://localhost:5173
echo.
echo Press any key to view logs...
pause >nul
docker-compose logs -f
