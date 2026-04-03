# DulceMoment App (Android)

Aplicación móvil para clientes y vendedores de DulceMoment.

## Características principales
- Catálogo visual y personalización de productos
- Pedido solo se confirma tras pago exitoso (flujo seguro)
- Desglose de subtotal, IVA y descuentos en checkout
- Cancelación y reactivación de pedidos en borrador/cancelados
- Historial de pedidos y pagos
- Dashboard de ventas para el vendedor
- Notificaciones automáticas (push/email) en cada cambio de estado
- Soporte para cupones/descuentos
- UI/UX profesional, responsiva y moderna

## Requisitos
- Android Studio Giraffe o superior
- SDK mínimo: 24 (Android 7.0)

## Instalación y ejecución
1. Clona el repositorio:
   ```bash
   git clone https://github.com/IDS-HUGO/DulceMoment.git
   cd DulceMoment/app
   ```
2. Abre el proyecto en Android Studio.
3. Configura la URL del backend en los archivos de red si es necesario.
4. Ejecuta en un emulador o dispositivo físico.

## Flujo de usuario
1. Registro/login.
2. Explora el catálogo y personaliza tu pedido.
3. Crea el pedido (queda en borrador).
4. Realiza el pago (incluye IVA y cupones).
5. El pedido se confirma y el vendedor es notificado.
6. Puedes cancelar/reactivar pedidos antes de pagar.
7. Consulta historial, estado y detalles de cada pedido.

## Mejores prácticas UI/UX
- Mensajes claros y amigables para cada estado.
- Diálogos de confirmación en acciones críticas.
- Indicadores de carga y manejo de errores de red.
- Colores e íconos para diferenciar estados.
- Consistencia visual y responsividad.

## Contacto
Para soporte o sugerencias: hugo8@ids-hugo.com

## Integración Backend de Notificaciones Push
Para implementar el backend compatible con la app (registro de token FCM, payload y eventos de envío), revisa:

- `app/BACKEND_FCM_CONTRACT.md`
