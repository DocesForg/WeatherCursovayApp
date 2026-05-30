package com.docesforg.bura.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_AUTH_TOKEN = "auth_token"
private const val KEY_ACCOUNT_ID = "account_id"
private const val KEY_ACCOUNT_PASSWORD = "account_password"

class AuthSessionRepository(private val prefs: SharedPreferences) {
    private val _loggedIn = MutableStateFlow(hasValidSession())
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()

    init {
        if (!_loggedIn.value) {
            clearSession()
        }
    }

    fun isLoggedIn(): Boolean = _loggedIn.value

    fun authToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)?.takeIf { it.isNotBlank() }
    fun accountPassword(): String = prefs.getString(KEY_ACCOUNT_PASSWORD, "") ?: ""

    fun accountId(): Long? {
        val value = prefs.getLong(KEY_ACCOUNT_ID, -1L)
        return if (value >= 0) value else null
    }

    fun saveSession(token: String, accountId: Long, password: String) {
        prefs.edit {
            putString(KEY_AUTH_TOKEN, token)
            putLong(KEY_ACCOUNT_ID, accountId)
            putString(KEY_ACCOUNT_PASSWORD, password)
        }
        _loggedIn.value = true
    }

    fun savePassword(password: String) {
        prefs.edit { putString(KEY_ACCOUNT_PASSWORD, password) }
    }

    fun clearSession() {
        prefs.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_ACCOUNT_ID)
            remove(KEY_ACCOUNT_PASSWORD)
        }
        _loggedIn.value = false
    }

    private fun hasValidSession(): Boolean = !prefs.getString(KEY_AUTH_TOKEN, null).isNullOrBlank()
            && prefs.getLong(KEY_ACCOUNT_ID, -1L) >= 0
}
