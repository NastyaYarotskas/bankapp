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

### Как запускать в Minikube

1. Собрать docker образы
```bush
eval $(minikube docker-env)
docker-compose build accounts-service blocker-service cash-service exchange-generator-service exchange-service front-ui notification-service transfer-service
```

2. Обновить зависимости helm чарта
```bush
cd .deployment
helm dependency update .
```

3. Установить keycloak
```bush
kubectl apply -f keycloak-deployment.yaml
minikube service keycloak --url
kubectl delete -f keycloak-deployment.yaml
```

4. Импортировать скоупы и клиенты для keycloak
```bush
cd .docker/keycloak-config-loader/scripts
sh upload-scopes-to-keycloak.sh
sh upload-clients-to-keycloak.sh
```

5. Задеплоить сервисы с помощью helm чартов
```bush
# for install
helm install bankapp .
# for update
helm upgrade bankapp .
# for delete
helm uninstall bankapp
```

6. Использовать `minikube tunnel`
7. Перейти по ссылке `localhost:9000`

### Как запускать helm test

1. перейти в папку `.deployment` и выполнить комманды:

```bush
helm lint . 

helm install --dry-run bankapp . 

helm test bankapp 
```