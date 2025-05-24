# bankapp

Микросервисное приложение «Банк» с использованием `Spring Boot` и паттернов микросервисной архитектуры.

### Как собрать проект

Для сборки проекта достаточно запустить команду:
```bush
# если `maven` установлен
mvn clean package       
  
# если `maven` не установлен
./mvnw clean install      # Linux/macOS
mvnw.cmd clean install    # Windows
```

TODO:
* encrypt password in accounts-service
* get rid of currency enum in accounts-service
* add db for exchange service?