package com.example.data.api

import com.example.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface CodelligenceApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<User>

    @PATCH("api/v1/users/me")
    suspend fun updateCurrentUser(@Body body: Map<String, String>): Response<User>

    @POST("api/v1/businesses")
    suspend fun createBusinessProfile(@Body business: BusinessProfile): Response<BusinessProfile>

    @GET("api/v1/businesses/{id}")
    suspend fun getBusinessProfile(@Path("id") id: String): Response<BusinessProfile>

    @PATCH("api/v1/businesses/{id}")
    suspend fun updateBusinessProfile(@Path("id") id: String, @Body body: Map<String, String>): Response<BusinessProfile>

    @GET("api/v1/listings")
    suspend fun getListings(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("city") city: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiListResponse<Listing>>

    @GET("api/v1/listings/{id}")
    suspend fun getListingById(@Path("id") id: String): Response<Listing>

    @POST("api/v1/listings")
    suspend fun createListing(@Body listing: Listing): Response<Listing>

    @PATCH("api/v1/listings/{id}")
    suspend fun updateListing(@Path("id") id: String, @Body listing: Listing): Response<Listing>

    @DELETE("api/v1/listings/{id}")
    suspend fun deleteListing(@Path("id") id: String): Response<Map<String, String>>

    @GET("api/v1/users/me/listings")
    suspend fun getMyListings(): Response<List<Listing>>

    @POST("api/v1/listings/{id}/favorite")
    suspend fun favoriteListing(@Path("id") id: String): Response<Map<String, String>>

    @DELETE("api/v1/listings/{id}/favorite")
    suspend fun unfavoriteListing(@Path("id") id: String): Response<Map<String, String>>

    @POST("api/v1/listings/{id}/report")
    suspend fun reportListing(@Path("id") id: String, @Body report: Report): Response<Map<String, String>>

    // Admin endpoints
    @GET("api/v1/admin/listings/pending")
    suspend fun getPendingListings(): Response<List<Listing>>

    @POST("api/v1/admin/listings/{id}/approve")
    suspend fun approveListing(@Path("id") id: String): Response<Listing>

    @POST("api/v1/admin/listings/{id}/reject")
    suspend fun rejectListing(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Listing>

    @POST("api/v1/admin/listings/{id}/suspend")
    suspend fun suspendListing(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Listing>

    @GET("api/v1/admin/reports")
    suspend fun getReports(): Response<List<Report>>

    @GET("api/v1/admin/audit-logs")
    suspend fun getAuditLogs(): Response<List<AuditLogEntry>>
}

object NetworkClient {
    private const val DEFAULT_BASE_URL = "https://api.codelligence.tech/"
    var currentBaseUrl: String = DEFAULT_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "CodelligenceFreeAds-Android/1.0")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    fun createService(baseUrl: String = currentBaseUrl): CodelligenceApiService {
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(CodelligenceApiService::class.java)
    }
}
