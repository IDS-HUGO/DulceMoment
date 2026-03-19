package com.example.dulcemoment.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DulceNotifier.ACTION_MARK_READ) {
            val notificationId = intent.getIntExtra(DulceNotifier.EXTRA_NOTIFICATION_ID, -1)
            if (notificationId > 0) {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }
    }
}
