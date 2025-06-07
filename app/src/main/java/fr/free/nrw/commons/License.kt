package fr.free.nrw.commons

import androidx.annotation.Nullable

/**
 * Represents a license.
 */
class License(
    val key: String,
    val template: String,
    private val url: String?,
    private val name: String?
) {

    init {
        require(key.isNotEmpty()) { "License.key must not be null" }
        require(template.isNotEmpty()) { "License.template must not be null" }
    }

    /**
     * Gets the license name. If name is null, return license key.
     */
    fun getName(): String = name ?: key

    /**
     * Gets the license URL for the provided language, or null if none.
     */
    fun getUrl(language: String): String? = url?.replace("$" + "lang", language)
}
