package io.github.yemyatthu1990.sampleapm

import android.app.Application
import android.content.Context
import android.util.Log
import io.github.yemyatthu1990.apm.APMClient
import io.github.yemyatthu1990.apm.MonitoringOptions
import io.github.yemyatthu1990.apm.UploadListener
import kotlinx.coroutines.*
import org.json.JSONObject

class SampleAPMApplication : Application() {
    private val applicationScope  = CoroutineScope(SupervisorJob()+Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()
        System.out.println(System.currentTimeMillis())
        applicationScope.launch {
            val apmClient = withContext(Dispatchers.IO) {
                initializeAPMClient(applicationContext)
            }

            System.out.println(apmClient.sessionId);
            var jsonString = "{\"name\":\"John\", \"age\":30, \"car\":null}"
            var jsonObject = JSONObject(jsonString)
            println(jsonObject.toString())
        }
    }

    private fun initializeAPMClient(context: Context): APMClient {

        val monitoringOptions = MonitoringOptions();
        monitoringOptions.isAutoUploadEnabled = true;
        monitoringOptions.isCrashReportEnabled = true;
        monitoringOptions.uploadListener = object : UploadListener {
            override fun onUploadFinished() {
                Log.d(this.javaClass.toString(), "uploading finished");
            }

        }

        return APMClient.Builder(context, "demo_client_id", "demo_secret_key")
                    .setMonitoringOptions(monitoringOptions)
                    .build()
    }
}