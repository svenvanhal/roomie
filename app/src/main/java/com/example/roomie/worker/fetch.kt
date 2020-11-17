package com.example.roomie.worker

import android.content.Context
import androidx.work.*
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.roomie.connectivity.RequestQueueSingleton
import com.example.roomie.model.Room
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class FetchRoomsWorker(appContext: Context, private val params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {

        // Get the input data
        val jsonUrl = params.inputData.getString(KEY_URL)

        // Retrieve JSON file with all ROOMs
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, jsonUrl, null,
            { response -> },
            { error -> }
        )
        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(jsonObjectRequest)


//        val json = fetchPost("https://www.room.nl/portal/object/frontend/getallobjects/format/json")

        // Parse JSON to list of Rooms


//        return rooms

        // Do the work here--in this case, upload the images.
//        val rooms = getRooms(applicationContext)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    companion object {
        private const val TAG = "FetchRoomsWorker"
        private const val KEY_URL = "key_user_data"

        fun buildRequest(url: String): PeriodicWorkRequest {

            val inputData = Data.Builder()
                .putString(KEY_URL, url)
                .build()

            return PeriodicWorkRequestBuilder<FetchRoomsWorker>(4, TimeUnit.HOURS, 1, TimeUnit.HOURS)
                .setInputData(inputData)
                .build()
        }
    }
}

fun getRooms(context: Context, filename_or_url: String): List<Room> {
    // Read JSON file to string
    val rawJson = context.assets.open(filename_or_url).bufferedReader().use {
        it.readText()
    }

    // Convert all JSONObjects to Room objects
    return parseJson(rawJson)
        .let {
            0.until(it.length())
                .map { i ->
                    jsonObjToRoom(it.getJSONObject(i))
                }
        }
}

fun parseJson(json: String): JSONArray {

    // Parse string as JSON
    val outer_json = JSONObject(json)

    // Check for the existence of results
    // If found, return those, else return an empty JSONArray
    return if (outer_json.has("result")) {
        outer_json.getJSONArray("result")
    } else JSONArray()
}

// Convert JSONObject to Room object
fun jsonObjToRoom(jsonObject: JSONObject): Room {

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

    return Room(
        id = jsonObject.optString("id"),
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

        lat = jsonObject.optString("latitude"),
        long = jsonObject.optString("longitude"),

        urlKey = jsonObject.optString("urlKey"),
    )
}
