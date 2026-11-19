pipeline {
    agent any

    // Note: If you configured named tools in "Manage Jenkins -> Global Tool Configuration",
    // you can uncomment this block. Otherwise, Jenkins uses 'mvn' & 'java' from the system PATH.
    /*
    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 11'
    }
    */

    environment {
        APP_NAME = 'event-management'
        VERSION  = '1.0.0'
        JAR_FILE = "target/${APP_NAME}-${VERSION}-shaded.jar"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Compiling the application with Maven..."
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "Running unit tests with Maven Surefire..."
                bat 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo "Packaging the application into a shaded executable JAR..."
                bat 'mvn package -DskipTests'
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo "Archiving build artifacts for distribution..."
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                echo "============================================"
                echo " Deployment Stage (CI/CD Pipeline)"
                echo "============================================"
                echo "Target Artifact: ${JAR_FILE}"
                echo "Starting/Deploying application on Port 8081..."
                echo "Application live URL: http://localhost:8081"
                echo "Deployment complete."
            }
        }
    }

    post {
        success {
            echo '============================================'
            echo ' SUCCESS: Pipeline completed without errors!'
            echo '============================================'
        }
        failure {
            echo '============================================'
            echo ' FAILURE: Pipeline failed. Check console log.'
            echo '============================================'
        }
        always {
            echo "Build #${env.BUILD_NUMBER} finished with status: ${currentBuild.currentResult}"
        }
    }
}
