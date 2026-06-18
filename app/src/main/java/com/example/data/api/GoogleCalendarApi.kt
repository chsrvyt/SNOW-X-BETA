package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Google Calendar API Models ---

data class GoogleCalendarListResponse(
    val summary: String? = null,
    val items: List<GoogleCalendarEvent>? = null
)

data class GoogleCalendarEvent(
    val id: String,
    val summary: String? = null,
    val description: String? = null,
    val location: String? = null,
    val start: GoogleCalendarTime? = null,
    val end: GoogleCalendarTime? = null
)

data class GoogleCalendarTime(
    val dateTime: String? = null, // "2026-10-24T14:40:00Z"
    val date: String? = null      // "2026-10-24" for all-day events
)

// --- Retrofit API Service for Google Calendar v3 ---

interface GoogleCalendarApiService {
    @GET("calendar/v3/calendars/{calendarId}/events")
    suspend fun getCalendarEvents(
        @Header("Authorization") authorization: String,
        @Path("calendarId") calendarId: String = "primary",
        @Query("singleEvents") singleEvents: Boolean = true,
        @Query("maxResults") maxResults: Int = 15,
        @Query("orderBy") orderBy: String = "startTime"
    ): GoogleCalendarListResponse
}

// --- Google Calendar Retrofit Client ---

object GoogleCalendarClient {
    private const val BASE_URL = "https://www.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: GoogleCalendarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GoogleCalendarApiService::class.java)
    }
}
