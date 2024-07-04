pipeline {
    agent any
    stages {
        stage("Checkout") {
            steps {
                git url: 'https://github.com/lttrung2001/demo-camerax.git'
            }
        }
        stage("Build debug APK") {
            steps {
                sh "./gradlew assembleDebug"
            }
        }
    }
}
