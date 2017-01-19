package remote.httpclient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
class BaseInterceptor implements Interceptor {
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String MEDIA_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String USER_AGENT = "Android App";

    public BaseInterceptor() {
    }

    @Override
    public final Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Request.Builder builder = request.newBuilder()
                .header(HEADER_USER_AGENT, USER_AGENT)
                .header(HEADER_CONTENT_TYPE, MEDIA_TYPE_FORM);

        request = builder.build();

        Response response = chain.proceed(request);

        return response;
    }

}
