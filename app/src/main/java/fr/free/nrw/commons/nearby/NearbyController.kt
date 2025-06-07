package fr.free.nrw.commons.nearby

import androidx.annotation.MainThread
import fr.free.nrw.commons.BaseMarker
import fr.free.nrw.commons.MapController
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.utils.LengthUtils.computeDistanceBetween
import fr.free.nrw.commons.utils.LengthUtils.formatDistanceBetween

class NearbyController : MapController() {

    fun computeDistancesFrom(current: LatLng, places: List<Place>): Map<Place, String> {
        val map = HashMap<Place, String>()
        places.forEach { place ->
            val distance = computeDistanceBetween(current, place.location)
            map[place] = formatDistanceBetween(distance)
        }
        return map
    }

    companion object {
        private val markerLabelList: MutableList<MarkerPlaceGroup> = ArrayList()

        @JvmStatic
        @MainThread
        fun updateMarkerLabelListBookmark(place: Place, isBookmarked: Boolean) {
            val iter = markerLabelList.listIterator()
            while (iter.hasNext()) {
                val markerPlaceGroup = iter.next()
                if (markerPlaceGroup.place.wikiDataEntityId == place.wikiDataEntityId) {
                    iter.set(MarkerPlaceGroup(isBookmarked, place))
                }
            }
        }
    }
}

