# Pedidos360 · Cómo iniciar y desplegar

Guía paso a paso con los comandos exactos. Hay dos formas de levantar el proyecto:

| Forma | Cuándo usarla | Tiempo |
|---|---|---|
| [A. En AWS (EC2)](#a-despliegue-en-aws-ec2) | Presentación y prueba completa | 15 a 20 min |
| [B. En el computador](#b-todo-en-el-computador-plan-b) | Desarrollo, o si AWS falla | 5 min |

El detalle del backend (topología de RabbitMQ, ACK/NACK, API del administrador) está en [back-app/README.md](back-app/README.md).

| Carpeta | Contenido |
|---|---|
| `back-app/` | 3 microservicios Spring Boot: `productos-catalogo` (8081), `venta-carrito` (8082), `rabbitmq-admin` (8083) |
| `front-app/` | Frontend Angular (4200) |
| `infraestructura/` | Terraform y scripts de despliegue en la EC2 |

---

## Antes de empezar

- **Todo lo que se despliega sale de GitHub.** La EC2 clona la rama `main`, así que antes de desplegar:
  ```bash
  git status          # no debe quedar nada pendiente
  git push
  ```
- Los comandos de la forma A se ejecutan en **Git Bash** (no en PowerShell), porque `credenciales.sh` es un script de Bash.
- El acceso a la EC2 queda abierto **solo para la IP desde la que se ejecuta `terraform apply`**. Hay que ejecutarlo conectado a la red donde se va a presentar. Si se cambia de red, basta repetir `terraform apply`.

---

## A. Despliegue en AWS (EC2)

### 1. Credenciales de AWS Academy

1. Entrar a AWS Academy → **Learner Lab** → **Start Lab** y esperar el círculo verde.
2. **AWS Details** → **AWS CLI: Show**. Copiar los tres valores.
3. Pegarlos en `infraestructura/terraform/credenciales.sh`, que debe quedar así:
   ```bash
   export AWS_ACCESS_KEY_ID=...
   export AWS_SECRET_ACCESS_KEY=...
   export AWS_SESSION_TOKEN=...
   ```
   Las credenciales cambian cada vez que se inicia el laboratorio. Este archivo no se sube a GitHub.

### 2. Crear la infraestructura

En Git Bash, desde la raíz del repositorio:

```bash
cd infraestructura/terraform
source credenciales.sh
terraform init
terraform apply
```

Escribir `yes` cuando lo pida. Al terminar imprime:

```
instance_public_ip = "X.X.X.X"
rabbitmq_dashboard = "http://X.X.X.X:15672"
ssh                = "ssh -i pedidos360.pem ubuntu@X.X.X.X"
```

Anotar la IP: se usa en todos los pasos siguientes (aparece como `IP`). Para verla de nuevo: `terraform output`.

> Terraform usa el key pair `pedidos360`. Si en la cuenta solo existe `vockey`:
> `terraform apply -var "key_name=vockey"`

### 3. Esperar el despliegue (8 a 10 minutos)

La instancia instala Docker, clona el repositorio, compila los 3 microservicios y los levanta junto a RabbitMQ y MySQL. Está listo cuando estas direcciones responden en el navegador:

| Dirección | Debe mostrar |
|---|---|
| `http://IP:8081/api/productos/ping` | Texto de servicio activo |
| `http://IP:8082/api/carrito/ping` | `venta-carrito activo` |
| `http://IP:8083/swagger-ui.html` | Documentación del administrador |
| `http://IP:15672` | Dashboard de RabbitMQ (usuario `admin`, clave `admin123`) |

En el dashboard, pestaña **Queues and Streams**, deben aparecer `pedidos.stock.queue`, `pedidos.ticket.queue`, `pedidos.stock.dlq` y `pedidos.ticket.dlq`.

### 4. Frontend

1. Editar `front-app/src/environments/environment.development.ts` y poner la IP:
   ```ts
   catalogoUrl: 'http://IP:8081',
   carritoUrl: 'http://IP:8082',
   ```
2. En una terminal:
   ```bash
   cd front-app
   npm install        # solo la primera vez
   npm start
   ```
3. Abrir `http://localhost:4200`.

No subir a GitHub el cambio de IP: es distinta en cada despliegue. Para deshacerlo: `git checkout front-app/src/environments/environment.development.ts`

### 5. Ver los logs de los microservicios

Abrir dos terminales de Git Bash en la carpeta donde está `pedidos360.pem`. En cada una, entrar a la instancia:

```bash
ssh -i pedidos360.pem ubuntu@IP
```

Y dejar corriendo un log en cada terminal:

```bash
docker logs -f productos-catalogo     # terminal 1: stock y su DLQ
docker logs -f venta-carrito          # terminal 2: productor, tickets y su DLQ
```

Se sale del log con `Ctrl + C` y de la instancia con `exit`.

### 6. Apagar todo al terminar

```bash
cd infraestructura/terraform
source credenciales.sh
terraform destroy
```

Escribir `yes`. Así no se gasta el saldo del laboratorio.

---

## Qué mostrar en la presentación

### 1. Flujo completo (productor → exchange → consumidores)

1. En `http://localhost:4200`, iniciar sesión con la cuenta de Azure AD.
2. Agregar productos al carrito, ir a **Carrito** y pulsar **Continuar compra**. Aparece el número de pedido.
3. En los logs:
   - `venta-carrito`: `[PRODUCTOR] Pedido ... publicado` y `[TICKET] Comprobante generado`
   - `productos-catalogo`: `[STOCK] OK pedido ...: stock descontado -> ACK`
4. En el dashboard (`http://IP:15672`), pestaña **Queues and Streams**: las colas muestran el mensaje entrando y saliendo.

### 2. Mensaje fallido que termina en la DLQ

1. En el dashboard: **Exchanges** → `pedidos.exchange` → **Publish message**.
2. Completar:
   - **Routing key**: `pedido.creado`
   - **Properties**: `content_type` = `application/json`
   - **Payload** (producto que no existe):
     ```json
     {"pedidoId":"DEMO-1","usuarioId":"demo","total":9980,
      "items":[{"productoId":999,"nombreProducto":"No existe","cantidad":2,"precioUnitario":4990}]}
     ```
3. Pulsar **Publish message**. En el log de `productos-catalogo`:
   - `[STOCK] Error NO recuperable ... -> NACK sin reencolar -> DLQ`
   - `[DLQ] Mensaje en cuarentena | cola origen: pedidos.stock.queue | motivo: rejected`

Otra variante: publicar el texto `hola` como payload (mensaje inválido) y llega a las dos DLQ.

### 3. Microservicio administrador de RabbitMQ

1. Obtener el JWT: en el navegador con la sesión iniciada, `F12` → **Network** → cualquier llamada al backend → **Headers** → copiar el valor de `Authorization` sin la palabra `Bearer`.
2. En Postman, importar `back-app/postman/Pedidos360-backend.postman_collection.json` y editar las variables de la colección:

   | Variable | Valor |
   |---|---|
   | `catalogoUrl` | `http://IP:8081` |
   | `carritoUrl` | `http://IP:8082` |
   | `adminUrl` | `http://IP:8083` |
   | `token` | el JWT copiado |

3. Ejecutar en orden: crear cola con DLQ, crear exchange, crear binding. Mostrar que aparecen en el dashboard.
4. Mostrar una validación: crear una cola con `"nombre": ""` responde **400** con el detalle del error.
5. Eliminar el binding, el exchange y la cola. Mostrar que desaparecen del dashboard.

### 4. Código que respalda la pauta

| Qué mostrar | Archivo |
|---|---|
| Nombres centralizados | `back-app/venta-carrito/src/main/resources/application.yml` (bloque `pedidos360.rabbitmq`) |
| Beans Queue, Exchange y Binding | `config/RabbitMQConfig.java` de catálogo y de carrito |
| Consumidor con ACK/NACK manual | `messaging/pedidos/StockPedidoConsumer.java` (catálogo) |
| Registro de la DLQ en logs | `messaging/dlq/DlqAuditoriaConsumer.java` |
| API REST del administrador | `rabbitmq-admin/.../controller/RabbitAdminController.java` |
| Lógica encapsulada en un servicio | `rabbitmq-admin/.../service/RabbitAdminService.java` |
| Validación de entrada | `rabbitmq-admin/.../dto/CrearColaRequest.java` |

---

## B. Todo en el computador (plan B)

Requisitos: Docker Desktop abierto, Java 17 o superior, Maven y Node.

### 1. RabbitMQ y MySQL

Desde la raíz del repositorio:

```bash
docker compose up -d
docker compose ps        # los dos contenedores deben estar "Up"
```

### 2. Los tres microservicios

Una terminal por cada uno. La terminal queda ocupada mostrando los logs; está listo cuando aparece `Started ...Application`.

```bash
cd back-app/productos-catalogo
mvn spring-boot:run
```

```bash
cd back-app/venta-carrito
mvn spring-boot:run
```

```bash
cd back-app/rabbitmq-admin
mvn spring-boot:run
```

Se detienen con `Ctrl + C`.

### 3. Frontend

`front-app/src/environments/environment.development.ts` debe apuntar a `localhost` (es el valor que viene en el repositorio):

```ts
catalogoUrl: 'http://localhost:8081',
carritoUrl: 'http://localhost:8082',
```

```bash
cd front-app
npm install        # solo la primera vez
npm start
```

Abrir `http://localhost:4200`. La demostración es la misma de arriba cambiando `IP` por `localhost`, y los logs se ven directamente en las terminales de cada microservicio.

### 4. Apagar

`Ctrl + C` en cada terminal y luego, en la raíz:

```bash
docker compose down
```

---

## Actualizar la EC2 después de un cambio

Sin recrear la instancia: hacer `git push`, entrar por SSH y ejecutar:

```bash
cd /opt/pedidos360 && sudo git pull && sudo bash infraestructura/desplegar.sh
```

---

## Si algo falla

| Síntoma | Qué revisar |
|---|---|
| `terraform apply` dice que las credenciales no son válidas o expiraron | Reiniciar el laboratorio, copiar las credenciales nuevas y repetir `source credenciales.sh` |
| `terraform apply` falla con `InvalidKeyPair.NotFound` | El key pair no existe en la cuenta: usar `-var "key_name=vockey"` |
| Las direcciones de la EC2 no cargan después de 10 minutos | Entrar por SSH y ejecutar `sudo cat /var/log/arranque.log` (al final debe decir `ARRANQUE COMPLETO`) y `docker ps` (deben verse 5 contenedores) |
| Dejó de cargar después de cambiar de red | La IP propia cambió: repetir `terraform apply` |
| Un microservicio se reinicia en la EC2 | `docker logs productos-catalogo` (o el que corresponda) muestra el error |
| El frontend muestra errores de CORS | La URL del frontend debe ser `http://localhost:4200` |
| El login vuelve con error de `redirect_uri` | Abrir el frontend exactamente en `http://localhost:4200`, que es la URL registrada en Azure AD |
| El backend responde 401 | No hay sesión iniciada o el JWT de Postman venció (dura cerca de 1 hora): copiar uno nuevo |
| En local aparece `PRECONDITION_FAILED - inequivalent arg` | Quedó una cola antigua con otra configuración: eliminarla en el dashboard y reiniciar el microservicio |
| En local un puerto está ocupado | Hay otro proceso usando 8081, 8082, 8083 o 4200: cerrar la terminal anterior |
