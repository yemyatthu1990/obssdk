package io.github.yemyatthu1990.sampleapm

import android.app.Application
import android.content.Context
import android.util.Log
import io.github.yemyatthu1990.apm.Agent
import io.github.yemyatthu1990.apm.AgentConfiguration
import io.github.yemyatthu1990.apm.MonitoringOptions
import io.github.yemyatthu1990.apm.UploadListener
import kotlinx.coroutines.*
import org.json.JSONObject

class SampleAPMApplication : Application() {
    private val applicationScope  = CoroutineScope(SupervisorJob()+Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()
        initializeAPMClient(this)
        System.out.println(System.currentTimeMillis())
//        applicationScope.launch {
//            val apmClient = withContext(Dispatchers.IO) {
//                initializeAPMClient(this@SampleAPMApplication)
//            }
//            var jsonString = "{\"name\":\"John\", \"age\":30, \"car\":null}"
//            var jsonObject = JSONObject(jsonString)
//            println(jsonObject.toString())
//        }
    }

    private fun initializeAPMClient(context: Application): Agent {
        Agent.start(context)
        return Agent.getInstance()
    }
}