node {
  stage('SCM') {
    checkout scm
    sh 'chmod +x mvnw'     // mvnw를 쓰는 경우만 필요
  }

  stage('Build') {
    sh './mvnw clean install -DskipTests'  // 또는 mvn clean install
  }

  stage('SonarQube Analysis') {
    withSonarQubeEnv() {
      sh './mvnw sonar:sonar -DskipTests'  // SonarQube 분석
    }
  }
}