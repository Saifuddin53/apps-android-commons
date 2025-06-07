package fr.free.nrw.commons.media

import com.google.gson.annotations.SerializedName

class MwParseResult {
    @Suppress("unused")
    private val pageid: Int = 0
    @Suppress("unused")
    private val index: Int = 0
    private val text: MwParseText? = null

    fun text(): String? = text?.text

    class MwParseText {
        @SerializedName("*")
        var text: String? = null
    }
}

