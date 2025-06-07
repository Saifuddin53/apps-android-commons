package fr.free.nrw.commons

import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.nearby.Place

abstract class MapController {
    /** Container grouping place list and boundary coordinates */
    class NearbyPlacesInfo {
        lateinit var placeList: List<Place>
        lateinit var boundaryCoordinates: Array<LatLng>
        var currentLatLng: LatLng? = null
        var searchLatLng: LatLng? = null
        var mediaList: List<Media>? = null
    }

    /** Container grouping explore place list and boundary coordinates */
    class ExplorePlacesInfo {
        lateinit var explorePlaceList: List<Place>
        lateinit var boundaryCoordinates: Array<LatLng>
        var currentLatLng: LatLng? = null
        var searchLatLng: LatLng? = null
        var mediaList: List<Media>? = null
    }
}
