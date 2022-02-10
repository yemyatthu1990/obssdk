package io.github.yemyatthu1990.apm

import android.Manifest
import android.content.Context
import android.util.Log
import io.github.yemyatthu1990.apm.MonitoringOptions
import androidx.annotation.RequiresPermission
import io.github.yemyatthu1990.apm.APMClient
import java.util.*

public class APMClient private constructor(
    context: Context,
    clientID: String,
    secretToken: String,
    monitoringOptions: MonitoringOptions?,
    public val sessionId: String
) {

    init {
        Log.d(this.javaClass.toString(), "apm client initialized")
    }

    public class Builder @RequiresPermission(Manifest.permission.INTERNET) constructor(
        context: Context?,
        clientId: String,
        secretToken: String
    ) {
        private val context: Context
        private val clientId: String
        private val secretToken: String
        private val sessionId: String
        private var monitoringOptions: MonitoringOptions? = null

        /**
         * Set monitoring options for APM Client
         * @param monitoringOptions Monitoring options for APM Client
         * @return Builder
         */
        fun setMonitoringOptions(monitoringOptions: MonitoringOptions?): Builder {
            this.monitoringOptions = monitoringOptions
            return this
        }

        fun build(): APMClient {
            return APMClient(context, clientId, secretToken, monitoringOptions, sessionId)
        }

        init {
            requireNotNull(context) { "Context must not be null" }
            this.context = context
            require(clientId.isNotEmpty()) { "Client ID must not be null or empty" }
            this.clientId = clientId
            require(secretToken.isNotEmpty()) { "Secret Token must not be null or empty" }
            this.secretToken = secretToken

            this.sessionId = UUID.randomUUID().toString().replace("[^a-zA-Z0-9]".toRegex(), "")
        }
    }
}