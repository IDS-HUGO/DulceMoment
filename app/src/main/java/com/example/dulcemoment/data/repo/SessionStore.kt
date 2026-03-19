package com.example.dulcemoment.data.repo

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.dulcemoment.data.local.UserEntity

class SessionStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "dulce_session_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveSession(user: UserEntity, token: String, refreshToken: String) {
        prefs.edit()
            .putInt("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_role", user.role)
            .putString("access_token", token)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun loadUser(): UserEntity? {
        val id = prefs.getInt("user_id", -1)
        if (id < 0) return null
        return UserEntity(
            id = id,
            name = prefs.getString("user_name", "") ?: "",
            email = prefs.getString("user_email", "") ?: "",
            password = "",
            role = prefs.getString("user_role", "customer") ?: "customer",
        )
    }

    fun loadToken(): String? = prefs.getString("access_token", null)

    fun loadRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun updateTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
