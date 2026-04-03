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
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue
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
}