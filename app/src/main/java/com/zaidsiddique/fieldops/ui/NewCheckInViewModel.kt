package com.zaidsiddique.fieldops.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zaidsiddique.fieldops.data.CheckIn
import com.zaidsiddique.fieldops.data.CheckInRepository
import com.zaidsiddique.fieldops.location.LocationHelper
import com.zaidsiddique.fieldops.network.GeocodingService
import com.zaidsiddique.fieldops.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SaveCheckInState(
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)

@HiltViewModel
class NewCheckInViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CheckInRepository,
    private val locationHelper: LocationHelper,
    private val geocodingService: GeocodingService
) : ViewModel() {

    private val _state =
        MutableStateFlow(SaveCheckInState())

    val state: StateFlow<SaveCheckInState> =
        _state.asStateFlow()

    fun saveCheckIn(
        qrCode: String,
        photoPath: String,
        useLocation: Boolean = true
    ) {

        if (_state.value.isSaving) return

        viewModelScope.launch {

            _state.value =
                SaveCheckInState(isSaving = true)

            try {

                var latitude = 0.0
                var longitude = 0.0
                var address: String? = null

                if (useLocation) {

                    val location =
                        try {
                            locationHelper.getCurrentLocation()
                        } catch (e: Exception) {
                            null
                        }

                    if (location != null) {

                        latitude = location.first
                        longitude = location.second

                        address =
                            try {
                                geocodingService
                                    .reverseGeocode(
                                        lat = latitude,
                                        lon = longitude
                                    )
                                    .display_name
                            } catch (e: Exception) {

                                android.util.Log.e(
                                    "FieldOps-Geocoding",
                                    "Reverse geocoding failed",
                                    e
                                )

                                null
                            }
                    }
                }

                val checkIn = CheckIn(
                    qrCode = qrCode,
                    timestamp = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    photoPath = photoPath
                )

                repository.saveLocally(checkIn)

                enqueueSync()

                _state.value =
                    SaveCheckInState(
                        success = true,
                        latitude = latitude,
                        longitude = longitude,
                        address = address
                    )

            } catch (e: Exception) {

                _state.value =
                    SaveCheckInState(
                        error = e.message
                            ?: "Failed to save check-in."
                    )
            }
        }
    }

    private fun enqueueSync() {

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "sync_checkins",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
    }

    fun clearError() {
        _state.value =
            SaveCheckInState()
    }
}