node {
  stage('SCM') {
    checkout scm
    sh 'chmod +x mvnw'     // mvnw를 쓰는 경우만 필요!!
  }

  stage('Build') {
    sh './mvnw clean install -DskipTests'  // 또는 mvn clean install
  }

  stage('SonarQube Analysis') {
    withSonarQubeEnv('myproject-pipeline') {
      sh '''
      ./mvnw sonar:sonar -DskipTests \
              -Dsonar.host.url=$SONAR_HOST_URL \
              -Dsonar.token=$SONAR_AUTH_TOKEN \
              -Dsonar.projectKey=chat-backend
      '''
    }
  }
}