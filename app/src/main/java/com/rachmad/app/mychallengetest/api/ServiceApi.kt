package com.rachmad.app.mychallengetest.api

import com.rachmad.app.mychallengetest.structure.GeocodeData
import com.rachmad.app.mychallengetest.structure.PlaceDetailsData
import com.rachmad.app.mychallengetest.structure.PredictionData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ServiceApi {
    @GET("/maps/api/geocode/json?language=id&region=id")
    fun geocodeAccess(
        @Query("latlng") latLong: String,
        @Query("key") key: String
    ): Call<GeocodeData>

    @GET("/maps/api/place/autocomplete/json?sessiontoken=DEVICE_ID&radius=50&language=id&components=country%3Aid")
    fun predictionAccess(
        @Query("input") search: String,
        @Query("key") key: String
    ): Call<PredictionData>

    @GET("/maps/api/place/details/json?language=id&region=id&fields=address_component%2Cname%2Cgeometry%2Cformatted_address")
    fun placeDetailsAccess(
        @Query("place_id") placeId: String,
        @Query("key") key: String
    ): Call<PlaceDetailsData>
}