# Step-by-Step Build & Run Guide (Visual)

## 🎯 Choose Your Path

```
┌─────────────────────────────────────────────────────────┐
│         How do you want to run the project?             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  A) 🚀 FASTEST: Use Automated Setup                    │
│     → Run: setup.sh (Linux/Mac) or setup.bat (Windows) │
│     → Time: ~2 minutes                                 │
│     → Includes: Docker, Jenkins, All services          │
│                                                         │
│  B) ⚡ QUICK: Use Docker Compose (Manual)             │
│     → Run 3 commands                                   │
│     → Time: ~3 minutes                                 │
│     → Best for testing                                 │
│                                                         │
│  C) 🐳 INTERMEDIATE: Build Docker Image Manually       │
│     → Build image + Run container                      │
│     → Time: ~5 minutes                                 │
│     → Good for learning                                │
│                                                         │
│  D) ☕ ADVANCED: Build with Maven (Local)             │
│     → Requires: Java 21 + Maven                        │
│     → Time: ~10 minutes                                │
│     → For development                                  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Path A: 🚀 FASTEST - Automated Setup (RECOMMENDED)

### Requirements
✅ Docker installed
✅ Docker Compose installed
✅ Git installed
✅ ~5 GB free disk space

### Steps

```
STEP 1: Clone Repository
┌────────────────────────────────────────────────────┐
│ $ git clone https://github.com/your-username/     │
│   jenkins_project.git                              │
│ $ cd jenkins_project                               │
└────────────────────────────────────────────────────┘
           ↓
STEP 2: Run Setup Script
┌────────────────────────────────────────────────────┐
│ Windows:                                           │
│ $ setup.bat                                        │
│                                                    │
│ Linux/macOS:                                       │
│ $ chmod +x setup.sh                                │
│ $ ./setup.sh                                       │
└────────────────────────────────────────────────────┘
           ↓
STEP 3: Wait for Services to Start
┌────────────────────────────────────────────────────┐
│ ✓ Docker image built                              │
│ ✓ Networks created                                │
│ ✓ Services started                                │
│ ✓ Health checks passed                            │
│ (Takes ~30 seconds)                               │
└────────────────────────────────────────────────────┘
           ↓
STEP 4: Access Services
┌────────────────────────────────────────────────────┐
│ 🌐 Application:                                    │
│    http://localhost:8080                           │
│                                                    │
│ 🔧 Jenkins:                                        │
│    http://localhost:8081                           │
│                                                    │
│ ✅ Test endpoint:                                  │
│    curl http://localhost:8080/weather?city=Pune   │
└────────────────────────────────────────────────────┘
           ↓
         ✅ DONE!
```

---

## Path B: ⚡ QUICK - Docker Compose Manual

### Step-by-Step

**STEP 1: Navigate to Project**
```bash
cd jenkins_project
```

**STEP 2: Create Environment File**
```bash
# Create .env file with:
cat > .env << EOF
OPENWEATHERMAP_API_KEY=23d5db18d04313ba4da419d015913dd5
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=omkar
BUILD_NUMBER=1
EOF
```

**STEP 3: Start Services**
```bash
docker-compose up -d
```

**STEP 4: Wait & Verify**
```bash
# Wait 15 seconds
sleep 15

# Test application
curl "http://localhost:8080/weather?city=Pune"

# Expected output:
# {
#   "city": "Pune",
#   "temperature": 28.5,
#   ...
# }
```

### Access Points

| What | URL | Purpose |
|------|-----|---------|
| App | http://localhost:8080 | Weather API |
| Jenkins | http://localhost:8081 | CI/CD Server |
| Health | http://localhost:8080/actuator/health | Health Check |

### Useful Commands After Setup

```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs -f jenkins-weather-app

# Stop everything
docker-compose down

# Restart app only
docker-compose restart jenkins-weather-app

# View specific logs
docker-compose logs jenkins
```

---

## Path C: 🐳 INTERMEDIATE - Docker Manual Build

### Step-by-Step

**STEP 1: Build Docker Image**
```bash
cd jenkins_project
docker build -t jenkins-weather-app:latest .
```

**Progress:**
```
Step 1/9 : FROM maven:3.9-eclipse-temurin-21 AS builder
Step 2/9 : WORKDIR /app
Step 3/9 : COPY pom.xml .
Step 4/9 : RUN mvn dependency:go-offline -B
         ... (downloading dependencies) ...
Step 5/9 : COPY src ./src
Step 6/9 : RUN mvn clean package -DskipTests -B
         ... (building) ...
Step 7/9 : FROM eclipse-temurin:21-jre-jammy
Step 8/9 : COPY --from=builder /app/target/jenkins_project-...
Step 9/9 : ENTRYPOINT ["java", "-jar", "app.jar"]

Successfully built abc123def456
Successfully tagged jenkins-weather-app:latest
```

**STEP 2: Run Container**
```bash
docker run -d \
  -p 8080:8080 \
  -e OPENWEATHERMAP_API_KEY=23d5db18d04313ba4da419d015913dd5 \
  --name weather-app \
  jenkins-weather-app:latest
```

**STEP 3: Verify Running**
```bash
# Check if running
docker ps

# Expected output:
# CONTAINER ID  IMAGE                     STATUS
# abc123def     jenkins-weather-app:...   Up 10 seconds

# Wait a moment then test
sleep 10
curl "http://localhost:8080/weather?city=Pune"
```

**STEP 4: View Logs**
```bash
docker logs -f weather-app

