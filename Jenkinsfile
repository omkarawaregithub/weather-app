pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_IMAGE = 'jenkins-weather-app:latest'
        CONTAINER_NAME = 'jenkins-weather-app'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the Spring Boot application'
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging the application'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image'
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying Docker container'
                sh """
                    docker rm -f ${CONTAINER_NAME} >/dev/null 2>&1 || true
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        -p 8080:8080 \
                        ${DOCKER_IMAGE}
                """
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
        success {
            echo 'Build and deployment succeeded.'
        }
        failure {
            echo 'Build or deployment failed.'
        }
    }
}