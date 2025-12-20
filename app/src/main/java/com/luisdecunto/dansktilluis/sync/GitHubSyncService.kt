package com.luisdecunto.dansktilluis.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Service for downloading database.json from GitHub
 */
class GitHubSyncService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DATABASE_URL =
            "https://raw.githubusercontent.com/luisdecunto/danish-exercises/main/data/exercises/database.json"
    }

    /**
     * Downloads the database.json file from GitHub
     * @return JSON string content, or null if download fails
     */
    suspend fun downloadDatabase(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(DATABASE_URL)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
