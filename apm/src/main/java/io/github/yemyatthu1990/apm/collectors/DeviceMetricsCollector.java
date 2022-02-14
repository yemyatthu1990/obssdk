package io.github.yemyatthu1990.apm.collectors;

import static android.content.Context.UI_MODE_SERVICE;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.UUID;

public class DeviceMetricsCollector extends MetricsCollector{
    private final String deviceIDKey = "device.id";
    private final String deviceModelKey = "device.model";
    private final String deviceTypeKey = "device.type";
    private final String deviceBrandKey = "device.brand";
    private final String deviceABIKey = "device.ABI";
    private final String deviceManufactureKey = "device.manufacture";
    private Context context;
    public DeviceMetricsCollector(Context context) {
        this.context = context;
    }

    public Map<String, String> getDeviceMetrics() {
        this.put(deviceIDKey, getDeviceId(context));
        this.put(deviceModelKey, Build.MODEL);
        this.put(deviceTypeKey, getDeviceType(context));
        this.put(deviceBrandKey, Build.BRAND);
        this.put(deviceABIKey, TextUtils.join(",", Build.SUPPORTED_ABIS));
        this.put(deviceManufactureKey, Build.MANUFACTURER);
        return this.map();
    }

    @SuppressLint("HardwareIds")
    private String getDeviceId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch ( Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }

        // unable to fetch android id. create a pseudo device id
        return getPseudoDeviceId(context);
    }

    private String getPseudoDeviceId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("apm", Context.MODE_PRIVATE);
        if (sharedPreferences.contains(deviceIDKey)) {
            return sharedPreferences.getString(deviceIDKey, "");
        }
        else {

            String pseudoDeviceId =  UUID.randomUUID().toString().replace("[^a-zA-Z0-9]", "");
            sharedPreferences.edit().putString(deviceIDKey, pseudoDeviceId).apply();
            return pseudoDeviceId;
        }
    }

    private String getDeviceType(Context context) {

        try {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
            if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
                return "tv";
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }
        if (isTablet(context)) {
            return "tablet";
        } else {
            return "phone";
        }
    }

    private boolean isTablet(Context context) {
        try {
            return (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        } catch (Exception e) {
            return false;
        }
    }
}
