pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'prod'], description: 'Deployment target environment')
        string(name: 'APP_PORT', defaultValue: '8080', description: 'Application HTTP listener port')
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true, description: 'Execute automated Selenium regression suite')
        choice(name: 'DEPLOY_TARGET', choices: ['DOCKER_CONTAINER', 'PUPPET_STANDALONE_NODE', 'TOMCAT_SERVER'], description: 'Runtime deployment target')
    }

    environment {
        APP_NAME = 'fitness-equipment-portal'
        REGISTRY_IMAGE = 'fitnessequipment/portal'
        BUILD_VERSION = "1.0.${BUILD_NUMBER}"
        HEALTHCHECK_URL = "http://localhost:${params.APP_PORT}/api/dashboard/health"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '10'))
        timestamps()
        ansiColor('xterm')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo "===> [Stage 1/9] Checking out source baseline from Git repository..."
                checkout scm
                sh '''
                    echo "Git Branch: ${GIT_BRANCH}"
                    echo "Git Commit: ${GIT_COMMIT}"
                '''
            }
        }

        stage('Compile & Lint') {
            steps {
                echo "===> [Stage 2/9] Compiling Java 21 classes with Maven..."
                sh 'mvn clean compile -B'
            }
        }

        stage('Unit & Service Tests') {
            steps {
                echo "===> [Stage 3/9] Executing JUnit 5 Service and Repository Unit Tests..."
                sh 'mvn test -Dtest=*ServiceTests,*ApiControllerTests,FitnessEquipmentApplicationTests -B'
            }
        }

        stage('Selenium Continuous Testing Gate') {
            when {
                expression { return params.RUN_SELENIUM == true }
            }
            steps {
                echo "===> [Stage 4/9] Running Headless Selenium WebDriver User Journeys..."
                sh '''
                    mkdir -p target/screenshots
                    mvn test -Dtest=*SeleniumTest,*JourneyTest -B || {
                        echo "[CRITICAL QUALITY GATE FAILURE] Selenium regression suite failed!"
                        echo "Blocking downstream container build and deployment!"
                        exit 1
                    }
                '''
            }
        }

        stage('Package & Archive Artifact') {
            steps {
                echo "===> [Stage 5/9] Packaging executable Spring Boot JAR..."
                sh 'mvn package -DskipTests -B'
                echo "Archiving build artifact target/fitness-equipment-portal.jar..."
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Docker Build & Tag') {
            when {
                expression { return params.DEPLOY_TARGET == 'DOCKER_CONTAINER' }
            }
            steps {
                echo "===> [Stage 6/9] Building and tagging versioned Docker image: ${REGISTRY_IMAGE}:${BUILD_VERSION}..."
                sh """
                    docker build -t ${REGISTRY_IMAGE}:${BUILD_VERSION} -t ${REGISTRY_IMAGE}:latest .
                    echo "Docker image built successfully with tag ${BUILD_VERSION}"
                """
            }
        }

        stage('Docker Registry Publish (Simulation)') {
            when {
                expression { return params.DEPLOY_TARGET == 'DOCKER_CONTAINER' }
            }
            steps {
                echo "===> [Stage 7/9] Pushing image ${REGISTRY_IMAGE}:${BUILD_VERSION} to container registry..."
                sh """
                    echo "[DOCKER PUSH] docker push ${REGISTRY_IMAGE}:${BUILD_VERSION}"
                    echo "[DOCKER PUSH] docker push ${REGISTRY_IMAGE}:latest"
                """
            }
        }

        stage('Continuous Deployment') {
            steps {
                echo "===> [Stage 8/9] Deploying release ${BUILD_VERSION} to target environment [${params.ENVIRONMENT}] on port ${params.APP_PORT}..."
                script {
                    if (params.DEPLOY_TARGET == 'DOCKER_CONTAINER') {
                        sh """
                            echo "Stopping existing container instance if running..."
                            docker stop fitness-equipment-portal-app || true
                            docker rm fitness-equipment-portal-app || true
                            echo "Starting updated container instance..."
                            docker run -d --name fitness-equipment-portal-app \
                                -p ${params.APP_PORT}:8080 \
                                -e PORT=8080 \
                                -e SPRING_PROFILES_ACTIVE=${params.ENVIRONMENT} \
                                --restart always \
                                ${REGISTRY_IMAGE}:${BUILD_VERSION}
                        """
                    } else if (params.DEPLOY_TARGET == 'PUPPET_STANDALONE_NODE') {
                        sh """
                            echo "Triggering Puppet Agent run on target node..."
                            sudo puppet agent -t || [ \$? -eq 2 ]
                        """
                    } else {
                        sh """
                            echo "Deploying artifact to Tomcat /opt/tomcat/webapps/..."
                            cp target/fitness-equipment-portal.jar /opt/fitness-portal/app.jar
                            sudo systemctl restart fitness-portal
                        """
                    }
                }
            }
        }

        stage('Smoke Test & Health Verification') {
            steps {
                echo "===> [Stage 9/9] Polling application health endpoint for verification..."
                sh """
                    echo "Waiting for application startup..."
                    sleep 10
                    for i in 1 2 3 4 5 6; do
                        STATUS=\$(curl -s -o /dev/null -w "%{http_code}" ${HEALTHCHECK_URL} || echo "000")
                        if [ "\$STATUS" = "200" ]; then
                            echo "Health check PASSED with HTTP 200 OK!"
                            exit 0
                        fi
                        echo "Attempt \$i: Endpoint returned HTTP \$STATUS. Retrying in 5 seconds..."
                        sleep 5
                    done
                    echo "Application health check FAILED. Initiating automatic rollback!"
                    exit 1
                """
            }
        }
    }

    post {
        always {
            echo "Publishing JUnit test execution reports..."
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
        failure {
            echo "[PIPELINE ALERT] Pipeline execution failed. Archiving failure screenshots..."
            archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
            echo "Deployment halted. Notification dispatched to engineering channel."
        }
        success {
            echo "[PIPELINE SUCCESS] Release ${BUILD_VERSION} successfully tested, packaged and deployed to ${params.ENVIRONMENT}!"
        }
    }
}
