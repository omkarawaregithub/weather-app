# Quick Start - 5 Minute Setup

## Fastest Way to Get Started

### Option A: Automated (Recommended)

**Windows:**
```batch
setup.bat
```

**Linux/macOS:**
```bash
chmod +x setup.sh
./setup.sh
```

✅ This will:
- Check Docker installation
- Create .env file
- Build Docker image
- Start all services
- Display access URLs

---

## Option B: Manual Docker Compose (3 Steps)

### Step 1: Setup Environment
```bash
cat > .env << EOF
OPENWEATHERMAP_API_KEY=23d5db18d04313ba4da419d015913dd5
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=omkar
BUILD_NUMBER=1
EOF
```

### Step 2: Start Services
```bash
docker-compose up -d
```

### Step 3: Verify It's Running
```bash
# Wait 15 seconds
sleep 15

# Test application
curl "http://localhost:8080/weather?city=Pune"

# You should get weather data!
```

---

## Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Application** | http://localhost:8080 | No auth |
| **Jenkins** | http://localhost:8081 | admin / admin |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **Weather API** | http://localhost:8080/weather?city=Pune | - |

---

## Common Tasks

### View Application Logs
```bash
docker-compose logs -f jenkins-weather-app
```

### View Jenkins Logs
```bash
docker-compose logs -f jenkins
```

### Stop All Services
```bash
docker-compose down
```

### Rebuild Docker Image
```bash
docker build -t jenkins-weather-app:latest .
```

### Restart Application Only
```bash
docker-compose restart jenkins-weather-app
```

### Clean Everything (Includes Volumes)
```bash
docker-compose down -v
docker system prune -a
```

---

## Build Without Docker Compose

### Step 1: Build Docker Image
```bash
docker build -t jenkins-weather-app:latest .
```

### Step 2: Run Container
```bash
docker run -d \
  -p 8080:8080 \
  -e OPENWEATHERMAP_API_KEY=23d5db18d04313ba4da419d015913dd5 \
  --name jenkins-app \
  jenkins-weather-app:latest
```

### Step 3: Test
```bash
curl "http://localhost:8080/weather?city=Pune"
```

### Stop Container
```bash
docker stop jenkins-app
docker rm jenkins-app
```

---

## Build with Maven (Local)

### Step 1: Install Java 21 & Maven

**Windows (Chocolatey):**
```powershell
choco install jdk21 maven
```

**macOS (Homebrew):**
```bash
brew install openjdk@21 maven
```

**Linux (Ubuntu):**
```bash
sudo apt-get install openjdk-21-jdk maven
```

### Step 2: Build & Run
```bash
# Build
mvn clean package

# Run
java -Dopenweathermap.api.key=23d5db18d04313ba4da419d015913dd5 \
     -jar target/jenkins_project-0.0.1-SNAPSHOT.jar
```

### Step 3: Test
```bash
curl "http://localhost:8080/weather?city=Pune"
```

---

## Verify Everything Works

```bash
# 1. Check all containers are running
docker ps

# 2. Test application endpoint
curl -v "http://localhost:8080/weather?city=Pune"

# 3. Expected response:
# HTTP/1.1 200 OK
# {
#   "city": "Pune",
#   "temperature": 28.5,
#   "description": "Partly cloudy",
#   ...
# }

# 4. Check health
curl http://localhost:8080/actuator/health

# 5. Expected response:
# {"status":"UP"}
```

---

## Docker Compose Environments

### Development Only (Fastest)
```bash
docker-compose -f docker-compose.dev.yml up -d
```
- Only app container
- Debug logging
- No Jenkins
- Fastest startup

### Full Stack (Recommended for Testing)
```bash
docker-compose up -d
```
- Application
- Jenkins
- All networks and volumes

### Production-like (Resource Intensive)
```bash
docker-compose -f docker-compose.prod.yml up -d
```
- Optimized JVM settings
- Production configs
- Resource limits

---

## Troubleshooting Quick Fixes

### Port Already in Use
```bash
# Find what's using port 8080
netstat -tulpn | grep 8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows

# Stop the service using it
docker stop container_id

# Or change port in docker-compose
# Change 8080:8080 to 8090:8080
```

### Container Won't Start
```bash
# Check logs
docker logs jenkins-weather-app

# Rebuild without cache
docker build --no-cache -t jenkins-weather-app:latest .

# Try again
docker-compose restart
```

### Docker Not Running
```bash
# Windows: Start Docker Desktop
# macOS: Open Docker.app
# Linux: systemctl start docker
```

### API Key Error
```bash
# Check if API key is set
echo $OPENWEATHERMAP_API_KEY

# Update .env file with valid API key
nano .env

# Restart container
docker-compose restart jenkins-weather-app
```

---

## Next: Setup CI/CD Pipeline

After verifying the app runs:

1. Read: `PIPELINE_SETUP.md`
2. Follow Jenkins setup steps
3. Configure GitHub webhook
4. Create pipeline job
5. Test pipeline execution

---

## File Structure Quick Reference

```
jenkins_project/
├── Dockerfile              ← Docker image definition
├── docker-compose.yml      ← Full stack
├── docker-compose.dev.yml  ← Dev environment
├── Jenkinsfile             ← CI/CD pipeline
├── setup.sh / setup.bat    ← Automated setup
├── BUILD_AND_RUN.md        ← Detailed guide (this!)
├── pom.xml                 ← Maven configuration
└── src/
    ├── main/
    │   ├── java/           ← Source code
    │   └── resources/      ← Config files
    └── test/               ← Tests
```

---

## Environment Variables

Required in `.env`:
```env
OPENWEATHERMAP_API_KEY=your_api_key
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_username
BUILD_NUMBER=1
```

---

## Still Need Help?

📖 **Read these files:**
- `PIPELINE_SETUP.md` - Detailed setup guide
- `QUICK_REFERENCE.md` - Command reference
- `CI_CD_SUMMARY.md` - Architecture overview

🔗 **External Resources:**
- [Docker Docs](https://docs.docker.com/)
- [Docker Compose Docs](https://docs.docker.com/compose/)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)

---

**That's it! You're ready to go! 🚀**

Start with: `./setup.sh` (or `setup.bat` on Windows)
