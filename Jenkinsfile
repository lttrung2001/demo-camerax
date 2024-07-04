pipeline {
    agent any
    parameters {
        gitParameter branchFilter: 'origin/(.*)', defaultValue: 'master', name: 'BRANCH', type: 'PT_BRANCH'
    }
    stages {
        stage("Checkout") {
            steps {
                git branch: "${params.BRANCH}", url: 'https://github.com/lttrung2001/demo-camerax.git'
                // git url: 'https://github.com/lttrung2001/demo-camerax.git',
                // branch: '${BRANCH}'
            }
        }
        stage("Get Commit Info") {
            steps {
                script {
                    // Get the commit hash and message
                    env.COMMIT_HASH = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    env.COMMIT_MESSAGE = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                }
            }
        }
        stage("Build debug APK") {
            steps {
                sh "./gradlew assembleDebug"
            }
        }
    }
}