# Expected output:
# Started JenkinsProjectApplication in X seconds
# Server running at http://localhost:8080
```

**STEP 5: Stop Container**
```bash
docker stop weather-app
docker rm weather-app
```

---

## Path D: ☕ ADVANCED - Maven Local Build

### Prerequisites

**Windows:**
```powershell
# Install Java 21
choco install jdk21

# Install Maven
choco install maven

# Verify
java -version
mvn --version
```

**macOS:**
```bash
brew install openjdk@21 maven
java -version
mvn --version
```

**Linux:**
```bash
sudo apt-get install openjdk-21-jdk maven
java -version
mvn --version
```

### Build Steps

**STEP 1: Build Project**
```bash
cd jenkins_project
mvn clean package
```

**Progress:**
```
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------< com.jenkins_project:jenkins_project >----------
[INFO] Building jenkins_project 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:... clean (default-clean) @ jenkins_project ---
[INFO] Deleting /path/to/jenkins_project/target
[INFO] 
[INFO] --- maven-resources-plugin:... resources (default-resources) @ ...
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- maven-compiler-plugin:... compile (default-compile) @ ...
[INFO] Compiling 4 source files to /path/to/jenkins_project/target/classes
[INFO] 
[INFO] --- maven-jar-plugin:... jar (default-jar) @ jenkins_project ---
[INFO] Building jar: /path/to/jenkins_project/target/jenkins_project-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] BUILD SUCCESS
[INFO] Total time: 35 seconds
```

**STEP 2: Run Tests (Optional)**
```bash
mvn test

# Expected output:
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

**STEP 3: Run Application**
```bash
java -Dopenweathermap.api.key=23d5db18d04313ba4da419d015913dd5 \
     -jar target/jenkins_project-0.0.1-SNAPSHOT.jar
```

**Expected output:**
```
Started JenkinsProjectApplication in 2.5 seconds
Server running at http://localhost:8080
```

**STEP 4: Test (In New Terminal)**
```bash
curl "http://localhost:8080/weather?city=Pune"
```

**STEP 5: Stop Application**
```
Press CTRL+C in the terminal running the app
```

---

## 🧪 Testing Each Setup

### Common Tests

**Test 1: Health Endpoint**
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

**Test 2: Weather Endpoint**
```bash
curl "http://localhost:8080/weather?city=Pune"
# Should return weather data
```

**Test 3: Invalid City**
```bash
curl "http://localhost:8080/weather?city=InvalidCity123"
# Should return 404 or error message
```

**Test 4: No City Parameter**
```bash
curl "http://localhost:8080/weather"
# Should return default city (Pune)
```

---

## 📊 Comparison Table

| Aspect | Path A | Path B | Path C | Path D |
|--------|--------|--------|--------|--------|
| Setup Time | 2 min | 3 min | 5 min | 10 min |
| Requires Docker | ✅ | ✅ | ✅ | ❌ |
| Includes Jenkins | ✅ | ✅ | ❌ | ❌ |
| Best For | Quick Start | Testing | Learning | Development |
| Difficulty | Easiest | Easy | Medium | Hard |
| Recommended | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |

---

## 🆘 Quick Troubleshooting

### Problem: Port 8080 Already in Use

**Solution:**
```bash
# Find what's using the port
# Windows:
netstat -ano | findstr :8080

# Linux/macOS:
lsof -i :8080

# Stop the service or change port
# Edit docker-compose.yml:
# Change "8080:8080" to "8090:8080"
```

### Problem: Docker Not Running

**Solution:**
```bash
# Windows/macOS: Start Docker Desktop application
# Linux:
sudo systemctl start docker
sudo usermod -aG docker $USER
newgrp docker
```

### Problem: Container Won't Start

**Solution:**
```bash
# Check logs
docker logs container_id

# Rebuild without cache
docker build --no-cache -t jenkins-weather-app:latest .

# Try again
docker-compose restart
```

### Problem: Health Check Fails

**Solution:**
```bash
# Wait longer
sleep 30

# Test endpoint with verbose output
curl -v http://localhost:8080/weather?city=Pune

# Check container logs
docker logs jenkins-weather-app
```

---

## ✅ Success Checklist

After setting up, you should have:

- [ ] Docker running
- [ ] Application accessible at http://localhost:8080
- [ ] Weather endpoint returns data
- [ ] Health endpoint shows "UP"
- [ ] No errors in logs
- [ ] All containers running (docker ps)

---

## 🚀 Next Steps

### After Basic Setup Works:

1. **Read CI/CD Documentation:**
   ```bash
   cat PIPELINE_SETUP.md
   ```

2. **Setup Jenkins Pipeline:**
   - Access http://localhost:8081
   - Install plugins
   - Configure credentials
   - Create pipeline job

3. **Configure GitHub Webhook:**
   - Go to GitHub repository settings
   - Add webhook to Jenkins

4. **Test Full CI/CD:**
   - Push code to repository
   - Watch pipeline execute

---

## 📚 Additional Resources

| Document | Purpose |
|----------|---------|
| `QUICKSTART.md` | 5-minute setup |
| `BUILD_AND_RUN.md` | Detailed build guide |
| `PIPELINE_SETUP.md` | CI/CD configuration |
| `QUICK_REFERENCE.md` | Command reference |
| `CI_CD_SUMMARY.md` | Architecture overview |

---

## 💬 Questions?

**Common Issues:**
- Check logs: `docker logs container_id`
- Rebuild: `docker build --no-cache .`
- Reset everything: `docker-compose down -v`

**Need Help?**
- Read BUILD_AND_RUN.md for detailed instructions
- Check Docker documentation: https://docs.docker.com/
- Check Spring Boot documentation: https://spring.io/

---

**Ready to start? Pick a path above and follow the steps! 🎉**
