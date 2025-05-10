package com.example.workhive.session

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {

    private val pref = context.getSharedPreferences("Test App", Context.MODE_PRIVATE)

    fun saveUserName(userName: String) {
        pref.edit().putString("user_name", userName).apply()
    }

    fun getUserName(): String = pref.getString("user_name", "") ?: ""

    fun saveToken(token: String) {
        pref.edit().putString("token", token).apply()
    }

}