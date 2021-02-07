package com.traveller.android.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {
    //query, key; T-location, radius, type
    @GET("/maps/api/place/textsearch/json?")
    Call<PlacesT> getTextSearch(@Query("query") String query, @Query("location") String location, @Query("radius") String radius, @Query("type") String type, @Query("key") String key);

    //key, location, radius; N-type, rankby(prominence,distance)
    @GET("/maps/api/place/nearbysearch/json?")
    Call<PlacesN> getNearby(@Query("location") String location, @Query("radius") String radius, @Query("type") String type, @Query("key") String key);
}
