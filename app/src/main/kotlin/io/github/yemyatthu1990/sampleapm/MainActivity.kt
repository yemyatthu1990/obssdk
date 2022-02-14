package io.github.yemyatthu1990.sampleapm

import android.net.UrlQuerySanitizer
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.yemyatthu1990.sampleapm.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//            .enqueue(object: Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    e.printStackTrace()
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    println(response?.body?.string())
//                }
//
//            })
        val task = networktask()
        task.execute()
    }

    class networktask : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg params: Void?): String? {
            val okHttpClient = OkHttpClient()
                .newBuilder()
                .build()

            println("OK HTTP NETWORKINTERCEPTOR SIZE: "+okHttpClient.networkInterceptors?.size)
            println(okHttpClient.interceptors.size)
            val response =  okHttpClient.newCall(Request.Builder()
                .url("https://gist.githubusercontent.com/rdsubhas/ed77e9547d989dabe061/raw/6d7775eaacd9beba826e0541ba391c0da3933878/gnc-js-api")
                .build())
                .execute();
            println("NEW OK HTTP NETWORKINTERCEPTOR SIZE: "+okHttpClient.networkInterceptors?.size)
            return response?.body?.string()
        }

        override fun onPostExecute(result: String?) {
            println(result)
        }
    }
}