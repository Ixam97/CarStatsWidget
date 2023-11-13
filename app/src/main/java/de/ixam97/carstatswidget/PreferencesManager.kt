package de.ixam97.carstatswidget

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    val base = "de.ixam97.carstatswidget"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(base, Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString("${base}_$key", value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString("${base}_$key", defaultValue) ?: defaultValue
    }

    fun saveBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("${base}_$key", value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean("${base}_$key", defaultValue) ?: defaultValue
    }
}