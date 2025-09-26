// Jenkinsfile (Scripted Pipeline)
node {
  stage('SCM') {
    checkout scm
    sh 'chmod +x mvnw'  // mvnw를 쓸 때만 필요
  }

  stage('Build') {
    sh '''
      set -euxo pipefail
      ./mvnw -B clean install -DskipTests
    '''
  }

  stage('SonarQube Analysis') {
    // Jenkins > Manage Jenkins > System > SonarQube servers 에 등록한 이름
    withSonarQubeEnv('myproject-pipeline') {
      // 디버깅이 필요하면 아래 줄 주석 해제해서 env 확인
      // sh 'env | sort | grep SONAR || true'

      sh '''
        set -euxo pipefail
        ./mvnw -B sonar:sonar -DskipTests \
          -Dsonar.host.url=$SONAR_HOST_URL \
          -Dsonar.token=$SONAR_AUTH_TOKEN \
          -Dsonar.projectKey=chat-backend
          # SonarCloud를 사용하는 경우 아래 옵션도 필요합니다:
          # -Dsonar.organization=<your_org_key>
      '''
    }
  }
}