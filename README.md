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

### Как запускать в Docker

1. Собрать приложение с помощью `mvn clean package` или `mvn -N wrapper:wrapper`
2. Добавить в `/etc/hosts`
   ```bush
      127.0.0.1 keycloak
   ```
3. Сделать файл `start.sh` исполняемыми
   ```bush
      chmod +x start.sh
   ```
2. Выполнить команду:
    ```bush 
        ./start.sh
    ```
3. Перейти по ссылке `http://localhost:9000/`

TODO:
* encrypt password in accounts-service
* get rid of currency enum in accounts-service
* add db for exchange service?
* currencies from one source
* move duplicate conf to parent?