package edu.rit.ks4898.narria

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40
    ): GoogleBooksResponse
}

object GoogleBooksApi {
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: GoogleBooksApiService = retrofit.create(GoogleBooksApiService::class.java)
}