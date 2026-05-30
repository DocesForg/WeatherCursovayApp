package com.docesforg.bura.platform.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BuraBackendApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @DELETE("api/accounts/{accountId}")
    suspend fun deleteAccount(@Path("accountId") accountId: Long)

    @PATCH("api/accounts/{accountId}/name")
    suspend fun updateName(@Path("accountId") accountId: Long, @Body body: UpdateNameRequestDto): AccountDto

    @PATCH("api/accounts/{accountId}/password")
    suspend fun updatePassword(@Path("accountId") accountId: Long, @Body body: UpdatePasswordRequestDto)

    @GET("api/accounts/{accountId}/favorites")
    suspend fun favorites(@Path("accountId") accountId: Long): List<FavoriteCityDto>

    @POST("api/accounts/{accountId}/favorites")
    suspend fun addFavorite(@Path("accountId") accountId: Long, @Body body: FavoriteCityRequestDto): FavoriteCityDto

    @DELETE("api/accounts/{accountId}/favorites/{favoriteId}")
    suspend fun deleteFavorite(@Path("accountId") accountId: Long, @Path("favoriteId") favoriteId: Long)

    @POST("api/accounts/{accountId}/support/messages")
    suspend fun sendSupportMessage(
        @Path("accountId") accountId: Long,
        @Body body: SendSupportMessageRequestDto,
    ): SupportMessageDto

    @GET("api/accounts/{accountId}/support/messages")
    suspend fun supportConversation(@Path("accountId") accountId: Long): SupportConversationDto

    @DELETE("api/accounts/{accountId}/support/messages")
    suspend fun deleteSupportConversation(@Path("accountId") accountId: Long)

    @POST("api/accounts/{accountId}/radio-tests")
    suspend fun runSignalTest(
        @Path("accountId") accountId: Long,
        @Body body: RadioSignalRequestDto
    ): RadioSignalResponseDto

    @GET("api/accounts/{accountId}/radio-tests")
    suspend fun radioHistory(@Path("accountId") accountId: Long): List<RadioSignalResponseDto>

    @GET("api/accounts/{accountId}/stats")
    suspend fun stats(@Path("accountId") accountId: Long): StatsDto
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val email: String, val displayName: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val account: AccountDto)

@Serializable
data class AccountDto(val id: Long, val email: String, val displayName: String)

@Serializable
data class UpdateNameRequestDto(val displayName: String)

@Serializable
data class UpdatePasswordRequestDto(val password: String)

@Serializable
data class FavoriteCityDto(val id: Long, val cityName: String, val latitude: Double, val longitude: Double)

@Serializable
data class FavoriteCityRequestDto(val cityName: String, val latitude: Double, val longitude: Double)

@Serializable
data class SendSupportMessageRequestDto(val email: String, val name: String, val message: String)

@Serializable
data class SupportMessageDto(val id: Long, val accountId: Long, val sender: String, val message: String, val createdAt: String)

@Serializable
data class SupportConversationDto(
    val accountId: Long,
    val email: String,
    val name: String,
    val forwardTo: String,
    val messages: List<SupportMessageDto>,
)

@Serializable
data class RadioSignalRequestDto(
    val cityA: String,
    val cityB: String,
    val latitudeA: Double,
    val longitudeA: Double,
    val latitudeB: Double,
    val longitudeB: Double,
    val frequencyMhz: Double,
)

@Serializable
data class RadioSignalResponseDto(
    val id: Long,
    val cityA: String,
    val cityB: String,
    val distanceKm: Double,
    val pathLossDb: Double,
    val quality: String,
    val latencyMs: Double = 0.0,
    val speedMbps: Double = 0.0,
    val createdAt: String,
)


@Serializable
data class StatsDto(val favorites: Int, val radioTests: Int, val supportRequests: Long)
