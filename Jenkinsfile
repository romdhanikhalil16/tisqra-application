pipeline {
    agent any
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        MAVEN_HOME = '/usr/share/maven'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_NAMESPACE = 'tisqra'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.BUILD_VERSION = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }
        
        stage('Test') {
            steps {
                sh '''
                    mvn test
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    publishHTML([
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'Code Coverage Report'
                    ])
                }
            }
        }
        
        stage('SonarQube Analysis') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    mvn sonar:sonar \
                        -Dsonar.projectKey=tisqra \
                        -Dsonar.sources=. \
                        -Dsonar.host.url=${SONARQUBE_HOST} \
                        -Dsonar.login=${SONARQUBE_TOKEN}
                '''
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    sh '''
                        docker build -t ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/api-gateway:${BUILD_VERSION} infrastructure/api-gateway/
                        docker build -t ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/config-server:${BUILD_VERSION} infrastructure/config-server/
                        docker build -t ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/discovery-service:${BUILD_VERSION} infrastructure/discovery-service/
                        docker build -t ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/user-service:${BUILD_VERSION} services/user-service/
                        docker build -t ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/event-service:${BUILD_VERSION} services/event-service/
                        docker build -t ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/notification-service:${BUILD_VERSION} services/notification-service/
                    '''
                }
            }
        }
        
        stage('Push Docker Images') {
            when {
                branch 'main'
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                            echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin
                            
                            docker push ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/api-gateway:${BUILD_VERSION}
                            docker push ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/config-server:${BUILD_VERSION}
                            docker push ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/discovery-service:${BUILD_VERSION}
                            docker push ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/user-service:${BUILD_VERSION}
                            docker push ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/event-service:${BUILD_VERSION}
                            docker push ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/notification-service:${BUILD_VERSION}
                            
                            docker logout
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    docker-compose -f docker-compose.yml up -d
                '''
            }
        }
        
        stage('Smoke Tests') {
            steps {
                sh '''
                    sleep 30
                    
                    curl -f http://localhost:8080/swagger-ui.html || exit 1
                    curl -f http://localhost:8081/swagger-ui.html || exit 1
                    curl -f http://localhost:8083/swagger-ui.html || exit 1
                '''
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            slackSend(
                color: 'good',
                message: "Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            )
        }
        failure {
            slackSend(
                color: 'danger',
                message: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            )
        }
    }
}
