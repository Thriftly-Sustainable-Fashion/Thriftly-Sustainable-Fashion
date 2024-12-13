package com.example.thriftlyfashion.remote

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("jwt_pref", Context.MODE_PRIVATE)

    // Token
    fun saveToken(token: String) {
        sharedPreferences.edit().putString("key_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("key_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("key_token").apply()
    }

    // User ID
    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("key_user_id", userId).apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("key_user_id", 0)
    }

    fun clearUserId() {
        sharedPreferences.edit().remove("key_user_id").apply()
    }

    // User Name
    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("key_user_name", name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("key_user_name", null)
    }

    fun clearUserName() {
        sharedPreferences.edit().remove("key_user_name").apply()
    }

    // User Email
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("key_user_email", email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("key_user_email", null)
    }

    fun clearUserEmail() {
        sharedPreferences.edit().remove("key_user_email").apply()
    }

    // User Phone Number
    fun saveUserPhoneNumber(phoneNumber: String) {
        sharedPreferences.edit().putString("key_user_phone_number", phoneNumber).apply()
    }

    fun getUserPhoneNumber(): String? {
        return sharedPreferences.getString("key_user_phone_number", null)
    }

    fun clearUserPhoneNumber() {
        sharedPreferences.edit().remove("key_user_phone_number").apply()
    }

    // Clear all data
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
