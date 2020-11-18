package com.example.roomie.model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.roomie.persistence.AppDatabase
import com.example.roomie.worker.FetchRoomsWorker
import java.time.Duration
import java.time.Instant

class RoomOfferViewModel(application: Application) : AndroidViewModel(application) {
    private val jsonUpdateUrl = "https://www.room.nl/portal/object/frontend/getallobjects/format/json"

    private val context: Context = application.applicationContext

    private val db = AppDatabase.getInstance(context)
    val rooms: LiveData<List<RoomOffer>> = db.roomOfferDao().getAll().asLiveData()
    val lastUpdated: LiveData<Instant?> = db.roomOfferDao().getLastTimestamp().asLiveData()

    fun forceRefreshRooms(cooldownPeriod: Int = 5) {

        val minutesSinceUpdate = Duration.between(
            lastUpdated.value ?: Instant.ofEpochMilli(0),
            Instant.now()
        ).toMinutes()

        Log.d("RoomOfferViewModel", "forceRefreshRooms: $minutesSinceUpdate minutes since last update")

        // Only update after at least 5 minutes have passed
        if (minutesSinceUpdate >= cooldownPeriod) {
            Log.d("RoomOfferViewModel", "forceRefreshRooms: enqueuing OneTimeWorkRequest to fetch room offers")

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    "OneTimeRoomOfferRefresh",
                    ExistingWorkPolicy.KEEP,
                    FetchRoomsWorker.buildOneTimeRequest(jsonUpdateUrl)
                )

        } else {
            Log.d("RoomOfferViewModel", "forceRefreshRooms: not updating")
        }
    }

    fun enqueuePeriodicalRefresh() {
        val periodicWorkRequest = FetchRoomsWorker.buildPeriodicRequest(jsonUpdateUrl)

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("PeriodicRoomOfferRefresh", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)
    }

}
