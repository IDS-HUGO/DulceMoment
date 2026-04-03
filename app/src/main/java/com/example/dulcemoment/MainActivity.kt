package com.example.dulcemoment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.dulcemoment.ui.DulceNotifier
import com.example.dulcemoment.ui.theme.DulceMomentTheme
import com.example.dulcemoment.ui.DulceApp
import com.example.dulcemoment.notifications.PushTokenRegistrar
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val orderOpenRequest = MutableStateFlow<Int?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderOpenRequest.value = extractOrderIdFromIntent()
        DulceNotifier(this).createChannelIfNeeded()
        requestNotificationPermissionIfNeeded()
        requestAndSyncFcmToken()
        enableEdgeToEdge()
        setContent {
            val pendingOrderId by orderOpenRequest.collectAsStateWithLifecycle()
            DulceMomentTheme {
                DulceApp(
                    openOrderIdRequest = pendingOrderId,
                    onOrderRequestConsumed = { orderOpenRequest.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        orderOpenRequest.value = extractOrderIdFromIntent()
    }

    private fun extractOrderIdFromIntent(): Int? {
        val id = intent?.getIntExtra(DulceNotifier.EXTRA_OPEN_ORDER_ID, -1) ?: -1
        return if (id > 0) id else null
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestAndSyncFcmToken() {
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        PushTokenRegistrar.syncToken(applicationContext, token)
                    }
                }
        }
    }
}