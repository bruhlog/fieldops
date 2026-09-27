package com.zaidsiddique.fieldops.data

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CheckInRepository @Inject constructor(
    private val dao: CheckInDao,
    private val db: FirebaseDatabase
) {

    fun getAll(): Flow<List<CheckIn>> =
        dao.getAll()

    suspend fun saveLocally(checkIn: CheckIn) {
        dao.insert(checkIn)
    }

    suspend fun getPendingCheckIns(): List<CheckIn> {
        return dao.getPending()
    }

    suspend fun markSynced(id: String) {
        val existing = dao.getById(id) ?: return

        dao.update(
            existing.copy(
                syncStatus = SyncStatus.SYNCED
            )
        )
    }

    suspend fun pushToFirebase(checkIn: CheckIn) {

        val record = mapOf(
            "qrCode" to checkIn.qrCode,
            "timestamp" to checkIn.timestamp,
            "latitude" to checkIn.latitude,
            "longitude" to checkIn.longitude,
            "address" to checkIn.address,
            "photoPath" to checkIn.photoPath,
            "syncedAt" to System.currentTimeMillis()
        )

        db.getReference("checkins")
            .child("demoUser")
            .child(checkIn.id)
            .setValue(record)
            .await()
    }
}