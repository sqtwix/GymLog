package com.example.gymlogapp.utils

import android.content.Context

class Preferences(context: Context) : TokenStore {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    override fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val PREFS_NAME = "gymlog_prefs"
        const val KEY_TOKEN = "jwt_token"
    }
}
