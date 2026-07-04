# CI/CD Quick Reference Guide

## File Structure

```
jenkins_project/
├── Dockerfile                 # Multi-stage Docker build
├── .dockerignore            # Files to exclude from Docker build
├── docker-compose.yml       # Complete stack with Jenkins
├── docker-compose.dev.yml   # Development environment
├── docker-compose.staging.yml # Staging environment
├── docker-compose.prod.yml  # Production environment
├── Jenkinsfile              # Jenkins Pipeline definition
├── sonar-project.properties # SonarQube configuration
├── .github/
│   └── workflows/
│       └── ci-cd.yml        # GitHub Actions pipeline
├── .gitlab-ci.yml           # GitLab CI pipeline
├── setup.sh                 # Linux/Mac setup script
├── setup.bat                # Windows setup script
├── PIPELINE_SETUP.md        # Detailed setup guide
└── QUICK_REFERENCE.md       # This file
```

## Quick Start

### Linux/Mac
```bash
chmod +x setup.sh
./setup.sh
```

### Windows
```batch
setup.bat
```

## Service Access

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | None |
| Jenkins | http://localhost:8081 | admin / admin |
| SonarQube | http://localhost:9000 | admin / admin |

## Common Commands

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f jenkins-weather-app
```

### Rebuild Docker Image
```bash
docker build -t jenkins-weather-app:latest .
```

### Execute Application Endpoint
```bash
curl http://localhost:8080/weather?city=Pune
```

### Health Check
```bash
curl -f http://localhost:8080/actuator/health
```

## Environment Files

### .env (Required)
```env
OPENWEATHERMAP_API_KEY=your_api_key
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_docker_username
BUILD_NUMBER=1
```

### application.properties
Already configured with environment variable substitution

## Pipeline Triggers

| Branch | Trigger | Deployment |
|--------|---------|-----------|
| develop | Auto on commit | Dev environment |
| staging | Auto on commit | Staging environment |
| main | Manual approval | Production environment |

## Docker Image Layers

1. **Builder Stage**: Maven compilation and packaging
2. **Runtime Stage**: JRE with application JAR
3. **Non-root user**: Security best practice
4. **Health checks**: Automated monitoring

## Jenkins Configuration Steps

1. Install Docker plugin in Jenkins
2. Add Docker credentials
3. Create new Pipeline job
4. Point to Jenkinsfile in repo
5. Configure webhook in GitHub/GitLab

## Troubleshooting

### Container Won't Start
```bash
docker logs jenkins-weather-app
docker-compose ps
```

### Port Already in Use
```bash
# Change port in docker-compose.yml
ports:
  - "8080:8080"  # Change first number to 8090:8080
```

### Docker Login Failed
```bash
docker logout
docker login
```

### Health Check Failing
```bash
# Give container more startup time
docker-compose logs jenkins-weather-app
curl -v http://localhost:8080/weather?city=Pune
```

## Security Checklist

- [ ] Update OPENWEATHERMAP_API_KEY in .env
- [ ] Change Jenkins default password
- [ ] Enable Jenkins authentication
- [ ] Configure HTTPS for webhooks
- [ ] Use private Docker registry
- [ ] Implement branch protection
- [ ] Review and rotate credentials regularly

## Performance Tips

- Use multi-stage Dockerfile (reduces image size by ~70%)
- Enable Docker layer caching during builds
- Set appropriate JVM heap sizes:
  - Dev: `-Xmx512m -Xms256m`
  - Staging: `-Xmx1g -Xms512m`
  - Prod: `-Xmx2g -Xms1g`

## Monitoring

### Application Metrics
- Health endpoint: `/actuator/health`
- Prometheus metrics: `/actuator/prometheus` (if enabled)

### Docker Metrics
```bash
docker stats
```

### Jenkins Build History
```
http://localhost:8081/job/jenkins-weather-app/
```

## Git Workflow

```bash
# Feature development
git checkout -b feature/weather-improvements develop

# Make changes
git add .
git commit -m "feat: improve weather display"

# Push and create PR
git push origin feature/weather-improvements
```

## Database (if needed)

For future MySQL integration:
```yaml
db:
  image: mysql:latest
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: jenkins_app
  volumes:
    - db-data:/var/lib/mysql
```

## Network Communication

Services communicate via Docker networks:
- `jenkins-network`: Main network
- `jenkins-network-dev`: Dev environment
- `jenkins-network-staging`: Staging environment
- `jenkins-network-prod`: Production environment

## CI/CD Best Practices

✅ **Version Control**: All configs in Git
✅ **Immutable Images**: Build once, deploy everywhere
✅ **Environment Parity**: Dev/Staging/Prod configs similar
✅ **Automated Testing**: Run tests in pipeline
✅ **Code Quality**: SonarQube integration
✅ **Monitoring**: Health checks enabled
✅ **Documentation**: README and guides maintained
✅ **Secrets Management**: Sensitive data in .env

## Scaling Considerations

For production scaling:
- Move to Kubernetes (Docker Compose → Helm)
- Add load balancer (NGINX)
- Implement auto-scaling policies
- Use managed container services (ECS, AKS, GKE)

## Support & Documentation

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [GitHub Actions Documentation](https://docs.github.com/actions)
- [GitLab CI Documentation](https://docs.gitlab.com/ee/ci/)

---

**Last Updated**: 2024
**Version**: 1.0
**Project**: Jenkins Weather Application
