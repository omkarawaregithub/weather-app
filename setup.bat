@echo off
REM Colors for output (Windows batch doesn't support colors, so using basic output)

echo ========== Jenkins Weather App - Setup Script ==========

REM Check Docker installation
echo Checking Docker installation...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not installed. Please install Docker Desktop first.
    pause
    exit /b 1
)
echo [OK] Docker is installed

REM Check Docker Compose installation
echo Checking Docker Compose installation...
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Compose is not installed. Please install Docker Desktop first.
    pause
    exit /b 1
)
echo [OK] Docker Compose is installed

REM Check .env file
echo Checking .env file...
if not exist ".env" (
    echo Creating .env file...
    (
        echo OPENWEATHERMAP_API_KEY=your_api_key_here
        echo DOCKER_REGISTRY=docker.io
        echo DOCKER_USERNAME=your_username
        echo BUILD_NUMBER=1
    ) > .env
    echo [OK] .env file created. Please update it with your credentials.
) else (
    echo [OK] .env file exists
)

REM Build Docker image
echo Building Docker image...
docker build -t jenkins-weather-app:latest -t jenkins-weather-app:1.0 .
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build Docker image
    pause
    exit /b 1
)
echo [OK] Docker image built successfully

REM Create networks if they don't exist
echo Creating Docker networks...
docker network create jenkins-network 2>nul || true
docker network create jenkins-network-dev 2>nul || true
docker network create jenkins-network-staging 2>nul || true
docker network create jenkins-network-prod 2>nul || true
echo [OK] Networks ready

REM Start services
echo Starting services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start services
    pause
    exit /b 1
)
echo [OK] Services started successfully

REM Wait for services to be ready
echo Waiting for services to be healthy...
timeout /t 15 /nobreak

REM Display service information
echo.
echo ========== Setup Complete ==========
echo.
echo Services running:
echo Application: http://localhost:8080
echo Jenkins: http://localhost:8081
echo.
echo Useful commands:
echo   View logs:     docker-compose logs -f
echo   Stop services: docker-compose down
echo   Restart app:   docker-compose restart jenkins-weather-app
echo   Rebuild:       docker build -t jenkins-weather-app:latest .
echo.
echo Next steps:
echo 1. Update credentials in Jenkins at http://localhost:8081
echo 2. Create a new Pipeline job
echo 3. Configure webhook in GitHub/GitLab
echo 4. Make a test commit to trigger the pipeline
echo.
pause
