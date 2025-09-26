// Jenkinsfile (Scripted Pipeline)
node {
  // 선택: 로그 가독성
  ansiColor('xterm')
  timestamps()

  stage('SCM') {
    checkout scm
    sh 'chmod +x mvnw' // mvnw를 쓸 때만 필요
  }

  stage('Build') {
    sh './mvnw -B clean install -DskipTests'
  }

  stage('SonarQube Analysis') {
    // Jenkins > Manage Jenkins > System > SonarQube servers에 등록한 "이름"
    withSonarQubeEnv('myproject-pipeline') {
      // (디버깅용) 환경변수 제대로 주입됐는지 확인하고 싶으면 주석 해제
      // sh 'env | sort | grep SONAR || true'

      sh '''
        ./mvnw -B sonar:sonar -DskipTests \
          -Dsonar.host.url=$SONAR_HOST_URL \
          -Dsonar.token=$SONAR_AUTH_TOKEN \
          -Dsonar.projectKey=chat-backend
          # SonarCloud 쓰면 아래 줄도 추가:
          # -Dsonar.organization=<your_org_key>
          # 필요 시 명시적으로 소스/바이너리 경로:
          # -Dsonar.sources=src/main/java \
          # -Dsonar.tests=src/test/java \
          # -Dsonar.java.binaries=target/classes
      '''
    }
  }

  // (선택) 품질 게이트 대기 — SonarQube Webhook 연동 권장
  stage('Quality Gate') {
    timeout(time: 10, unit: 'MINUTES') {
      waitForQualityGate() // 실패면 파이프라인 실패 처리
    }
  }

  // (선택) 리포트/아티팩트 수집
  stage('Archive') {
    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, onlyIfSuccessful: false
  }
}
