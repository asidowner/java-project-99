### Task manager
It is a simple Task Manager application. User can log in, create new task, change its status and add labels.

### Hexlet tests and linter status:
[![Actions Status](https://github.com/asidowner/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/asidowner/java-project-99/actions)

### Code climate
[![Maintainability](https://api.codeclimate.com/v1/badges/a259171fb0cc0331ae50/maintainability)](https://codeclimate.com/github/asidowner/java-project-99/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/a259171fb0cc0331ae50/test_coverage)](https://codeclimate.com/github/asidowner/java-project-99/test_coverage)

### See on render.com
https://task-manager-rjgx.onrender.com/

### Build
```shell
./gradlew --no-daemon dependencies
./gradlew --no-daemon build
```

### Run
```shell
java -jar build/libs/app-0.0.1-SNAPSHOT.jar
```

### Build docker
```shell
docker build -t task-manager-app .
```

### Run docker
```shell
docker run -dp 127.0.0.1:8080:8080 task-manager-app
```
