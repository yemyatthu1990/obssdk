package io.github.yemyatthu1990.apm.collectors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.RequiresPermission;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;


public class NetworkMetricCollector extends MetricsCollector {

    // TELEPHONY
    private static final String carrierKey = "network.carrier.name";
    private static final String carrierIsoCodeKey = "network.carrier.iso";
    private static final String carrierCountryCodeKey = "network.carrier.country";
    private static final String carrierNetworkCodeKey = "network.carrier.network";
    private static final String carrierNetworkType = "network.carrier.type";
    private static final String proxyAddressKey = "network.proxy";
    private static final String telephonyDeviceIdKey = "network.telephony.id";
    private static final String telephonyIMSIKey = "network.telephony.imsi";
    private static final String simStateKey = "network.sim.state";
    private static final String simIsRoamingKey = "network.sim.roaming";
    // WIFI
    private static final String wifiIPKey = "network.wifi.ip";
    private static final String wifiSSIDKey = "network.wifi.ssid";
    private static final String wifiBssidKey = "network.wifi.bssid";
    private static final String mobileDbmKey = "network.mobile.dbm";

    private final Context context;

    public NetworkMetricCollector(Context context) {
        super();
        this.context = context;
    }

    public String getProxyDetails() {
        String proxyAddress = "";
        try {
            proxyAddress = System.getProperty("http.proxyHost");
        } catch (Exception ex) {
            return "";
        }
        return proxyAddress;
    }

    public ConcurrentMap<String, String> getNetworkMetrics() {
        // client app related info
        try {
            this.getCarrierName(context);
            this.getIMSI(context);
            this.put(wifiIPKey, getWifiIp(context));
            this.put(wifiSSIDKey, getWifiSSID(context));
            this.put(wifiBssidKey, getWifiBSSID(context));
            this.put(mobileDbmKey, getSignalStrength(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map();
    }

    @SuppressLint("MissingPermission")
    private String getSignalStrength(Context context) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                List<String> mobileDbms = new ArrayList<>();
                List<CellSignalStrength> signalStrengths = manager.getSignalStrength().getCellSignalStrengths();
                for (CellSignalStrength cellSignalStrength : signalStrengths) {
                    mobileDbms.add(String.valueOf(cellSignalStrength.getDbm()));
                }
                return TextUtils.join(",", mobileDbms);
            } else {
                return getSignalStrengthPreQ(context);
            }
        } catch (Exception ignored) {
            return "error";
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private String getSignalStrengthPreQ(Context context) throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
                if (cellInfos != null) {
                    List<String> mobileDbms = new ArrayList<>();
                    for (int i = 0; i < cellInfos.size(); i++) {
                        if (cellInfos.get(i).isRegistered()) {
                            if (cellInfos.get(i) instanceof CellInfoWcdma) {
                                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
                                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                mobileDbms.add(String.valueOf(cellSignalStrengthWcdma.getDbm()));
                            } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                                CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                                mobileDbms.add(String.valueOf(cellSignalStrengthGsm.getDbm()));
                            } else if (cellInfos.get(i) instanceof CellInfoLte) {
                                CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                                mobileDbms.add(String.valueOf(cellSignalStrengthLte.getDbm()));
                            } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
                                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                                mobileDbms.add(String.valueOf(cellSignalStrengthCdma.getDbm()));
                            }
                        }
                    }
                    return TextUtils.join(",", mobileDbms);
                }
                return "";
            } catch (Exception ignored) {
                return "error";
            }
        } else {
            return "disabled";
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void getCarrierName(Context context) {

        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkType = "disabled";
            if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                int nType = manager.getNetworkType();
                switch (nType) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        networkType = "1xRTT";
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        networkType = "CDMA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        networkType = "EDGE";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        networkType = "eHRPD";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        networkType = "EVDO rev. 0";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        networkType = "EVDO rev. A";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        networkType = "EVDO rev. B";
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        networkType = "GPRS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        networkType = "HSDPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        networkType = "HSPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        networkType = "HSPA+";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        networkType = "HSUPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        networkType = "iDen";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        networkType = "LTE";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        networkType = "UMTS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        networkType = String.valueOf(nType);
                        break;
                    default:
                        networkType = "";
                }
            }
            this.getProxy(context);
            this.put(carrierKey, manager.getNetworkOperatorName());
            this.put(carrierIsoCodeKey, manager.getNetworkCountryIso());
            this.put(carrierNetworkType, networkType);

            String nOperator = this.getPrivateData("gsm.operator.numeric");
            String mcc, mnc;
            if (nOperator != null && nOperator.length() > 3) {
                mcc = nOperator.substring(0, 3).replace(",", "");
                mnc = nOperator.substring(3).replace(",", "");

            } else {
                mcc = mnc = "";
            }
            this.put(carrierCountryCodeKey, mcc);
            this.put(carrierNetworkCodeKey, mnc);
            this.put(simStateKey, this.getPrivateData("gsm.sim.state"));
            this.put(simIsRoamingKey, this.getPrivateData("gsm.operator.isroaming"));


            if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && Build.VERSION.SDK_INT < 29) {
                if (Build.VERSION.SDK_INT < 26) {
                    this.put(telephonyDeviceIdKey, manager.getDeviceId());
                } else {
                    if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                        this.put(telephonyDeviceIdKey, manager.getImei());
                    } else if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                        this.put(telephonyDeviceIdKey, manager.getMeid());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getProxy(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                ProxyInfo pi = connectivityManager.getDefaultProxy();
                this.put(proxyAddressKey, pi.getHost());
            } catch (Exception ignored) {
                this.put(proxyAddressKey, "");
            }
        } else {
            try {
                this.put(proxyAddressKey, getProxyDetails());
            } catch (Exception ignored) {
                this.put(proxyAddressKey, "");
            }
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void getIMSI(Context context) {

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && Build.VERSION.SDK_INT < 29) {
                if (manager.getSubscriberId() != null) {
                    this.put(telephonyIMSIKey, manager.getSubscriberId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getWifiIp(Context context) {

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED && wifiManager.getConnectionInfo() != null) {

                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

                // Convert little-endian to big-endian if needed
                if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                    ipAddress = Integer.reverseBytes(ipAddress);
                }

                byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

                String ipAddressString;
                try {
                    ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
                } catch (UnknownHostException ex) {
                    ipAddressString = "error";
                }

                return ipAddressString;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getWifiSSID(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = ((wifiInfo.getSSID() == null || wifiInfo.getSSID().isEmpty()) || "<unknown ssid>".equals(wifiInfo.getSSID())) ? "" : wifiInfo.getSSID();
                ssid = ssid.replaceAll("\"", "");
                return ssid;
            } else {
                return "disabled";
            }
        } catch (Exception e) {
            return "error";
        }
    }

    private String getWifiBSSID(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return ((wifiInfo.getBSSID() == null || wifiInfo.getBSSID().isEmpty()) || "00:00:00:00:00:00".equals(wifiInfo.getBSSID())) ? "" : wifiInfo.getBSSID();
            } else {

                return "disabled";
            }
        } catch (Exception e) {
            return "error";
        }
    }

    private String getPrivateData(String key) {
        try {
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            return (String) method.invoke(null, key);
        } catch (Exception e) {
            return "";
        }
    }
}

