package fr.free.nrw.commons.explore.map

import android.content.Context
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.free.nrw.commons.BaseMarker
import fr.free.nrw.commons.MapController
import fr.free.nrw.commons.Media
import fr.free.nrw.commons.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.nearby.Place
import fr.free.nrw.commons.utils.ImageUtils
import fr.free.nrw.commons.utils.LocationUtils
import fr.free.nrw.commons.utils.PlaceUtils

open class ExploreMapController @JvmOverloads constructor(
    private val exploreMapCalls: ExploreMapCalls? = null
) : MapController() {

    var latestSearchLocation: LatLng? = null
    var currentLocation: LatLng? = null
    var latestSearchRadius: Double = 0.0
    var currentLocationSearchRadius: Double = 0.0

    fun createNearbyBaseMarkers(
        context: Context,
        placeList: List<Place>,
        callback: (List<BaseMarker>, MapController.ExplorePlacesInfo) -> Unit
    ) {
        val resources = context.resources
        val baseMarkerList = mutableListOf<BaseMarker>()
        val exploreInfo = MapController.ExplorePlacesInfo(placeList.size)
        for (place in placeList) {
            val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_mapmarker_central, context.theme)
            val baseMarker = BaseMarker(drawable!!, place.location.latitude, place.location.longitude)
            baseMarker.place = place
            baseMarker.label = place.label?.text ?: ""
            baseMarkerList.add(baseMarker)
            if (place.thumb.isNotEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(place.thumb)
                    .apply(RequestOptions().centerCrop())
                    .into(object : CustomTarget<android.graphics.Bitmap>() {
                        override fun onResourceReady(resource: android.graphics.Bitmap, transition: Transition<in android.graphics.Bitmap>?) {
                            baseMarker.icon = BitmapDescriptorFactory.fromBitmap(resource)
                            if (baseMarkerList.size == placeList.size) {
                                callback(baseMarkerList, exploreInfo)
                            }
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
        if (placeList.isEmpty()) {
            callback(baseMarkerList, exploreInfo)
        }
    }
}

