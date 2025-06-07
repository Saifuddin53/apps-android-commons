package fr.free.nrw.commons.nearby

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Nullable

/**
 * Handles the links to Wikipedia, Commons, and Wikidata that are displayed for a Place
 */
class Sitelinks private constructor(
    val wikipediaLink: String?,
    val commonsLink: String?,
    val wikidataLink: String?
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(wikipediaLink)
        dest.writeString(commonsLink)
        dest.writeString(wikidataLink)
    }

    override fun describeContents(): Int = 0

    val wikipediaUri: Uri
        get() = sanitiseString(wikipediaLink)
    val commonsUri: Uri
        get() = sanitiseString(commonsLink)
    val wikidataUri: Uri
        get() = sanitiseString(wikidataLink)

    private fun sanitiseString(stringUrl: String?): Uri {
        val sanitisedStringUrl = stringUrl?.replace("[<>\n\r]".toRegex(), "")?.trim() ?: ""
        return Uri.parse(sanitisedStringUrl)
    }

    override fun toString(): String {
        return "Sitelinks{" +
            "wikipediaLink='" + wikipediaLink + '\'' +
            ", commonsLink='" + commonsLink + '\'' +
            ", wikidataLink='" + wikidataLink + '\'' +
            '}'
    }

    class Builder {
        private var wikidataLink: String? = null
        private var commonsLink: String? = null
        private var wikipediaLink: String? = null

        fun setWikipediaLink(link: String?): Builder {
            this.wikipediaLink = link
            return this
        }

        fun setWikidataLink(link: String?): Builder {
            this.wikidataLink = link
            return this
        }

        fun setCommonsLink(@Nullable link: String?): Builder {
            this.commonsLink = link
            return this
        }

        fun build(): Sitelinks {
            return Sitelinks(wikipediaLink, commonsLink, wikidataLink)
        }
    }

    companion object CREATOR : Parcelable.Creator<Sitelinks> {
        override fun createFromParcel(`in`: Parcel): Sitelinks {
            return Sitelinks(`in`)
        }

        override fun newArray(size: Int): Array<Sitelinks?> {
            return arrayOfNulls(size)
        }
    }
}
