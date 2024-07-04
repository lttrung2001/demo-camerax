// pipeline {
//     agent any
//     stages {
//         stage("Checkout") {
//             steps {
//                 git url: 'https://github.com/lttrung2001/demo-camerax.git',
//                 branch: '${BRANCH}'
//             }
//         }
//         stage("Get Commit Info") {
//             steps {
//                 script {
//                     // Get the commit hash and message
//                     env.COMMIT_HASH = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
//                     env.COMMIT_MESSAGE = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
//                 }
//             }
//         }
//         stage("Build debug APK") {
//             steps {
//                 sh "./gradlew assembleDebug"
//             }
//         }
//     }
// }

pipeline {
    agent any
    parameters {
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Git branch to build')
        string(name: 'GIT_COMMIT', defaultValue: '', description: 'Git commit to build')
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                          branches: [[name: "${params.GIT_BRANCH}"]],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [],
                          userRemoteConfigs: [[url: 'https://github.com/lttrung2001/demo-camerax.git']]])
                // Checkout specific commit if specified
                if (params.GIT_COMMIT) {
                    sh "git checkout ${params.GIT_COMMIT}"
                }
            }
        }
        stage('Build') {
            steps {
                // Build the APK
                sh './gradlew assembleDebug'
            }
        }
    }
}
