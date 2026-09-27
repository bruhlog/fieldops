package com.zaidsiddique.fieldops.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zaidsiddique.fieldops.data.CheckInRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CheckInRepository
) : CoroutineWorker(
    context,
    params
) {

    override suspend fun doWork(): Result {

        Log.d(
            "FieldOps-Sync",
            "SyncWorker started"
        )

        val pending = try {
            repository.getPendingCheckIns()
        } catch (e: Exception) {
            Log.e(
                "FieldOps-Sync",
                "Failed to read pending check-ins",
                e
            )
            return Result.retry()
        }

        Log.d(
            "FieldOps-Sync",
            "Pending check-ins: ${pending.size}"
        )

        if (pending.isEmpty()) {
            Log.d(
                "FieldOps-Sync",
                "Nothing to synchronize"
            )
            return Result.success()
        }

        pending.forEach { checkIn ->

            try {

                Log.d(
                    "FieldOps-Sync",
                    "Uploading check-in: ${checkIn.id}"
                )

                repository.pushToFirebase(checkIn)

                repository.markSynced(checkIn.id)

                Log.d(
                    "FieldOps-Sync",
                    "Check-in synced successfully: ${checkIn.id}"
                )

            } catch (e: Exception) {

                Log.e(
                    "FieldOps-Sync",
                    "Failed to sync check-in: ${checkIn.id}",
                    e
                )

                return Result.retry()
            }
        }

        Log.d(
            "FieldOps-Sync",
            "All pending check-ins synchronized"
        )

        return Result.success()
    }
}