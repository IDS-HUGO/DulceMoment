package com.example.dulcemoment.notifications

import android.content.Context
import com.example.dulcemoment.data.network.DeviceTokenRequest
import com.example.dulcemoment.data.network.RetrofitClient
import com.example.dulcemoment.data.repo.SessionStore

object PushTokenRegistrar {
    suspend fun syncToken(context: Context, token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(IllegalArgumentException("Token FCM inválido"))

        val sessionStore = SessionStore(context.applicationContext)
        sessionStore.saveFcmToken(token)

        val user = sessionStore.loadUser() ?: return Result.success(Unit)
        if (sessionStore.isFcmTokenSynced(user.id, token)) {
            return Result.success(Unit)
        }

        val accessToken = sessionStore.loadToken() ?: return Result.success(Unit)
        val authHeader = "Bearer $accessToken"

        return runCatching {
            RetrofitClient.api.registerDeviceToken(
                authHeader = authHeader,
                body = DeviceTokenRequest(token = token),
            )
            sessionStore.markFcmTokenSynced(user.id, token)
        }
    }
}
