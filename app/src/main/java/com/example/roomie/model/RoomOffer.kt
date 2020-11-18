package com.example.roomie.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Entity
data class RoomOffer(

    @PrimaryKey(autoGenerate = true) val id: Int? = null,

    @ColumnInfo(name = "web_id") val webId: Int,

    @ColumnInfo(name = "info_short") val infoShort: String,

    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "postal_code") val postalCode: String,
    @ColumnInfo(name = "region") val region: String,
    @ColumnInfo(name = "municipality") val municipality: String,

    @ColumnInfo(name = "floor") val floor: String,
    @ColumnInfo(name = "surface") val surface: Int,
    @ColumnInfo(name = "is_studio") val isStudio: Boolean,

    @ColumnInfo(name = "net_rent") val netRent: Float,
    @ColumnInfo(name = "total_rent") val totalRent: Float,

    @ColumnInfo(name = "available_from") val availableFrom: Instant? = null,
    @ColumnInfo(name = "publication_date") val publicationDate: Instant? = null,
    @ColumnInfo(name = "closing_date") val closingDate: Instant? = null,

    @ColumnInfo(name = "latitude") val latitude: String,
    @ColumnInfo(name = "longitude") val longitude: String,

    // https://www.room.nl/aanbod/studentenwoningen/details/ + urlKey
    @ColumnInfo(name = "url_key") val urlKey: String,

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: Long?
) {
    fun timeRemaining(now: Instant = Instant.now()): Duration {
        return Duration.between(now, this.closingDate)
    }

    companion object {
        // Convert JSONObject to Room object
        fun fromJsonObject(jsonObject: JSONObject): RoomOffer {

            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val street = jsonObject.optString("street").trim()
            val houseNumber = jsonObject.optString("houseNumber").trim()
            val houseNumberAddition = jsonObject.optString("houseNumberAddition").let {
                when {
                    it.isNullOrBlank() -> ""
                    it.startsWith('-') -> it.trim()
                    else -> "-${it.trim()}"
                }
            }

            return RoomOffer(
                webId = jsonObject.getInt("id"),
                infoShort = jsonObject.optString("infoveldKort"),

                address = "$street $houseNumber$houseNumberAddition",
                postalCode = jsonObject.optString("postalcode"),
                region = jsonObject.getJSONObject("regio").optString("name"),
                municipality = jsonObject.getJSONObject("municipality").optString("name"),

                floor = jsonObject.getJSONObject("floor").optString("localizedName"),
                surface = jsonObject.optInt("areaDwelling"),
                isStudio = jsonObject.optBoolean("isZelfstandig"),

                netRent = jsonObject.optDouble("netRent").toFloat(),
                totalRent = jsonObject.optDouble("totalRent").toFloat(),

                availableFrom = jsonObject.optString("availableFromDate")
                    .let {
                        if (!it.isNullOrBlank()) {
                            LocalDate.parse(it, dateFormat)
                                .atStartOfDay()
                                .atZone(ZoneId.of("Europe/Amsterdam"))
                                .toInstant()
                        } else null
                    },

                publicationDate = jsonObject.optString("publicationDate")
                    .let {
                        if (!it.isNullOrBlank()) {
                            LocalDateTime.parse(it, dateTimeFormat)
                                .atZone(ZoneId.of("Europe/Amsterdam"))
                                .toInstant()
                        } else null
                    },
                closingDate = jsonObject.optString("closingDate")
                    .let {
                        if (!it.isNullOrBlank()) {
                            LocalDateTime.parse(it, dateTimeFormat)
                                .atZone(ZoneId.of("Europe/Amsterdam"))
                                .toInstant()
                        } else null
                    },

                latitude = jsonObject.optString("latitude"),
                longitude = jsonObject.optString("longitude"),

                urlKey = jsonObject.optString("urlKey"),

                createdAt = Instant.now().toEpochMilli()
            )
        }
    }
}
