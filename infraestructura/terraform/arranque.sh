#!/bin/bash
# Script de arranque de la instancia (user_data): se ejecuta una sola vez, como root,
# la primera vez que la maquina enciende. Instala Docker, clona el repositorio y
# levanta RabbitMQ, MySQL y los 3 microservicios con infraestructura/desplegar.sh.
# El log queda en /var/log/arranque.log
exec > /var/log/arranque.log 2>&1
set -eux

# Docker (incluye el plugin "docker compose")
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
usermod -aG docker ubuntu || true

# Codigo del proyecto
git clone --depth 1 --branch "${rama}" "${repo_url}" /opt/pedidos360

bash /opt/pedidos360/infraestructura/desplegar.sh

echo "ARRANQUE COMPLETO"
