package fr.free.nrw.commons.explore.map

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fr.free.nrw.commons.BaseMarker
import fr.free.nrw.commons.databinding.FragmentExploreMapBinding
import fr.free.nrw.commons.kvstore.JsonKvStore
import fr.free.nrw.commons.location.LatLng
import fr.free.nrw.commons.location.LocationServiceManager
import javax.inject.Inject

class ExploreMapFragment : Fragment(), ExploreMapContract.View {

    private var _binding: FragmentExploreMapBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var presenter: ExploreMapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreMapBinding.inflate(inflater, container, false)
        presenter.attachView(this)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        _binding = null
    }

    override fun isNetworkConnectionEstablished(): Boolean = true

    override fun populatePlaces(curlatLng: LatLng) {}
    override fun askForLocationPermission() {}
    override fun recenterMap(curLatLng: LatLng) {}
    override fun hideBottomDetailsSheet() {}
    override fun getMapCenter(): LatLng = LatLng(0.0, 0.0)
    override fun getMapFocus(): LatLng = LatLng(0.0, 0.0)
    override fun getLastMapFocus(): LatLng = LatLng(0.0, 0.0)
    override fun addMarkersToMap(nearbyBaseMarkers: List<BaseMarker>) {}
    override fun clearAllMarkers() {}
    override fun addSearchThisAreaButtonAction() {}
    override fun setSearchThisAreaButtonVisibility(isVisible: Boolean) {}
    override fun setProgressBarVisibility(isVisible: Boolean) {}
    override fun isDetailsBottomSheetVisible(): Boolean = false
    override fun isSearchThisAreaButtonVisible(): Boolean = false
    override fun getLastLocation(): LatLng = LatLng(0.0, 0.0)
    override fun disableFABRecenter() {}
    override fun enableFABRecenter() {}
    override fun setFABRecenterAction(onClickListener: View.OnClickListener) {}
    override fun backButtonClicked(): Boolean = false
}

