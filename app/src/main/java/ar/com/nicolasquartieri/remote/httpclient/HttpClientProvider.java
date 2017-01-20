package ar.com.nicolasquartieri.remote.httpclient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class HttpClientProvider {
    private static final int CONNECTION_TIMEOUT = 15 * 1000;
    private static OkHttpClient sHttpClient;

    public static OkHttpClient getHttpClient() {
        if (sHttpClient == null) {
            sHttpClient = createHttpClient();
        }
        return sHttpClient;
    }

    private static OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        builder.addInterceptor(new BaseInterceptor());
        return builder.build();
    }

    public static Response executeRequest(Request request)
            throws IOException {
        return getHttpClient().newCall(request).execute();
    }

    private HttpClientProvider() {
    }
}
