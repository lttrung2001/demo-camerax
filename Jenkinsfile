pipeline {
    agent any
    stages {
        stage("Checkout") {
            steps {
                git url: 'https://github.com/lttrung2001/demo-camerax.git',
                branch: '${BRANCH}'
            }
        }
        stage("Build debug APK") {
            steps {
                sh "./gradlew assembleDebug"
            }
        }
        stage("Publish APK) {
              steps {
                  sh 'firebase appdistribution:distribute ./app/build/outputs/apk/debug/app-debug.apk --app 1:100838938041:android:782097ca0ae7fc92f55268 --token "${FIREBASE_TOKEN}" --release-notes "${NOTE}" -- debug'
              }
        }
    }
}
