# Build & Run Guide - Jenkins Weather Application

## Prerequisites

### System Requirements
- **OS**: Windows, macOS, or Linux
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Java**: OpenJDK 21 (optional for local Maven builds)
- **Maven**: 3.8+ (optional for local Maven builds)
- **Git**: 2.30+

### Installation

#### Windows
```powershell
# Install Docker Desktop
choco install docker-desktop -y

# Install Git
choco install git -y

# Verify installations
docker --version
docker-compose --version
git --version
```

#### macOS
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Docker Desktop
brew install --cask docker

# Install Git
brew install git

# Verify installations
docker --version
docker-compose --version
git --version
```

#### Linux (Ubuntu/Debian)
```bash
# Update package manager
sudo apt-get update

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Git
sudo apt-get install git -y

# Add current user to docker group (avoid sudo for docker)
sudo usermod -aG docker $USER
newgrp docker

# Verify installations
docker --version
docker-compose --version
git --version
```

---

## Option 1: Build with Maven (Local Build)

### Step 1: Clone the Repository
```bash
cd ~/projects
git clone https://github.com/your-username/jenkins_project.git
cd jenkins_project
```

### Step 2: Setup Environment Variables
```bash
# Create .env file
cp .env.example .env

# Edit .env with your API key
nano .env
# or on Windows:
notepad .env
```

**Update `.env`:**
```env
OPENWEATHERMAP_API_KEY=your_actual_api_key
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_docker_username
BUILD_NUMBER=1
```

### Step 3: Build with Maven
```bash
# Install dependencies and build
mvn clean install

# Run tests
mvn test

# Build package
mvn clean package
```

### Step 4: Run Application Locally
```bash
# Option A: Using Spring Boot Maven Plugin
mvn spring-boot:run

# Option B: Run the JAR directly
java -jar target/jenkins_project-0.0.1-SNAPSHOT.jar

# Option C: With environment variables
java -Dopenweathermap.api.key=your_api_key \
     -jar target/jenkins_project-0.0.1-SNAPSHOT.jar
```

### Step 5: Test the Application
```bash
# In a new terminal
# Test health endpoint
curl http://localhost:8080/actuator/health

# Test weather endpoint
curl "http://localhost:8080/weather?city=Pune"

# Response should be:
# {"city":"Pune","temperature":28.5,"description":"Partly cloudy",...}
```

---

## Option 2: Build with Docker (Recommended)

### Step 1: Clone the Repository
```bash
git clone https://github.com/your-username/jenkins_project.git
cd jenkins_project
```

### Step 2: Create Environment File
```bash
cat > .env << EOF
OPENWEATHERMAP_API_KEY=your_actual_api_key
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_docker_username
BUILD_NUMBER=1
EOF
```

### Step 3: Build Docker Image
```bash
# Build with specific version
docker build -t jenkins-weather-app:1.0 .

# Build with latest tag
docker build -t jenkins-weather-app:latest .

# Build with both tags
docker build -t jenkins-weather-app:1.0 -t jenkins-weather-app:latest .

# View image size
docker images | grep jenkins-weather-app
```

### Step 4: Run Docker Container
```bash
# Basic run
docker run -d \
  -p 8080:8080 \
  -e OPENWEATHERMAP_API_KEY=your_api_key \
  --name jenkins-app \
  jenkins-weather-app:latest

# With resource limits
docker run -d \
  -p 8080:8080 \
  -e OPENWEATHERMAP_API_KEY=your_api_key \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  --memory="512m" \
  --cpus="1" \
  --name jenkins-app \
  jenkins-weather-app:latest

# Check container status
docker ps -a

# View container logs
docker logs -f jenkins-app

# Stop container
docker stop jenkins-app

# Remove container
docker rm jenkins-app
```

### Step 5: Test the Application
```bash
# Wait a few seconds for app to start
sleep 5

# Test health endpoint
curl http://localhost:8080/actuator/health

# Test weather endpoint
curl "http://localhost:8080/weather?city=Pune"

