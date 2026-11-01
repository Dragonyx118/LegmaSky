package com.example.legmasky.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AqiApiClient {
    private static final String BASE_URL = "https://api.waqi.info/";
    public static final String WAQI_TOKEN = "84a526a2cd1c1661559207a83ab56bbcc6174013";

    private static Retrofit retrofit;

    public static AqiApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AqiApi.class);
    }
}