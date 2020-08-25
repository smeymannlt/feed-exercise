package com.lightricks.feedexercise.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedData(@Json(name = "templatesMetadata") val metadata: List<Metadata> = listOf()) {
    @JsonClass(generateAdapter = true)
    data class Metadata(
        @Json(name = "configuration") val configuration: String = "",
        @Json(name = "id") val id: String = "",
        @Json(name = "isNew") val isNew: Boolean = false,
        @Json(name = "isPremium") val isPremium: Boolean = false,
        @Json(name = "templateCategories") val templateCategories: List<String> = listOf(),
        @Json(name = "templateName") val templateName: String = "",
        @Json(name = "templateThumbnailURI") val templateThumbnailUrl: String = ""
    )

    companion object {
        val EMPTY = FeedData()
    }
}
