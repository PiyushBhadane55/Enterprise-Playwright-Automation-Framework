pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'dev', 'uat', 'prod'], description: 'Select Test Environment')
        string(name: 'BROWSER', defaultValue: 'chrome', description: 'Browser to run tests on (chrome, firefox, edge, webkit)')
    }

    tools {
        maven 'Maven 3.9'
        jdk 'Java 21'
    }

    environment {
        ALLURE_HOME = tool name: 'Allure', type: 'io.qameta.allure.jenkins.tools.AllureCommandlineInstallation'
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                echo "Compiling code and resolving dependencies..."
                sh "mvn clean test-compile -B"
            }
        }

        stage('Execute Tests') {
            steps {
                echo "Running test suite on ${params.ENVIRONMENT} using browser ${params.BROWSER}..."
                // Continue on failure to ensure reports are generated even if tests fail
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh "mvn test -Denv=${params.ENVIRONMENT} -Dbrowser=${params.BROWSER} -Dtestng.dtd.http=true"
                }
            }
        }

        stage('Allure Report') {
            steps {
                echo "Generating Allure Reports..."
                allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
            }
        }

        stage('Archive Results') {
            steps {
                echo "Archiving screenshots, videos, and traces..."
                archiveArtifacts artifacts: 'target/screenshots/**/*.png, target/videos/**/*.webm, target/traces/**/*.zip', allowEmptyArchive: true
            }
        }
    }

    post {
        always {
            echo "Pipeline complete. Cleaning up workspace..."
            cleanWs()
        }
        success {
            echo "Automation execution completed successfully!"
        }
        failure {
            echo "Automation execution encountered failures. Please check the Allure report and archived screenshots/videos."
        }
    }
}
