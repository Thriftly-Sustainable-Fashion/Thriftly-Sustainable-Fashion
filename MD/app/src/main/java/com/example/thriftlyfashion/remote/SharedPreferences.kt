package com.example.thriftlyfashion.remote

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("jwt_pref", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("key_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("key_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("key_token").apply()
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("key_user_id", userId).apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("key_user_id", 0)
    }

    fun clearUserId() {
        sharedPreferences.edit().remove("key_user_id").apply()
    }
}
