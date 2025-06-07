package fr.free.nrw.commons.nearby

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.free.nrw.commons.location.LatLng
import kotlinx.parcelize.Parcelize

@Entity(tableName = "place")
@Parcelize
data class Place(
    var language: String? = null,
    var name: String? = null,
    var label: Label? = null,
    var longDescription: String? = null,
    @Embedded var location: LatLng = LatLng(0.0, 0.0),
    var category: String? = null,
    var siteLinks: Sitelinks? = null,
    var pic: String = "",
    var exists: Boolean? = null,
    @PrimaryKey @NonNull var entityID: String = "",
    var thumb: String = "",
    var isMonument: Boolean = false
) : Parcelable {

    val wikiDataEntityId: String?
        get() = if (entityID.isNotEmpty()) entityID else siteLinks?.wikidataLink?.lastPathSegment

    fun hasWikipediaLink() = siteLinks?.wikipediaLink != null && siteLinks?.wikipediaLink != Uri.EMPTY
    fun hasWikidataLink() = siteLinks?.wikidataLink != null && siteLinks?.wikidataLink != Uri.EMPTY
    fun hasCommonsLink() = siteLinks?.commonsLink != null && siteLinks?.commonsLink != Uri.EMPTY
}

