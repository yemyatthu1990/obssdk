package io.github.yemyatthu1990.sampleapm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import io.github.yemyatthu1990.apm.Agent
import io.github.yemyatthu1990.apm.AndroidLog
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<AppCompatButton>(R.id.crashButton)
            .setOnClickListener { throw IllegalAccessException("Crash test") }
        findViewById<AppCompatButton>(R.id.anrButton)
            .setOnClickListener { while (true);}
        findViewById<AppCompatButton>(R.id.longRunningButton)
            .setOnClickListener {
                scope.launch {
                    runTask()
                }
            }
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .build()
        okHttpClient.newCall(Request.Builder()
             .url("https://gist.githubusercontent.com/rdsubhas/ed77e9547d989dabe061/raw/6d7775eaacd9beba826e0541ba391c0da3933878/gnc-js-api")
             .build())
             .enqueue(object: Callback {
                 override fun onFailure(call: Call, e: IOException) {
                 }

                 override fun onResponse(call: Call, response: Response) {
                     AndroidLog.d("mainactivity", response.body?.string())
                 }

             })

    }

    override fun onStop() {
        super.onStop()
        scope.cancel()
    }

    private fun runTask() {
        val trace = Agent.getInstance().startTracing("long running task", null)
        for (i in 0..3) {
            val childTrace = trace.addTrace("long running task number: $i", null)
            Thread.sleep(5000)
            childTrace.stopTracing()
        }
        trace.stopTracing();
    }

}