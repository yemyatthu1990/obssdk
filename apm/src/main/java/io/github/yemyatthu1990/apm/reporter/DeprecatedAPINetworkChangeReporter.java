package io.github.yemyatthu1990.apm.reporter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import androidx.annotation.Nullable;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class DeprecatedAPINetworkChangeReporter extends BroadcastReceiver {
    private ConnectivityManager connectivityManager;
    private Tracer tracer;
    public DeprecatedAPINetworkChangeReporter( ConnectivityManager connectivityManager, Tracer tracer) {
        this.connectivityManager = connectivityManager;
        this.tracer = tracer;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Network network = this.connectivityManager.getActiveNetwork();
                traceNetwork(network, network!=null);

            } else {
                NetworkInfo networkInfo = this.connectivityManager.getActiveNetworkInfo();
                traceNetwork(networkInfo, networkInfo != null);
            }
        }
    }

    private void traceNetwork(@Nullable Network network, boolean isAvailable) {
        String networkType = "unknown";
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                networkType = "cellular";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkType = "wifi";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                networkType = "vpn";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB)) {
                networkType = "usb";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                networkType = "bluetooth";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                networkType = "ethernet";
            }
        }

            tracer.spanBuilder("network.change")
                    .setAttribute("network.available", isAvailable)
                    .setAttribute(SemanticAttributes.NET_TRANSPORT, networkType)
                    .startSpan()
                    .end();

    }

    private void traceNetwork(@Nullable NetworkInfo info, boolean isAvailable) {
        String networkType = "unknown";
        if (info != null) {
            switch (info.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    networkType = "cellular";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    networkType = "wifi";
                    break;
                case ConnectivityManager.TYPE_VPN:
                    networkType = "vpn";
                    break;
                case ConnectivityManager.TYPE_BLUETOOTH:
                    networkType = "bluetooth";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    networkType = "ethernet";
                    break;
                default:
                    networkType = "unknown";
                    break;
            }

        }

        tracer.spanBuilder("network.change")
                .setAttribute("network.available", isAvailable)
                .setAttribute(SemanticAttributes.NET_TRANSPORT, networkType)
                .startSpan()
                .end();
    }
}
