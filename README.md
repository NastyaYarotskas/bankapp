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

1. Установите Ingress Controller в кластер

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx   --namespace ingress-nginx --create-namespace
```

2. Собрать docker образы
```bush
eval $(minikube docker-env)
docker-compose build accounts-service blocker-service cash-service exchange-generator-service exchange-service front-ui notification-service transfer-service
```

3. Обновить зависимости helm чарта
```bush
cd .deployment
helm dependency update .
```

4. Установить keycloak и кафку
```bush
cd ..
kubectl apply -f keycloak-deployment.yaml
## для удаления
kubectl delete -f keycloak-deployment.yaml

kubectl apply -f kafka-deployment.yaml
## для удаления
kubectl delete -f kafka-deployment.yaml
```

5. Добавьте записи в `/etc/hosts`

```bash
sudo nano /etc/hosts
```

Добавьте:

```text
127.0.0.1 keycloak.test.local
127.0.0.1 front-ui.test.local
```

6. Использовать `minikube tunnel`

7. Импортировать скоупы и клиенты для keycloak
```bush
cd .docker/keycloak-config-loader/scripts
sh upload-scopes-to-keycloak.sh
sh upload-clients-to-keycloak.sh
```

8. Задеплоить сервисы с помощью helm чартов
```bush
cd .deployment
# for install
helm install bankapp .
# for update
helm upgrade bankapp .
# for delete
helm uninstall bankapp
```

9. Перейти по ссылке `http://front-ui.test.local`

### Как запускать helm test

1. перейти в папку `.deployment` и выполнить комманды:

```bush
helm lint . 

helm install --dry-run bankapp . 

helm test bankapp 
```

### Как запускать Jenkins

1. Установите Ingress Controller в кластер

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx   --namespace ingress-nginx --create-namespace
```

2. Создайте файл `jenkins_kubeconfig.yaml`

Jenkins будет использовать этот файл для доступа к Kubernetes.

Выполните в терминале:

```bash
cp ~/.kube/config jenkins_kubeconfig.yaml
```

Затем отредактируйте файл:

**Замените `server` на:**

```yaml
server: https://192.168.49.2:8443
```

**Добавьте:**

```yaml
insecure-skip-tls-verify: true
```

Это нужно, чтобы Jenkins внутри контейнера смог обратиться к вашему локальному кластеру и проигнорировал самоподписанные сертификаты.

5. Создайте `.env` файл

Создайте файл `.env` в корне проекта:

```env
# Путь до локального kubeconfig-файла
KUBECONFIG_PATH=/Users/username/.kube/jenkins_kubeconfig.yaml

# Параметры для GHCR
GITHUB_USERNAME=your-username
GITHUB_TOKEN=ghp_...
GHCR_TOKEN=ghp_...

# Docker registry (в данном случае GHCR)
DOCKER_REGISTRY=ghcr.io/your-username
GITHUB_REPOSITORY=your-username/bankapp
```

> Убедитесь, что ваш GitHub Token имеет права `write:packages`, `read:packages` и `repo`.

6. Запустите Jenkins

```bash
cd jenkins
docker compose up -d --build
```

Jenkins будет доступен по адресу: [http://localhost:8080](http://localhost:8080)

---

## Как использовать

1. Откройте Jenkins: [http://localhost:8080](http://localhost:8080)
2. Перейдите в задачу `BankappHelmApp` → `Build Now`
3. Jenkins выполнит:
   - сборку и тесты
   - сборку Docker-образов
   - публикацию образов в GHCR
   - деплой в Kubernetes в два namespace: `test` и `prod`