package api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("/attendance")
    suspend fun getPresences(): ApiResponse
}

data class ApiResponse(
    val count: Int,
    val records: List<ApiRecord>,
)

data class ApiRecord(
    @SerializedName("name") val faceName: String,
    val confidence: Double,
    @SerializedName("date") val timestamp: String,
)

object RetrofitClient {
    private const val BASE_URL = "https://nonpossibly-aspish-fletcher.ngrok-free.dev"

    val instance: ApiService by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
