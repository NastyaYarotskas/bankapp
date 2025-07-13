#!/bin/bash

# Запускаем Keycloak
docker compose up -d keycloak-config-loader --build

# Ждем пока Keycloak станет доступен
while ! curl -s http://localhost:8080; do
  sleep 5
done

# Настраиваем клиента и получаем секрет
# shellcheck disable=SC2155
export FRONT_UI_SERVICE_CLIENT_SECRET=$(curl -s 'http://localhost:8500/v1/kv/secrets/FRONT_UI_SERVICE/client-secret?raw')
# shellcheck disable=SC2155
export ACCOUNTS_SERVICE_CLIENT_SECRET=$(curl -s 'http://localhost:8500/v1/kv/secrets/ACCOUNTS_SERVICE/client-secret?raw')
# shellcheck disable=SC2155
export CASH_SERVICE_CLIENT_SECRET=$(curl -s 'http://localhost:8500/v1/kv/secrets/CASH_SERVICE/client-secret?raw')
# shellcheck disable=SC2155
export EXCHANGE_GENERATOR_SERVICE_CLIENT_SECRET=$(curl -s 'http://localhost:8500/v1/kv/secrets/EXCHANGE_GENERATOR_SERVICE/client-secret?raw')
# shellcheck disable=SC2155
export TRANSFER_SERVICE_CLIENT_SECRET=$(curl -s 'http://localhost:8500/v1/kv/secrets/TRANSFER_SERVICE/client-secret?raw')

echo ""
echo "Client Secret FRONT_UI_SERVICE_CLIENT_SECRET:  ${FRONT_UI_SERVICE_CLIENT_SECRET}"
echo "Client Secret ACCOUNTS_SERVICE_CLIENT_SECRET:  ${ACCOUNTS_SERVICE_CLIENT_SECRET}"
echo "Client Secret CASH_SERVICE_CLIENT_SECRET:  ${CASH_SERVICE_CLIENT_SECRET}"
echo "Client Secret EXCHANGE_GENERATOR_SERVICE_CLIENT_SECRET:  ${EXCHANGE_GENERATOR_SERVICE_CLIENT_SECRET}"
echo "Client Secret TRANSFER_SERVICE_CLIENT_SECRET:  ${TRANSFER_SERVICE_CLIENT_SECRET}"

# Запускаем приложение с переменной окружения
docker compose up --build -d

echo "Приложение доступно на http://localhost:9000"