package io.github.yemyatthu1990.apm.collector;

import android.app.ActivityManager;
import android.content.Context;

public class MemoryInfo {

    private static ActivityManager activityManager;
    public static void init(Context context) {
        MemoryInfo.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public static double getTotalRam() {
        if (activityManager == null){
            throw new IllegalStateException("MemoryInfo must be initialized by calling MemoryInfo.init(Context context) before accessing Total Ram method");
        }
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.totalMem/ (double)1000000000;
    }
    public static double getUsageRam() {
        return 100 - ((getFreeRam() / (double)getTotalRam()) * 100.0);
    }
    public static double getFreeRam() {
        if (activityManager == null){
            throw new IllegalStateException("MemoryInfo must be initialized by calling MemoryInfo.init(Context context) before accessing Free Ram method");
        }
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.availMem / (double)1000000000;
    }

//    private String getTotalRAMOldAndroid() {
//        RandomAccessFile reader = null;
//        String load = null;
//        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
//        double totRam = 0;
//        double freeRam = 0;
//        JSONObject data = new JSONObject();
//
//        try {
//            reader = new RandomAccessFile("/proc/meminfo", "r");
//            load = reader.readLine();
//            // Get the Number value from the string
//            Pattern p = Pattern.compile("(\\d+)");
//            Matcher m = p.matcher(load);
//            String value = "";
//            String freeValue = "";
//            while (m.find()) {
//                value = m.group(1);
//            }
//            load = reader.readLine();
//            m = p.matcher(load);
//            while (m.find()) {
//                freeValue = m.group(1);
//            }
//            reader.close();
//            if (value != null) {
//                totRam = Double.parseDouble(value);
//            }
//            if (freeValue != null) {
//                freeRam = Double.parseDouble(freeValue);
//            }
//
//            double gb = totRam / 1000000;
//            double freeGb = freeRam / 1000000;
//            data.put("total", twoDecimalForm.format(gb));
//            data.put("free", twoDecimalForm.format(freeGb));
//        } catch (Exception e) {
//            InternalLogger.getInstance().error(e);
//        }
//        return data.toString();
//    }
}
