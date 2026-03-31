pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        IMAGE_NAME = 'customer-orders-api'
        CONTAINER_NAME = 'customer-orders-api'
        APP_PORT = '8080'
        SONARQUBE_SERVER = 'SonarQube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT_SHA = sh(
                            script: 'git rev-parse --short=8 HEAD',
                            returnStdout: true
                    ).trim()
                    env.IMAGE_TAG = env.GIT_SHORT_SHA
                }
            }
        }

        stage('Build and Test') {
            steps {
                sh './mvnw clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/*.jar,target/site/jacoco/**,target/surefire-reports/**'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_AUTH_TOKEN')]) {
                        sh './mvnw sonar:sonar -Dsonar.token=$SONAR_AUTH_TOKEN'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .'
            }
        }

        stage('Deploy') {
            when {
                expression {
                    env.BRANCH_NAME == null || env.BRANCH_NAME == 'main'
                }
            }
            steps {
                sh '''
                    IMAGE_NAME="${IMAGE_NAME}" \
                    IMAGE_TAG="${IMAGE_TAG}" \
                    CONTAINER_NAME="${CONTAINER_NAME}" \
                    HOST_PORT="${APP_PORT}" \
                    ./scripts/deploy-local.sh
                '''
            }
        }

        stage('Smoke Test') {
            when {
                expression {
                    env.BRANCH_NAME == null || env.BRANCH_NAME == 'main'
                }
            }
            steps {
                sh 'curl --fail --silent http://localhost:${APP_PORT}/actuator/health'
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for ${env.GIT_SHORT_SHA}."
        }
        failure {
            echo 'Pipeline failed. Review the stage logs above to see where it stopped.'
        }
    }
}
