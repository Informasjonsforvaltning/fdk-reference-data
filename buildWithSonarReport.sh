GIT_BRANCH=`git symbolic-ref --short -q HEAD`

mvn clean install sonar:sonar -Dsonar.projectKey=Informasjonsforvaltning_fdk-reference-data -Dsonar.organization=informasjonsforvaltning -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_LOGIN -Dsonar.branch.name=$GIT_BRANCH
