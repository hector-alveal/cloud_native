# Cambios agregados: login real con Azure AD (MSAL) + conexión a los microservicios

Este proyecto ya traía el diseño (páginas, componentes, carrito) hecho por tu compañero.
Se le agregó lo que faltaba para cumplir la pauta: login real con Azure AD y llamadas
HTTP reales a los microservicios `productos-catalogo` y `venta-carrito` a través del
API Gateway.

## 1. Instalar dependencias nuevas

```bash
npm install
```

Esto instalará `@azure/msal-angular` y `@azure/msal-browser`, que se agregaron a
`package.json`.

## 2. Completar los datos reales (OBLIGATORIO antes de correr)

Edita `src/environments/environment.development.ts` (y `environment.ts` para producción)
y reemplaza:

- `clientId`: el Application (client) ID de tu App Registration en Azure AD (guía 1.2.5).
- `tenantId`: el Directory (tenant) ID de tu Tenant (guía 1.2.3).
- `apiScopes`: el scope expuesto por tu API en la App Registration, normalmente
  `api://<clientId>/access_as_user`.
- `apiBaseUrl`: la URL de tu API Manager (AWS API Gateway), por ejemplo
  `https://abc123.execute-api.us-east-1.amazonaws.com/dev`.

## 3. Verificar la Redirect URI en Azure

En la App Registration, en "Authentication", agrega como Redirect URI (tipo SPA):

```
http://localhost:4200
```

Si no coincide exactamente con `redirectUri` del environment, el login va a fallar.

## 4. Verificar CORS en el API Gateway

El API Gateway debe permitir el origen `http://localhost:4200` (guía 1.1.4), o las
llamadas desde Angular serán bloqueadas por el navegador aunque el JWT sea válido.

## 5. Correr el proyecto

```bash
npm start
```

## Qué se agregó/modificó (resumen técnico)

- `src/app/auth-config.ts`: configuración de MSAL (instancia, guard, interceptor).
- `src/app/app.config.ts`: registra MSAL + HttpClient + MsalInterceptor globalmente.
- `src/app/app.routes.ts`: la ruta `/carrito` ahora usa `MsalGuard` (exige sesión).
- `src/app/pages/login/login.ts` y `.html`: botón "Iniciar sesión con Microsoft"
  (loginRedirect), reemplazando el login falso de usuario/contraseña.
- `src/app/components/navbar/*`: muestra el nombre del usuario logueado y botón de
  cerrar sesión.
- `src/app/app.ts`: procesa la respuesta de la redirección de Azure AD al volver del login.
- `src/app/services/producto.ts`: ahora usa `HttpClient` para llamar a
  `GET {apiBaseUrl}/api/productos` en vez de datos hardcodeados. El JWT se adjunta
  automáticamente vía `MsalInterceptor` (no hay que hacerlo a mano).
- `src/app/services/carrito.ts`: usa `HttpClient` contra `{apiBaseUrl}/api/carrito`,
  identificando al usuario con la cuenta activa de MSAL (`localAccountId`).
- `src/app/pages/catalogo/catalogo.ts` y `carrito.ts`: se adaptaron para trabajar con
  `Observable` (llamadas HTTP asíncronas) en vez de arrays en memoria, agregando
  estados de "cargando" y error básico.

## Pendiente de probar en conjunto con el backend

1. Que el login con Azure AD complete el flujo y redirija de vuelta a `/`.
2. Que `GET /api/productos` a través del Gateway devuelva los productos reales
   (no los del array `PRODUCTOS` que ya no se usa).
3. Que agregar un producto al carrito lo persista en `venta-carrito` (revisa la
   base de datos o el endpoint `GET /api/carrito/{usuarioId}` directamente).
4. Que sin sesión iniciada, entrar a `/carrito` redirija automáticamente al login
   (eso lo hace `MsalGuard`).
