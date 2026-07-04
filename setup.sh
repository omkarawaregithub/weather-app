#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========== Jenkins Weather App - Setup Script ==========${NC}"

# Check Docker installation
echo -e "${BLUE}Checking Docker installation...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker is installed${NC}"

# Check Docker Compose installation
echo -e "${BLUE}Checking Docker Compose installation...${NC}"
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose is installed${NC}"

# Check .env file
echo -e "${BLUE}Checking .env file...${NC}"
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}Creating .env file...${NC}"
    cat > .env << EOF
OPENWEATHERMAP_API_KEY=your_api_key_here
DOCKER_REGISTRY=docker.io
DOCKER_USERNAME=your_username
BUILD_NUMBER=1
EOF
    echo -e "${GREEN}✓ .env file created. Please update it with your credentials.${NC}"
else
    echo -e "${GREEN}✓ .env file exists${NC}"
fi

# Load environment variables
source .env

# Build Docker image
echo -e "${BLUE}Building Docker image...${NC}"
docker build -t jenkins-weather-app:latest -t jenkins-weather-app:1.0 .
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Docker image built successfully${NC}"
else
    echo -e "${YELLOW}✗ Failed to build Docker image${NC}"
    exit 1
fi

# Create networks if they don't exist
echo -e "${BLUE}Creating Docker networks...${NC}"
docker network create jenkins-network 2>/dev/null || true
docker network create jenkins-network-dev 2>/dev/null || true
docker network create jenkins-network-staging 2>/dev/null || true
docker network create jenkins-network-prod 2>/dev/null || true
echo -e "${GREEN}✓ Networks ready${NC}"

# Start services
echo -e "${BLUE}Starting services...${NC}"
docker-compose up -d
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Services started successfully${NC}"
else
    echo -e "${YELLOW}✗ Failed to start services${NC}"
    exit 1
fi

# Wait for services to be ready
echo -e "${BLUE}Waiting for services to be healthy...${NC}"
sleep 15

# Test application health
echo -e "${BLUE}Testing application health...${NC}"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/weather?city=Pune 2>/dev/null || echo "000")
if [ "$RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ Application is healthy (Status: 200)${NC}"
else
    echo -e "${YELLOW}⚠ Application may not be fully ready (Status: $RESPONSE)${NC}"
fi

# Display service information
echo ""
echo -e "${GREEN}========== Setup Complete ==========${NC}"
echo ""
echo "Services running:"
echo -e "${BLUE}Application${NC}: http://localhost:8080"
echo -e "${BLUE}Jenkins${NC}: http://localhost:8081"
echo ""
echo "Useful commands:"
echo "  View logs:     docker-compose logs -f"
echo "  Stop services: docker-compose down"
echo "  Restart app:   docker-compose restart jenkins-weather-app"
echo "  Rebuild:       docker build -t jenkins-weather-app:latest ."
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Update credentials in Jenkins at http://localhost:8081"
echo "2. Create a new Pipeline job"
echo "3. Configure webhook in GitHub/GitLab"
echo "4. Make a test commit to trigger the pipeline"
echo ""
