package com.example.roomie.model

import java.time.Duration
import java.time.Instant

data class Room(
    val id: String,
    val infoShort: String,

    val address: String,
    val postalCode: String,
    val region: String,
    val municipality: String,

    val floor: String,
    val surface: Int,
    val isStudio: Boolean,

    val netRent: Float,
    val totalRent: Float,

    val availableFrom: Instant?,
    val publicationDate: Instant?,
    val closingDate: Instant?,

    val lat: String,
    val long: String,

    // https://www.room.nl/aanbod/studentenwoningen/details/ + urlKey
    val urlKey: String,
) {
    fun timeRemaining(now: Instant = Instant.now()): Duration {
        return Duration.between(now, this.closingDate)
    }
}
