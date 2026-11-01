package com.example.legmasky.network;

import com.example.legmasky.model.AqiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AqiApi {
    // coords va passato già formattato come "geo:LAT;LON" (encoded=true per non
    // far url-encodare i due punti e il punto e virgola, che WAQI richiede letterali)
    @GET("feed/{coords}/")
    Call<AqiResponse> getAqi(
            @Path(value = "coords", encoded = true) String coords,
            @Query("token") String token
    );
}