package com.example.cafelabiru

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ADDRESS = "user_address"
        private const val KEY_USER_ADDRESS_DETAIL = "user_address_detail"
        private const val KEY_USER_PHONE = "user_phone"
    }

    fun saveUserData(userName: String, email: String, address: String = "", addressDetail: String = "", phone: String = "") {
        prefs.edit().apply {
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ADDRESS, address.ifEmpty { "Simpang baru, Tampan" })
            putString(KEY_USER_ADDRESS_DETAIL, addressDetail.ifEmpty { "Simpang baru, Tampan, Pekanbaru City, Riau, Indonesia" })
            putString(KEY_USER_PHONE, phone.ifEmpty { "+62 812-3456-7890" })
            apply()
        }
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "User") ?: "User"
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserAddress(): String = prefs.getString(KEY_USER_ADDRESS, "Simpang baru, Tampan") ?: "Simpang baru, Tampan"
    fun getUserAddressDetail(): String = prefs.getString(KEY_USER_ADDRESS_DETAIL, "Simpang baru, Tampan, Pekanbaru City, Riau, Indonesia") ?: "Simpang baru, Tampan, Pekanbaru City, Riau, Indonesia"
    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "+62 812-3456-7890") ?: "+62 812-3456-7890"

    fun updateAddress(address: String, addressDetail: String) {
        prefs.edit().apply {
            putString(KEY_USER_ADDRESS, address)
            putString(KEY_USER_ADDRESS_DETAIL, addressDetail)
            apply()
        }
    }

    fun updatePhone(phone: String) {
        prefs.edit().apply {
            putString(KEY_USER_PHONE, phone)
            apply()
        }
    }
}