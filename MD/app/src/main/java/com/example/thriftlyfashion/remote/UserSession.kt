package com.example.thriftlyfashion.remote

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREF_NAME = "UserSession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "auth_token"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var userId: Int?
        get() = sharedPreferences.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
        set(value) = sharedPreferences.edit().putInt(KEY_USER_ID, value ?: -1).apply()

    var authToken: String?
        get() = sharedPreferences.getString(KEY_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_TOKEN, value).apply()

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
