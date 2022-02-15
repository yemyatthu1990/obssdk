package io.github.yemyatthu1990.sampleapm

import android.app.Application
import io.github.yemyatthu1990.apm.Agent
import kotlinx.coroutines.*

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
        Agent.start(context, "http://10.228.213.101:9411/api/v2/spans")
        return Agent.getInstance()
    }
}