# View logs
docker logs jenkins-app
```

---

## Option 3: Build & Run with Docker Compose

### Step 1: Clone Repository & Setup
```bash
git clone https://github.com/your-username/jenkins_project.git
cd jenkins_project
```

### Step 2: Create Environment File
```bash
cat > .env << EOF
OPENWEATHERMAP_API_KEY=your_actual_api_key
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_docker_username
BUILD_NUMBER=1
EOF
```

### Step 3: Start Development Environment
```bash
# Start development stack (no Jenkins)
docker-compose -f docker-compose.dev.yml up -d

# View status
docker-compose -f docker-compose.dev.yml ps

# View logs
docker-compose -f docker-compose.dev.yml logs -f
```

### Step 4: Start Complete Stack (with Jenkins)
```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs for specific service
docker-compose logs -f jenkins-weather-app
docker-compose logs -f jenkins
```

### Step 5: Access Services
```
Application:  http://localhost:8080
Jenkins:      http://localhost:8081
```

### Step 6: Test Services
```bash
# Test application
curl "http://localhost:8080/weather?city=Pune"

# Get Jenkins initial password
docker exec jenkins-server cat /var/jenkins_home/secrets/initialAdminPassword
```

### Step 7: Stop Services
```bash
# Stop and remove all containers
docker-compose down

# Stop but keep volumes
docker-compose stop

# Restart services
docker-compose restart

# Remove volumes too
docker-compose down -v
```

---

## Option 4: Automated Setup Script

### Linux/macOS
```bash
chmod +x setup.sh
./setup.sh
```

### Windows
```powershell
# Right-click PowerShell → Run as Administrator
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup.bat
```

The script will:
✅ Check Docker installation
✅ Create .env file if missing
✅ Build Docker image
✅ Create Docker networks
✅ Start all services
✅ Wait for services to be healthy
✅ Display access URLs

---

## Step-by-Step: Complete CI/CD Pipeline Setup

### 1. Setup Jenkins Server

```bash
# Start Jenkins container (or use docker-compose)
docker run -d \
  -p 8081:8080 \
  -p 50000:50000 \
  -v jenkins-home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins-server \
  jenkins/jenkins:lts

# Get initial admin password
docker logs jenkins-server | grep "Initial Admin Password"
```

### 2. Configure Jenkins

1. Open http://localhost:8081
2. Enter the initial admin password
3. Install suggested plugins
4. Create admin user
5. Install additional plugins:
   - Pipeline
   - Git
   - Docker Pipeline
   - SonarQube Scanner
   - Email Extension

### 3. Add Jenkins Credentials

1. Go to: **Manage Jenkins** → **Manage Credentials** → **System** → **Global credentials**
2. Click: **Add Credentials**
3. Add:
   - **Docker Hub**: Type=Username/Password, ID=docker-credentials
   - **GitHub**: Type=Personal Access Token, ID=github-credentials
   - **GitLab**: Type=Personal Access Token, ID=gitlab-credentials

### 4. Create Pipeline Job

1. Click: **New Item**
2. Name: `jenkins-weather-app`
3. Type: **Pipeline**
4. In Pipeline section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/your-username/jenkins_project.git`
   - Credentials: Select your GitHub credentials
   - Script Path: `Jenkinsfile`
5. Click: **Save**

### 5. Setup GitHub Webhook

1. Go to your GitHub repository
2. Settings → Webhooks → Add webhook
3. Payload URL: `http://jenkins-server:8081/github-webhook/`
4. Content type: `application/json`
5. Events: Push events, Pull requests
6. Click: **Add webhook**

### 6. Test Pipeline

```bash
# Make a test commit to develop branch
git checkout -b test-feature develop
echo "test" >> README.md
git add .
git commit -m "test: trigger CI pipeline"
git push origin test-feature

# Create Pull Request on GitHub
# Pipeline will trigger automatically
```

---

## Multi-Environment Deployment

### Deploy to Development
```bash
# Trigger on develop branch
git checkout develop
git commit --allow-empty -m "Deploy to dev"
git push origin develop

# Or manually
docker-compose -f docker-compose.dev.yml up -d
```

### Deploy to Staging
```bash
# Trigger on staging branch
git checkout staging
git merge develop --ff-only
git push origin staging

# Or manually
docker-compose -f docker-compose.staging.yml up -d
```

