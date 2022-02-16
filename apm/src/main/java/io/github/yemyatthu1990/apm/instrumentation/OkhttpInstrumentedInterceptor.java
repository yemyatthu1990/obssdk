package io.github.yemyatthu1990.apm.instrumentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.trace.Span;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OkhttpInstrumentedInterceptor implements Interceptor {
    private final Interceptor interceptor;
    public OkhttpInstrumentedInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return interceptor.intercept(new InstrumentedChain(chain));
    }


    static class InstrumentedChain implements Chain {
        private final Chain delegateChain;
        public InstrumentedChain(Chain delegateChain) {
            this.delegateChain = delegateChain;
        }

        @NonNull
        @Override
        public Call call() {
            return delegateChain.call();
        }

        @Override
        public int connectTimeoutMillis() {
            return delegateChain.connectTimeoutMillis();
        }

        @Nullable
        @Override
        public Connection connection() {
            return delegateChain.connection();
        }

        @NonNull
        @Override
        public Response proceed(@NonNull Request request) throws IOException {
            Response response;
            try {
                response = delegateChain.proceed(request);
            } catch (IOException e) {
                //Call span
                throw e;
            }
            return response;
        }

        @Override
        public int readTimeoutMillis() {
            return delegateChain.readTimeoutMillis();
        }

        @NonNull
        @Override
        public Request request() {
            return delegateChain.request();
        }

        @NonNull
        @Override
        public Chain withConnectTimeout(int i, @NonNull TimeUnit timeUnit) {
            return delegateChain.withConnectTimeout(i, timeUnit);
        }

        @NonNull
        @Override
        public Chain withReadTimeout(int i, @NonNull TimeUnit timeUnit) {
            return delegateChain.withReadTimeout(i, timeUnit);
        }

        @NonNull
        @Override
        public Chain withWriteTimeout(int i, @NonNull TimeUnit timeUnit) {
            return delegateChain.withWriteTimeout(i, timeUnit);
        }

        @Override
        public int writeTimeoutMillis() {
            return delegateChain.writeTimeoutMillis();
        }
    }
}
