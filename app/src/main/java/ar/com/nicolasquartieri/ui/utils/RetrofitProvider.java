package ar.com.nicolasquartieri.ui.utils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmail.com)
 */
public class RetrofitProvider {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            retrofit = createRetrofitClient();
        }
        return retrofit;
    }

    private static Retrofit createRetrofitClient() {
        //TODO
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//        httpClient.addInterceptor(logging);

        return new Retrofit.Builder()
                .baseUrl(EnvironmentUtils.getBaseUrl())
//                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private RetrofitProvider() {
    }
}
