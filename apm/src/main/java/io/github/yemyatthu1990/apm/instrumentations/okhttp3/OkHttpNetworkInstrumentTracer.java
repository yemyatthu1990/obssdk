package io.github.yemyatthu1990.apm.instrumentations.okhttp3;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.PeerServiceAttributesExtractor;
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTracing;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.OkHttpNetAttributesExtractor;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.internal.connection.RealCall;

public class OkHttpNetworkInstrumentTracer {

//    public Call newCall(Request request) {
////        for(Interceptor interceptor: okHttpClient.networkInterceptors()) {
////            if (interceptor instanceof TracingInterceptor) {
////                okHttpClient.networkInterceptors().remove(interceptor);
////                break;
////            }
////        }
//        return getInstrumentCallFactory(okHttpClient).newCall(request);
//    }
    public static Interceptor getInstrumentInterceptor() {
        return  OkHttpTracing.builder(GlobalOpenTelemetry.get())
                .addAttributesExtractor(
                        PeerServiceAttributesExtractor.create(new OkHttpNetAttributesExtractor()))
                .build()
                .newInterceptor();
    }
    public static Call.Factory getInstrumentCallFactory(OkHttpClient okHttpClient) {
        return  OkHttpTracing.builder(GlobalOpenTelemetry.get())
                .addAttributesExtractor(
                        PeerServiceAttributesExtractor.create(new OkHttpNetAttributesExtractor()))
                .build()
                .newCallFactory(okHttpClient);
    }
}
