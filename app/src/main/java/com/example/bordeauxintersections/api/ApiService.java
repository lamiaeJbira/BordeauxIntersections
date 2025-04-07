package com.example.bordeauxintersections.api;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/api/explore/v2.1/catalog/datasets/ci_chantier/records")
    Call<ChantierResponse> getChantiers(
            @Query("limit") int limit,
            @Query("offset") int offset
    );
}