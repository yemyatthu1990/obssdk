package io.github.yemyatthu1990.apm.collectors

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import io.github.yemyatthu1990.apm.log.AndroidLog
import java.lang.Exception
import java.util.*

class DeviceMetricsCollector(private val context: Context): MetricsCollector() {
    private val deviceIDKey = "DEVICE_ID"
    init {
        this.put(deviceIDKey, getDeviceId(context))
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(context: Context): String {
        try {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {

        }

        // unable to fetch android id. create a pseudo device id
        return getPseudoDeviceId(context)
    }

    private fun getPseudoDeviceId(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("apm", Context.MODE_PRIVATE)
        return if (sharedPreferences?.contains(deviceIDKey) == true) {
            sharedPreferences.getString(deviceIDKey, "")?:""
        } else {
            val pseudoDeviceId =  UUID.randomUUID().toString().replace("[^a-zA-Z0-9]".toRegex(), "")
            sharedPreferences.edit().putString(deviceIDKey, pseudoDeviceId).apply()
            pseudoDeviceId
        }
    }
 }