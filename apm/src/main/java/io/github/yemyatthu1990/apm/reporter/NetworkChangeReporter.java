package io.github.yemyatthu1990.apm.reporter;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import io.github.yemyatthu1990.apm.AppState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class NetworkChangeReporter extends ConnectivityManager.NetworkCallback implements AppState {
    private final AtomicReference<Boolean> shouldEmitChanges = new AtomicReference<>();
    private final AtomicReference<Boolean> hasActiveNetwork = new AtomicReference<>();
    private final ConnectivityManager cm;
    private final Tracer tracer;
    public NetworkChangeReporter(ConnectivityManager cm, Tracer tracer) {
        shouldEmitChanges.set(true);
        this.cm = cm;
        hasActiveNetwork.set(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
        this.tracer = tracer;
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        hasActiveNetwork.set(true);
        if (shouldEmitChanges.get()) {
            traceNetwork(network, true);
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        hasActiveNetwork.set(false);
        if (shouldEmitChanges.get()) {
            traceNetwork(network, false);
        }
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        hasActiveNetwork.set(false);
    }

    @Override
    public void onAppEnterBackground() {
        shouldEmitChanges.set(false);
    }

    @Override
    public void onAppEnterForeground() {
        shouldEmitChanges.set(true);
    }

    private void traceNetwork(Network network, boolean isAvailable) {
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(network);
        String networkType = "";
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
        if (shouldEmitChanges.get()) {
            tracer.spanBuilder("network.change")
                    .setAttribute("network.available", isAvailable)
                    .setAttribute(SemanticAttributes.NET_TRANSPORT, networkType)
                    .startSpan()
                    .end();
        }
    }
}