### Deploy to Production
```bash
# Trigger on main branch (requires manual approval in Jenkins)
git checkout main
git merge staging --ff-only
git push origin main

# Jenkins will pause for approval
# Approve in Jenkins UI to proceed

# Or manually
docker-compose -f docker-compose.prod.yml up -d
```

---

## Useful Commands Reference

### Docker Commands
```bash
# Build image
docker build -t jenkins-weather-app:latest .

# List images
docker images

# Run container
docker run -d -p 8080:8080 jenkins-weather-app:latest

# List containers
docker ps -a

# View logs
docker logs -f container_id

# Stop container
docker stop container_id

# Remove container
docker rm container_id

# Push to registry
docker push username/jenkins-weather-app:latest
```

### Docker Compose Commands
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View status
docker-compose ps

# View logs
docker-compose logs -f service_name

# Rebuild images
docker-compose build --no-cache

# Restart specific service
docker-compose restart jenkins-weather-app

# Execute command in container
docker-compose exec jenkins-weather-app bash
```

### Maven Commands
```bash
# Build project
mvn clean package

# Run tests
mvn test

# Build without tests
mvn clean package -DskipTests

# Run application
mvn spring-boot:run

# Clean build directory
mvn clean

# Check dependencies
mvn dependency:tree
```

### Git Commands
```bash
# Clone repository
git clone https://github.com/your-username/jenkins_project.git

# Create feature branch
git checkout -b feature/my-feature develop

# Commit changes
git commit -am "feat: my new feature"

# Push to remote
git push origin feature/my-feature

# Create pull request (GitHub CLI)
gh pr create --base develop --head feature/my-feature

# Merge branches
git checkout develop
git merge feature/my-feature
git push origin develop
```

---

## Troubleshooting

### Application won't start
```bash
# Check logs
docker logs jenkins-weather-app

# Common issues:
# 1. API key not set
# 2. Port already in use
# 3. Out of memory

# Fix: Stop conflicting containers
docker ps
docker stop container_id
```

### Docker build fails
```bash
# Rebuild with no cache
docker build --no-cache -t jenkins-weather-app:latest .

# Check disk space
docker system df

# Clean up
docker system prune -a
```

### Health check failing
```bash
# Wait longer for startup
sleep 15

# Test endpoint
curl -v http://localhost:8080/weather?city=Pune

# Check application logs
docker logs jenkins-weather-app

# Check if app is listening
docker exec jenkins-weather-app netstat -tuln
```

### Jenkins not accessible
```bash
# Check if Jenkins is running
docker ps | grep jenkins

# View Jenkins logs
docker logs jenkins-server

# Restart Jenkins
docker restart jenkins-server

# Access on http://localhost:8081
```

---

## Performance Optimization

### Local Development
```bash
# Use dev compose for faster startup
docker-compose -f docker-compose.dev.yml up -d

# Use smaller heap size
-Xmx512m -Xms256m
```

### Production Deployment
```bash
# Use optimized JVM settings
-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

# Set resource limits
--memory="2g"
--cpus="2"

# Use production compose
docker-compose -f docker-compose.prod.yml up -d
```

---

## Health Check Verification

```bash
# Application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}

# Weather endpoint
curl "http://localhost:8080/weather?city=Pune"

# Expected response:
# {"city":"Pune","temperature":28.5,"description":"Partly cloudy",...}
```

---

## Next Steps

1. ✅ Build and run locally with Docker Compose
2. ✅ Verify application is working
3. ✅ Setup Jenkins server
4. ✅ Configure GitHub webhook
5. ✅ Create and test pipeline
6. ✅ Deploy to different environments
7. ✅ Monitor and troubleshoot

---

## Success Checklist

- [ ] Docker is installed and working
- [ ] Application builds successfully
- [ ] Docker image is created
- [ ] Container runs without errors
- [ ] Health endpoint responds with 200 status
- [ ] Weather endpoint returns data
- [ ] Jenkins server is running
- [ ] Webhook is configured
- [ ] Pipeline job is created
- [ ] First pipeline run successful
- [ ] Application deployed to all environments

---

For more help, check:
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Spring Boot Guide](https://spring.io/projects/spring-boot)
