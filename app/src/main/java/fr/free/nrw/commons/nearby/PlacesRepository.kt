package fr.free.nrw.commons.nearby

import fr.free.nrw.commons.location.LatLng
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Repository for [Place] entities backed by [PlacesLocalDataSource].
 */
class PlacesRepository @Inject constructor(
    private val localDataSource: PlacesLocalDataSource
) {

    fun save(place: Place): Completable = localDataSource.savePlace(place)

    fun fetchPlace(entityID: String): Place = localDataSource.fetchPlace(entityID)

    fun fetchPlaces(mapBottomLeft: LatLng, mapTopRight: LatLng): List<Place> =
        localDataSource.fetchPlaces(mapBottomLeft, mapTopRight)

    /** Clears the Nearby cache on an IO thread. */
    fun clearCache(): Completable =
        localDataSource.clearCache().subscribeOn(Schedulers.io())
}
