package io.github.yemyatthu1990.apm.collectors

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService


class NetworkMetricCollector: MetricsCollector() {

    private fun isConnectedToANetwork(context: Context): Boolean {
        return false
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//        val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            connectivityManager?.activeNetworkInfo
//        } else {
//            connectivityManager?.activeNetworkInfo
//        }
//        return activeNetwork != null && activeNetwork.is
    }
}