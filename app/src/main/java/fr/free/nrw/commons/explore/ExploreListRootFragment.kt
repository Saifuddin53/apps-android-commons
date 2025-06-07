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
import fr.free.nrw.commons.explore.categories.media.CategoriesMediaFragment
import fr.free.nrw.commons.media.MediaDetailPagerFragment
import fr.free.nrw.commons.navtab.NavTab

class ExploreListRootFragment() : CommonsDaggerSupportFragment(),
    MediaDetailPagerFragment.MediaDetailProvider,
    CategoryImagesCallback {

    private var mediaDetails: MediaDetailPagerFragment? = null
    private var listFragment: CategoriesMediaFragment? = null
    private var _binding: FragmentFeaturedRootBinding? = null
    private val binding get() = _binding!!

    constructor(bundle: Bundle) : this() {
        val title = bundle.getString("categoryName")
        listFragment = CategoriesMediaFragment().apply {
            arguments = Bundle().apply { putString("categoryName", title) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentFeaturedRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            listFragment?.let { setFragment(it, mediaDetails) }
        }
    }

    fun setFragment(fragment: Fragment, otherFragment: Fragment?) {
        with(childFragmentManager.beginTransaction()) {
            when {
                fragment.isAdded && otherFragment != null -> hide(otherFragment).show(fragment)
                fragment.isAdded && otherFragment == null -> show(fragment)
                !fragment.isAdded && otherFragment != null -> hide(otherFragment).add(R.id.explore_container, fragment)
                else -> replace(R.id.explore_container, fragment)
            }
            addToBackStack("CONTRIBUTION_LIST_FRAGMENT_TAG")
            commit()
        }
        childFragmentManager.executePendingTransactions()
    }

    fun removeFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().remove(fragment).commit()
        childFragmentManager.executePendingTransactions()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onMediaClicked(position: Int) {
        _binding?.exploreContainer?.visibility = View.VISIBLE
        (parentFragment as? ExploreFragment)?.binding?.tabLayout?.visibility = View.GONE
        mediaDetails = MediaDetailPagerFragment.newInstance(false, true)
        (parentFragment as? ExploreFragment)?.setScroll(false)
        mediaDetails?.let { setFragment(it, listFragment) }
        mediaDetails?.showImage(position)
    }

    override fun getMediaAtPosition(i: Int): Media? {
        return listFragment?.getMediaAtPosition(i)
    }

    override fun getTotalMediaCount(): Int {
        return listFragment?.totalMediaCount ?: 0
    }

    override fun getContributionStateAt(position: Int): Int? = null

    override fun refreshNominatedMedia(index: Int) {
        if (mediaDetails != null && listFragment?.isVisible == false) {
            removeFragment(mediaDetails!!)
            onMediaClicked(index)
        }
    }

    override fun viewPagerNotifyDataSetChanged() {
        mediaDetails?.notifyDataSetChanged()
    }

    fun backPressed(): Boolean {
        if (mediaDetails != null && mediaDetails!!.isVisible) {
            (parentFragment as? ExploreFragment)?.binding?.tabLayout?.visibility = View.VISIBLE
            removeFragment(mediaDetails!!)
            (parentFragment as? ExploreFragment)?.setScroll(true)
            listFragment?.let { setFragment(it, mediaDetails) }
            (activity as? MainActivity)?.showTabs()
            return true
        } else {
            (activity as? MainActivity)?.setSelectedItemId(NavTab.CONTRIBUTIONS.code())
        }
        (activity as? MainActivity)?.showTabs()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
