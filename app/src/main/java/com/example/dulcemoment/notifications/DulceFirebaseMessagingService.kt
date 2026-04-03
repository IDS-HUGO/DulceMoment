package com.example.dulcemoment.notifications

import com.example.dulcemoment.ui.DulceNotifier
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DulceFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            PushTokenRegistrar.syncToken(applicationContext, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Actualización de pedido"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Tienes una actualización en DulceMoment"

        val orderId = message.data["order_id"]?.toIntOrNull()
        val notificationId = message.messageId?.hashCode()
            ?: orderId
            ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val notifier = DulceNotifier(applicationContext)
        notifier.createChannelIfNeeded()
        notifier.notifyOrderUpdate(
            title = title,
            body = body,
            notificationId = notificationId,
            orderId = orderId,
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
