pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 11'
    }

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
                echo "Compiling the application..."
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "Running unit tests..."
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
                echo "Packaging the application into a shaded JAR..."
                bat 'mvn package -DskipTests'
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo "Archiving build artifacts..."
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                echo "============================================"
                echo " Deployment Stage"
                echo "============================================"
                echo "Artifact: ${JAR_FILE}"
                echo "Stopping any running instance of the app..."
                // On a real server you would run:
                //   bat 'taskkill /F /IM java.exe /T 2>nul || exit /b 0'
                // Then start the new jar as a background service:
                //   bat "start /B javaw -jar ${JAR_FILE}"
                echo "Starting application (simulation)..."
                echo "Application would be available at: http://localhost:8081"
                echo "Deploy simulation complete."
            }
        }
    }

    post {
        success {
            echo '============================================'
            echo ' Pipeline SUCCEEDED! App is ready.'
            echo '============================================'
        }
        failure {
            echo '============================================'
            echo ' Pipeline FAILED! Check the logs above.'
            echo '============================================'
        }
        always {
            echo "Build #${env.BUILD_NUMBER} finished with status: ${currentBuild.currentResult}"
            cleanWs()
        }
    }
}
