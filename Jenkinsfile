pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')        // Poll every 2 minutes
        cron('H/5 * * * *')           // Build every 5 minutes
    }

    stages {

        stage('Clone Repository') {
            steps {
                echo "Cloning Repository..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building Maven Project..."
                bat 'mvn clean package'
            }
        }

        stage('Echo Build Status') {
            steps {
                echo "Build completed successfully!"
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
}
