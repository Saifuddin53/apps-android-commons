package fr.free.nrw.commons.nearby

import fr.free.nrw.commons.location.LatLng
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Local data source handling cached [Place] objects.
 */
class PlacesLocalDataSource @Inject constructor(
    private val placeDao: PlaceDao
) {

    fun fetchPlace(entityID: String): Place = placeDao.getPlace(entityID)

    fun fetchPlaces(mapBottomLeft: LatLng, mapTopRight: LatLng): List<Place> {
        data class Constraint(val latBegin: Double, val lngBegin: Double, val latEnd: Double, val lngEnd: Double)
        val constraints = mutableListOf<Constraint>()

        if (mapTopRight.latitude < mapBottomLeft.latitude) {
            if (mapTopRight.longitude < mapBottomLeft.longitude) {
                constraints += Constraint(mapBottomLeft.latitude, mapBottomLeft.longitude, 90.0, 180.0)
                constraints += Constraint(mapBottomLeft.latitude, -180.0, 90.0, mapTopRight.longitude)
                constraints += Constraint(-90.0, mapBottomLeft.longitude, mapTopRight.latitude, 180.0)
                constraints += Constraint(-90.0, -180.0, mapTopRight.latitude, mapTopRight.longitude)
            } else {
                constraints += Constraint(mapBottomLeft.latitude, mapBottomLeft.longitude, 90.0, mapTopRight.longitude)
                constraints += Constraint(-90.0, mapBottomLeft.longitude, mapTopRight.latitude, mapTopRight.longitude)
            }
        } else {
            if (mapTopRight.longitude < mapBottomLeft.longitude) {
                constraints += Constraint(mapBottomLeft.latitude, mapBottomLeft.longitude, mapTopRight.latitude, 180.0)
                constraints += Constraint(mapBottomLeft.latitude, -180.0, mapTopRight.latitude, mapTopRight.longitude)
            } else {
                constraints += Constraint(mapBottomLeft.latitude, mapBottomLeft.longitude, mapTopRight.latitude, mapTopRight.longitude)
            }
        }

        val cachedPlaces = mutableListOf<Place>()
        for (c in constraints) {
            cachedPlaces += placeDao.fetchPlaces(c.latBegin, c.lngBegin, c.latEnd, c.lngEnd)
        }
        return cachedPlaces
    }

    fun savePlace(place: Place): Completable = placeDao.save(place)

    fun clearCache(): Completable = placeDao.deleteAll()
}
