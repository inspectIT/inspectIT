
node('inspectIT') {
   stage('Preparation') {
      git credentialsId: '60b7f10c-cf94-4f98-83b9-17f5453a3f8c', url: 'git@github.com:inspectIT/inspectIT.git'
   }
   stage('Clean') {
    sh "./gradlew clean"
   }
   stage('Jar') {
    sh "./gradlew jar"
   }
   stage('Test') {
    parallel (
     testAgent: { sh "./gradlew :inspectit.agent.java:test" },
     testCMR: { sh "./gradlew :inspectit.server:test" }
    )
   }
}
