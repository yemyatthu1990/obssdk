package io.github.yemyatthu1990.apm.instrumentation;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.github.yemyatthu1990.apm.Agent;
import io.github.yemyatthu1990.apm.NoOpInterceptor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.PeerServiceAttributesExtractor;
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTracing;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.OkHttpNetAttributesExtractor;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
public static List<String> getCallerClassesName() {
    StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
    List<String> clzzes = new ArrayList<>();
    for (int i=1; i<stElements.length; i++) {
        StackTraceElement ste = stElements[i];
        clzzes.add(ste.getClassName());
    }
    return clzzes;
}
    public static Interceptor getInstrumentInterceptor() {
        //We shouldn't add the interceptor to zipkin okhttpclient so, this is a very hackish
        // way to solve this problem for now
        //TODO find a better way to fix this
        if (getCallerClassesName().contains("zipkin2.reporter.okhttp3.OkHttpSender")) {
            return new NoOpInterceptor();
        } else {
            if (Agent.isSDKInitializingDone.get()) {
                Interceptor tracingInterceptor =
                        OkHttpTracing.builder(GlobalOpenTelemetry.get())
                                .addAttributesExtractor(new CustomAttributesExtractor())
                                .build()
                                .newInterceptor();
                return new OkhttpInstrumentedInterceptor(tracingInterceptor);
            }
        }
        return new NoOpInterceptor();
    }

    public static Call.Factory getInstrumentCallFactory(OkHttpClient okHttpClient) {
        return  OkHttpTracing.builder(GlobalOpenTelemetry.get())
                .addAttributesExtractor(
                        PeerServiceAttributesExtractor.create(new OkHttpNetAttributesExtractor()))
                .build()
                .newCallFactory(okHttpClient);
    }
}
