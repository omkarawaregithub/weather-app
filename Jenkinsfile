pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_IMAGE = 'jenkins-weather-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'jenkins-weather-app'
        HOST_PORT = '8081'
        CONTAINER_PORT = '8080'
    }

    stages {

        stage('Checkout') {
            steps {
                echo "========== CHECKOUT =========="
                checkout scm
            }
        }

        stage('Verify Tools') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker --version
                '''
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Verify Artifact') {
            steps {
                sh '''
                    echo "Generated JAR:"
                    ls -lh target/*.jar
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                    docker build \
                    -t ${DOCKER_IMAGE}:${IMAGE_TAG} \
                    -t ${DOCKER_IMAGE}:latest .
                """
            }
        }

        stage('Verify Docker Image') {
            steps {
                sh "docker images | grep ${DOCKER_IMAGE}"
            }
        }

        stage('Deploy Container') {
            steps {
                sh """
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    docker run -d \
                      --name ${CONTAINER_NAME} \
                      -p ${HOST_PORT}:${CONTAINER_PORT} \
                      ${DOCKER_IMAGE}:${IMAGE_TAG}
                """
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    sleep 15
                    curl -I http://localhost:8081
                '''
            }
        }

        stage('Running Containers') {
            steps {
                sh 'docker ps'
            }
        }

        stage('Cleanup Old Images') {
            steps {
                sh '''
                    docker image prune -f
                '''
            }
        }
    }

    post {

        success {
            echo '================================='
            echo 'Pipeline executed successfully!'
            echo 'Application is deployed.'
            echo '================================='
        }

        failure {
            echo '================================='
            echo 'Pipeline failed!'
            echo 'Container Logs:'
            sh 'docker logs ${CONTAINER_NAME} || true'
            echo '================================='
        }

        always {
            echo 'Pipeline completed.'
        }
    }
}