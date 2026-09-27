package com.zaidsiddique.fieldops.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->

            val client =
                LocationServices.getFusedLocationProviderClient(context)

            client.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {
                        cont.resume(
                            location.latitude to location.longitude
                        )
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
}