package fr.free.nrw.commons.nearby

import androidx.annotation.Nullable
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.mwapi.OkHttpJsonApiClient
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyPlaces @Inject constructor(private val okHttpJsonApiClient: OkHttpJsonApiClient) {

    @Throws(IOException::class)
    fun getFromWikidataQuery(curLatLng: LatLng, languageCode: String?, radius: String?): List<Place> {
        return try {
            okHttpJsonApiClient.getNearbyPlaces(curLatLng, languageCode, radius)
        } catch (e: Exception) {
            Timber.e(e)
            emptyList()
        }
    }

    @Throws(Exception::class)
    fun getPlacesAsKML(leftLatLng: LatLng, rightLatLng: LatLng): String {
        return okHttpJsonApiClient.getPlacesAsKML(leftLatLng, rightLatLng)
    }

    @Throws(Exception::class)
    fun getPlacesAsGPX(leftLatLng: LatLng, rightLatLng: LatLng): String {
        return okHttpJsonApiClient.getPlacesAsGPX(leftLatLng, rightLatLng)
    }
}

