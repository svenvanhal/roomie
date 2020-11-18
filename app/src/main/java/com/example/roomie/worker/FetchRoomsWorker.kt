package com.example.roomie.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.roomie.persistence.AppDatabase
import com.example.roomie.model.RoomOffer
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class FetchRoomsWorker(appContext: Context, private val params: WorkerParameters) : Worker(appContext, params) {

    private val client = OkHttpClient()
    private val db = AppDatabase.getInstance(applicationContext)

    override fun doWork(): Result {
        Log.i(TAG, "Start fetching room offers!")

        // Get JSON url from settings
        val jsonUrl = params.inputData.getString(KEY_URL)
        if (jsonUrl.isNullOrBlank()) {
            Log.e(TAG, "No URL provided!")
            return Result.failure()
        }

        val headers = Headers.Builder()
            .add("Host: www.room.nl")
            .add("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0")
            .add("Accept: application/json, text/plain, */*")
            .add("Accept-Language: nl,en-US;q=0.7,en;q=0.3")
            .add("X-Requested-With: XMLHttpRequest")
            .add("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
            .add("Origin: https://www.room.nl")
            .add("DNT: 1")
            .add("Referer: https://www.room.nl/aanbod/studentenwoningen")
            .add("Pragma: no-cache")
            .add("Cache-Control: no-cache")
            .add("TE: Trailers")
            .build()

        // Build request
        val request = Request.Builder()
            .url(jsonUrl)
            .headers(headers)
            .post("".toRequestBody())
            .build()

        // Fetch JSON file
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure invoked: ${e.message}")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Unexpected response code $response")
                        throw IOException("Unexpected response code $response")
                    }

                    // Parse result
                    val rawJson = response.body!!.string()
                    val jsonArray = parseJson(rawJson)
                    val rooms = getRooms(jsonArray, RoomOffer::fromJsonObject)

                    if (rooms.isNotEmpty()) {

                        // TODO: TEMPORARY!! Clear existing rooms
                        db.roomOfferDao().deleteAll()

                        // Insert rooms into database
                        db.roomOfferDao().insertAll(rooms)
                    }

                    Log.i(TAG, "Done fetching room offers!")
                }
            }
        })

        // TODO: as the request is async, this is now always successful
        return Result.success()
    }

    companion object {
        private const val TAG = "FetchRoomsWorker"
        private const val KEY_URL = "key_user_data"

        fun buildPeriodicRequest(url: String): PeriodicWorkRequest {
            val inputData = Data.Builder()
                .putString(KEY_URL, url)
                .build()

            return PeriodicWorkRequestBuilder<FetchRoomsWorker>(4, TimeUnit.HOURS, 1, TimeUnit.HOURS)
                .setInputData(inputData)
                .build()
        }

        fun buildOneTimeRequest(url: String): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putString(KEY_URL, url)
                .build()

            return OneTimeWorkRequestBuilder<FetchRoomsWorker>()
                .setInputData(inputData)
                .build()
        }
    }
}

private fun parseJson(json: String): JSONArray {

    // Parse string as JSON
    val outerJson = JSONObject(json)

    // Check for the existence of results
    // If found, return those, else return an empty JSONArray
    return if (outerJson.has("result")) {
        outerJson.getJSONArray("result")
    } else JSONArray()
}

private fun getRooms(jsonArray: JSONArray, converter: (JSONObject) -> RoomOffer): List<RoomOffer> {

    // Convert all JSONObjects to Room objects
    return jsonArray.let {
        0.until(it.length())
            .map { i ->
                converter(it.getJSONObject(i))
            }
    }
}
