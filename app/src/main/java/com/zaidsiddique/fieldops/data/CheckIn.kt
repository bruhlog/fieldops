package com.zaidsiddique.fieldops.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

@Entity(tableName = "checkins")
data class CheckIn(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val qrCode: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val photoPath: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val remoteId: String? = null
)