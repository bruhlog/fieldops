package com.zaidsiddique.fieldops.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class NominatimResponse(
    val display_name: String?
)

interface GeocodingService {

    @Headers(
        "User-Agent: FieldOps/1.0 (Android portfolio application)"
    )
    @GET("reverse?format=json")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): NominatimResponse
}