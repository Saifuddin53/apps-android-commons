package fr.free.nrw.commons.explore.map

import fr.free.nrw.commons.Media
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.media.MediaClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreMapCalls @Inject constructor() {
    @Inject
    lateinit var mediaClient: MediaClient

    fun callCommonsQuery(currentLatLng: LatLng): List<Media> {
        val coordinates = "${currentLatLng.latitude}|${currentLatLng.longitude}"
        return mediaClient.getMediaListFromGeoSearch(coordinates).blockingGet()
    }
}
