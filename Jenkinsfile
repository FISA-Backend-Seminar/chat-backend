node {
  stage('SCM') {
    checkout scm
    sh 'chmod +x mvnw'   // mvnw 쓸 때만
  }

  stage('Build') {
    // 컴파일/패키징까지 먼저 수행
    sh './mvnw -B -DskipTests clean verify'
  }

  stage('SonarQube Analysis') {
    // 여기엔 Jenkins에 등록한 SonarQube 서버 "설치 이름"을 넣어야 함
    withSonarQubeEnv('logmoa') {
      // POM에 projectKey/organization이 있다면 이것만으로 충분
      sh './mvnw -B -DskipTests sonar:sonar'
      // 만약 SonarCloud이고 POM에 org가 없다면 아래 식으로 추가:
      // sh './mvnw -B -DskipTests sonar:sonar -Dsonar.organization=your-org'
    }
  }
}