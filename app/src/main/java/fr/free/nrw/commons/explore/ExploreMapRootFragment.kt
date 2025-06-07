package fr.free.nrw.commons.explore

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fr.free.nrw.commons.Media
import fr.free.nrw.commons.R
import fr.free.nrw.commons.category.CategoryImagesCallback
import fr.free.nrw.commons.contributions.MainActivity
import fr.free.nrw.commons.databinding.FragmentFeaturedRootBinding
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment
import fr.free.nrw.commons.explore.map.ExploreMapFragment
import fr.free.nrw.commons.media.MediaDetailPagerFragment
import fr.free.nrw.commons.navtab.NavTab

class ExploreMapRootFragment(bundle: Bundle? = null) :
    CommonsDaggerSupportFragment(),
    MediaDetailPagerFragment.MediaDetailProvider,
    CategoryImagesCallback {

    private var mediaDetails: MediaDetailPagerFragment? = null
    private var mapFragment: ExploreMapFragment = ExploreMapFragment()
    private var _binding: FragmentFeaturedRootBinding? = null
    private val binding get() = _binding!!

    init {
        val title = bundle?.getString("categoryName")
        val zoom = bundle?.getDouble("prev_zoom") ?: 0.0
        val latitude = bundle?.getDouble("prev_latitude") ?: 0.0
        val longitude = bundle?.getDouble("prev_longitude") ?: 0.0
        val featuredArguments = Bundle().apply { putString("categoryName", title) }
        if (zoom != 0.0 || latitude != 0.0 || longitude != 0.0) {
            featuredArguments.putDouble("prev_zoom", zoom)
            featuredArguments.putDouble("prev_latitude", latitude)
            featuredArguments.putDouble("prev_longitude", longitude)
        }
        mapFragment.arguments = featuredArguments
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFeaturedRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            setFragment(mapFragment, mediaDetails)
        }
    }

    private fun setFragment(fragment: Fragment, other: Fragment?) {
        childFragmentManager.beginTransaction().apply {
            if (fragment.isAdded) {
                if (other != null) hide(other) else Unit
                show(fragment)
            } else {
                if (other != null) hide(other)
                replace(R.id.explore_container, fragment)
            }
            addToBackStack("CONTRIBUTION_LIST_FRAGMENT_TAG")
        }.commit()
        childFragmentManager.executePendingTransactions()
    }

    fun backPressed(): Boolean {
        if (mediaDetails != null && mediaDetails!!.isVisible) {
            removeFragment(mediaDetails!!)
            (activity as MainActivity).showTabs()
            return true
        }
        if (mapFragment.isVisible && mapFragment.backButtonClicked()) {
            return true
        }
        (activity as MainActivity).setSelectedItemId(NavTab.CONTRIBUTIONS.code())
        (activity as MainActivity).showTabs()
        return false
    }

    private fun removeFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().remove(fragment).commit()
        childFragmentManager.executePendingTransactions()
    }

    fun loadNearbyMapFromExplore() {
        mapFragment.loadNearbyMapFromExplore()
    }

    override fun onMediaClicked(position: Int) {
        binding.exploreContainer.visibility = View.VISIBLE
        (parentFragment as ExploreFragment).binding.tabLayout.visibility = View.GONE
        mediaDetails = MediaDetailPagerFragment.newInstance(false, true)
        (parentFragment as ExploreFragment).setScroll(false)
        setFragment(mediaDetails!!, mapFragment)
        mediaDetails!!.showImage(position)
    }

    override fun getMediaAtPosition(i: Int): Media? = mediaDetails?.getMediaAtPosition(i)

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

