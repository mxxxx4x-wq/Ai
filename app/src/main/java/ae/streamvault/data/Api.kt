package ae.streamvault.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

interface XtreamApi {
    @GET("player_api.php") suspend fun login(@Query("username") u: String, @Query("password") p: String): LoginResponse
    @GET("player_api.php") suspend fun categories(@Query("username") u: String, @Query("password") p: String, @Query("action") action: String): List<Category>
    @GET("player_api.php") suspend fun media(@Query("username") u: String, @Query("password") p: String, @Query("action") action: String, @Query("category_id") category: String? = null): List<MediaItemDto>
}

interface MetadataApi { @GET("metadata") suspend fun metadata(@Query("title") title: String, @Query("year") year: Int? = null, @Query("type") type: String): ImdbMeta }

object ApiFactory {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }).build()
    fun xtream(base: String): XtreamApi = Retrofit.Builder().baseUrl(base).client(client).addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(XtreamApi::class.java)
    fun metadata(base: String): MetadataApi = Retrofit.Builder().baseUrl(base).client(client).addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(MetadataApi::class.java)
}
