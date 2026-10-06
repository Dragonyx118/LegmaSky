package com.example.legmasky.network;

import com.example.legmasky.model.StationData;
import com.example.legmasky.model.ForecastResponse;
import com.example.legmasky.model.OfficialAlertsResponse;
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

    @GET("data/{station_id}/forecast")
    Call<ForecastResponse> getForecast(
            @Path("station_id") String stationId,
            @Query("module") String module
    );

    @GET("data/{station_id}/official-alerts")
    Call<OfficialAlertsResponse> getOfficialAlerts(@Path("station_id") String stationId);
}