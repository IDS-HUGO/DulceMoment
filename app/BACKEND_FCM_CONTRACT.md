# Backend Contract for FCM Push Integration

This document defines the backend contract required by the Android app to support remote push notifications (FCM) for customer and seller order updates.

## 1) Required Endpoint

### Register or refresh device token

- Method: `POST`
- Path: `/api/v1/notifications/device-token`
- Auth: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`

Request body:

```json
{
  "token": "fcm_device_token_string",
  "platform": "android"
}
```

Success response (example):

```json
{
  "ok": true,
  "message": "Device token registered"
}
```

Backend behavior:
- If `(user_id, token)` already exists, update `last_seen_at`.
- If token exists for another user (shared phone with new login), reassign or deactivate old mapping.
- Keep only active tokens for delivery.

## 2) Suggested Database Table

```sql
CREATE TABLE IF NOT EXISTS device_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token TEXT NOT NULL,
  platform VARCHAR(20) NOT NULL DEFAULT 'android',
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, token)
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_token ON device_tokens(token);
```

## 3) Delivery Events

Send push notifications on these events:

1. `order_created`
- Target: seller user(s)
- Message: new order received

2. `payment_confirmed`
- Target: customer and seller
- Message: payment approved / order confirmed

3. `order_status_changed`
- Target: customer
- Message: status changed (`in_oven`, `decorating`, `on_the_way`, `delivered`)

## 4) FCM Payload Contract

Use FCM HTTP v1 API and include both `notification` and `data` blocks.

```json
{
  "message": {
    "token": "FCM_DEVICE_TOKEN",
    "notification": {
      "title": "Pedido pagado",
      "body": "El pedido #123 fue pagado y está confirmado."
    },
    "data": {
      "title": "Pedido pagado",
      "body": "El pedido #123 fue pagado y está confirmado.",
      "order_id": "123",
      "event_type": "payment_confirmed"
    },
    "android": {
      "priority": "high",
      "notification": {
        "channel_id": "dulce_order_updates"
      }
    }
  }
}
```

Required `data` keys used by app:
- `title`
- `body`
- `order_id` (numeric string)

Optional:
- `event_type`

## 5) Invalid Token Handling

On FCM send response:
- If response indicates invalid/unregistered token, set `is_active = false` for that token.
- Do not retry invalid tokens in future deliveries.

## 6) App Side Integration Already Implemented

The app already includes:
- Firebase messaging service (`DulceFirebaseMessagingService`) for background/terminated delivery handling.
- Token sync helper (`PushTokenRegistrar`) that calls `/api/v1/notifications/device-token`.
- Session-level token persistence and sync de-duplication.
- Notification deep-link to order detail using `order_id`.

## 7) Environment Note

To receive real FCM pushes, Android project must include:
- `app/google-services.json`

Without that file, compile can still pass but real FCM delivery will not be operational.

### Android setup checklist

1. In Firebase Console, register Android app package: `com.example.dulcemoment`.
2. Download `google-services.json`.
3. Place it at: `app/google-services.json`.
4. Rebuild the app.

Note:
- This project applies `com.google.gms.google-services` only if `app/google-services.json` exists, so local builds remain stable before Firebase credentials are added.
