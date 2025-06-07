package fr.free.nrw.commons.media

import com.google.gson.annotations.SerializedName

class Caption(
    @SerializedName("language") val language: String? = null,
    @SerializedName("value") val value: String? = null
)

