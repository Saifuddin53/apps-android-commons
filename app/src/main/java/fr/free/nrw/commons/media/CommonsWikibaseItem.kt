package fr.free.nrw.commons.media

import com.google.gson.annotations.SerializedName

class CommonsWikibaseItem(
    @SerializedName("type") val type: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("labels") val labels: Map<String, Caption>? = null,
    @SerializedName("statements") val statements: Any? = null
)

