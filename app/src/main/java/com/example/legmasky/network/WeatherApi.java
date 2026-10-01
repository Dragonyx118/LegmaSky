package com.example.legmasky.network;

import com.example.legmasky.model.StationData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("data/{station_id}/latest")
    Call<StationData> getLatest(
            @Path("station_id") String stationId,
            @Query("module") String module
    );
}