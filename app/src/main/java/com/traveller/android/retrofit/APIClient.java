package com.traveller.android.retrofit;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;
    private static final String BASEURLl = "https://maps.googleapis.com/";
    /*static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    static OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();*/

    public static Retrofit getClient() {
        if (retrofit == null)
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASEURLl)
                    .addConverterFactory(GsonConverterFactory.create())
    //                .client(client)
                    .build();
        return retrofit;
    }

}
