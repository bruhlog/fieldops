package com.zaidsiddique.fieldops.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Insert
    suspend fun insert(checkIn: CheckIn)

    @Query("SELECT * FROM checkins ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE syncStatus = 'PENDING'")
    suspend fun getPending(): List<CheckIn>

    @Query("SELECT * FROM checkins WHERE id = :id")
    suspend fun getById(id: String): CheckIn?

    @Update
    suspend fun update(checkIn: CheckIn)
}