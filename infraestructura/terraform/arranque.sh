#!/bin/bash
# Script de arranque de la instancia (user_data): se ejecuta una sola vez, como root,
# la primera vez que la maquina enciende. Deja Docker instalado y RabbitMQ + MySQL
# corriendo con los mismos nombres, puertos y credenciales del docker-compose.yml local.
# El log queda en /var/log/arranque.log
exec > /var/log/arranque.log 2>&1
set -eux

# Docker
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
usermod -aG docker ubuntu || true

# RabbitMQ con dashboard (15672). --restart: vuelve a levantarse si la instancia se reinicia
docker run -d --name rabbitmq-server --restart unless-stopped \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  -v rabbitmq_data:/var/lib/rabbitmq \
  rabbitmq:3-management

# MySQL. carrito_db la crea venta-carrito al conectarse (createDatabaseIfNotExist=true)
docker run -d --name mysql-server --restart unless-stopped \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=catalogo_db \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0

echo "ARRANQUE COMPLETO"
