# CI/CD Pipeline Setup Guide

## Project Overview
- **Application**: Jenkins Weather App
- **Framework**: Spring Boot 3.5.3
- **Java Version**: 21
- **Port**: 8080

## CI/CD Pipeline Architecture

```
Code Commit → GitHub/GitLab → Jenkins → Build → Test → SonarQube → Docker Build → Push Registry → Deploy
```

## Pipeline Stages

### 1. **Checkout**
   - Checks out code from repository

### 2. **Build**
   - Builds Maven project
   - Creates executable JAR file

### 3. **Unit Tests**
   - Runs all unit tests
   - Publishes test reports

### 4. **Code Quality Analysis**
   - Analyzes code with SonarQube
   - Checks code coverage

### 5. **Docker Image Build**
   - Creates multi-stage Docker image
   - Tags with build number and latest

### 6. **Push to Registry**
   - Authenticates with Docker Hub
   - Pushes images to registry

### 7. **Deploy**
   - **Dev**: Deploys on every commit to `develop` branch
   - **Staging**: Deploys on every commit to `staging` branch
   - **Production**: Manual approval required for `main` branch

### 8. **Smoke Tests**
   - Verifies application health
   - Tests API endpoints

## Prerequisites

### Jenkins Server
1. **Jenkins Installation**
   ```bash
   docker run -d -p 8081:8080 -p 50000:50000 \
     -v jenkins-home:/var/jenkins_home \
     -v /var/run/docker.sock:/var/run/docker.sock \
     jenkins/jenkins:lts
   ```

2. **Required Jenkins Plugins**
   - Pipeline
   - Git
   - Docker Pipeline
   - SonarQube Scanner
   - Email Extension
   - Credentials Binding

3. **Credentials Setup in Jenkins**
   - Go to: **Manage Jenkins** → **Manage Credentials**
   - Add credentials:
     - `docker-username`: Docker Hub username
     - `docker-password`: Docker Hub password/token
     - `github-credentials`: GitHub token (if using GitHub)

### Local Environment

1. **Install Docker**
   ```bash
   # Windows
   choco install docker-desktop
   
   # Linux
   curl -fsSL https://get.docker.com -o get-docker.sh | sh
   
   # macOS
   brew install docker
   ```

2. **Install Docker Compose**
   ```bash
   docker-compose --version
   ```

3. **Install Maven (optional, Docker handles it)**
   ```bash
   mvn --version
   ```

## Environment Variables

Create `.env` file in project root:

```env
OPENWEATHERMAP_API_KEY=your_api_key_here
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_username
BUILD_NUMBER=1
```

## Running Locally

### Development Environment
```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Full Stack with Jenkins
```bash
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f jenkins-weather-app
```

### Stop Services
```bash
docker-compose down
```

## Building Docker Image Manually

```bash
# Build image
docker build -t jenkins-weather-app:1.0 .

# Run container
docker run -d -p 8080:8080 \
  -e OPENWEATHERMAP_API_KEY=your_key \
  jenkins-weather-app:1.0

# Push to registry
docker tag jenkins-weather-app:1.0 username/jenkins-weather-app:1.0
docker push username/jenkins-weather-app:1.0
```

## Jenkins Pipeline Configuration

### Webhook Setup (GitHub/GitLab)

**GitHub:**
1. Go to repository **Settings** → **Webhooks**
2. Add webhook: `http://jenkins-server:8081/github-webhook/`
3. Events: Push events, Pull requests

**GitLab:**
1. Go to project **Settings** → **Integrations**
2. Add webhook: `http://jenkins-server:8081/project/your-project`
3. Trigger: Push events, Merge request events

### Creating Jenkins Pipeline

1. New Item → **Pipeline**
2. Name: `jenkins-weather-app`
3. Pipeline section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: Your Git repo
   - Credentials: Select GitHub credentials
   - Script Path: `Jenkinsfile`
4. **Save**

## Branch Strategy

- **main**: Production releases (requires approval)
- **staging**: Pre-production testing
- **develop**: Development integration

## Docker Registry

### Push to Docker Hub
```bash
docker login
docker tag jenkins-weather-app:latest username/jenkins-weather-app:latest
docker push username/jenkins-weather-app:latest
```

### Push to Private Registry
```bash
docker tag jenkins-weather-app:latest registry.example.com/jenkins-weather-app:latest
docker push registry.example.com/jenkins-weather-app:latest
```

## Monitoring & Logs

### Jenkins Logs
```bash
docker logs -f jenkins-server
```

### Application Logs
```bash
docker logs -f jenkins-weather-app
```

### Persistent Logs
```bash
docker exec jenkins-weather-app cat /app/logs/app.log
```

## Troubleshooting

### Container Won't Start
```bash
# Check logs
docker-compose logs jenkins-weather-app

# Rebuild image
docker-compose build --no-cache
```

### Docker Authentication Failed
```bash
# Login manually
docker login

# In Jenkins, update credentials
```

### SonarQube Connection Error
1. Ensure SonarQube is running
2. Update Jenkins credentials
3. Check firewall rules

## Security Best Practices

✅ Use environment variables for sensitive data
✅ Enable Jenkins authentication
✅ Use private Docker registries
✅ Implement branch protection rules
✅ Enable HTTPS for webhooks
✅ Regularly update Jenkins plugins
✅ Run containers with non-root users (already configured)
✅ Use health checks (already configured)

## Performance Optimization

- Multi-stage Docker build (reduces image size)
- Maven dependency caching in Docker
- Parallel test execution in Jenkins
- Resource limits in docker-compose
- Health checks for automatic restart

## Success Indicators

✅ Jenkins job runs successfully
✅ Docker image builds without errors
✅ Application starts and responds to health checks
✅ SonarQube analysis completes
✅ Images push to registry successfully
✅ Application deploys to target environment

## Next Steps

1. Setup Jenkins server
2. Configure GitHub/GitLab webhook
3. Add Docker credentials to Jenkins
4. Create pipeline job
5. Test pipeline with a commit
6. Monitor pipeline execution
7. Verify deployments

---

For detailed Jenkins documentation, visit: https://www.jenkins.io/doc/
For Docker documentation, visit: https://docs.docker.com/
