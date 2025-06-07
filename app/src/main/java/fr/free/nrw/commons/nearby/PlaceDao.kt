package fr.free.nrw.commons.nearby

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable

/**
 * DAO for storing and retrieving [Place] objects, used for Nearby map caching.
 */
@Dao
abstract class PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveSynchronous(place: Place)

    @Query("SELECT * from place WHERE entityID=:entity")
    abstract fun getPlace(entity: String): Place

    @Query(
        "SELECT * from place WHERE name!='' AND latitude>=:latBegin AND longitude>=:lngBegin " +
            "AND latitude<:latEnd AND longitude<:lngEnd"
    )
    abstract fun fetchPlaces(
        latBegin: Double,
        lngBegin: Double,
        latEnd: Double,
        lngEnd: Double
    ): List<Place>

    fun save(place: Place): Completable =
        Completable.fromAction { saveSynchronous(place) }

    @Query("DELETE FROM place")
    abstract fun deleteAllSynchronous()

    fun deleteAll(): Completable =
        Completable.fromAction { deleteAllSynchronous() }
}
