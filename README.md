# DulceMoment - App móvil profesional

Este repositorio contiene la app Android (cliente + tienda) conectada a una API FastAPI separada.

## API separada
- Ubicación: `D:\UNIVERSIDAD\DulceMomentAPI\backend-fastapi`
- Base URL usada por la app (emulador Android): `http://10.0.2.2:8002/`

## Funcionalidades implementadas
- Login y Register con roles `customer` y `store`.
- Sesión segura en app con token JWT persistido en `EncryptedSharedPreferences`.
- Refresh token automático en segundo plano ante `401`.
- Logout con revocación de sesión en API.
- Opción de cerrar sesión en todos los dispositivos.
- Cliente:
	- selección de pastelitos y compra,
	- personalización por ingredientes, tamaño, forma, sabor y color,
	- precios calculados por personalización,
	- pago con tarjeta,
	- logística/seguimiento del pedido por etapas.
- Tienda:
	- alta de productos,
	- carga de imagen por URL usando Cloudinary (desde API),
	- control de inventario y marcado de agotado,
	- actualización de etapas del pedido.
- Notificaciones por etapas visibles en app y emitidas desde API:
	- "Tu pastel entró al horno"
	- "Estamos decorando tu pastel"
	- "¡Tu pedido va en camino!"
	- "Entregado"

## Cuentas demo
- Cliente: `cliente@dulce.com` / `123456`
- Tienda: `tienda@dulce.com` / `123456`

## Ejecución
1. Levanta la API en el proyecto separado (puerto `8002`).
2. En esta raíz, compila/ejecuta Android:
	 - `./gradlew :app:assembleDebug`
3. Abre en Android Studio y corre en emulador/dispositivo.

## Probar contra API desplegada
- El endpoint ahora se toma desde `BuildConfig.API_BASE_URL`.
- Para apuntar a una API remota al compilar:
	- `./gradlew :app:assembleDebug -PAPI_BASE_URL=https://tu-api.com/`
	- `./gradlew :app:assembleRelease -PAPI_BASE_URL=https://tu-api.com/`
- Si no pasas propiedad:
	- `debug` usa `http://10.0.2.2:8002/`
	- `release` usa `https://tu-api-desplegada.com/` (placeholder)

## Nota de seguridad
- Las operaciones sensibles (crear producto, cambiar estado de pedido, pagos, listar pedidos privados) usan `Authorization: Bearer <JWT>`.
