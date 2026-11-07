package ae.streamvault.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class UserInfo(val auth: Int? = 0, val status: String? = null, val username: String? = null)
@Serializable data class LoginResponse(@SerialName("user_info") val userInfo: UserInfo? = null)
@Serializable data class Category(@SerialName("category_id") val id: String, @SerialName("category_name") val name: String)
@Serializable data class MediaItemDto(
    @SerialName("stream_id") val streamId: Int? = null,
    @SerialName("series_id") val seriesId: Int? = null,
    val name: String,
    @SerialName("stream_icon") val icon: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val rating: String? = null,
    @SerialName("rating_5based") val rating5: Double? = null,
    val plot: String? = null,
    @SerialName("container_extension") val extension: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null
) { val id get() = streamId ?: seriesId ?: 0; val poster get() = icon ?: cover }

@Serializable data class ImdbMeta(val imdbId: String? = null, val title: String, val year: Int? = null, val plot: String? = null, val rating: Double? = null, val runtimeMinutes: Int? = null, val genres: List<String> = emptyList(), val posterUrl: String? = null)

data class Credentials(val server: String, val username: String, val password: String) {
    val base get() = server.trimEnd('/') + "/"
    fun streamUrl(type: String, id: Int, ext: String = if (type == "live") "m3u8" else "mp4") = "${base}$type/$username/$password/$id.$ext"
}
enum class LibraryType(val action: String, val path: String) { LIVE("get_live_streams", "live"), MOVIES("get_vod_streams", "movie"), SERIES("get_series", "series") }
