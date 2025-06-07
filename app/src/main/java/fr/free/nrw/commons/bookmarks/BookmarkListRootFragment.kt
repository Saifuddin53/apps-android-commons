package fr.free.nrw.commons.bookmarks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import fr.free.nrw.commons.Media
import fr.free.nrw.commons.R
import fr.free.nrw.commons.bookmarks.category.BookmarkCategoriesFragment
import fr.free.nrw.commons.bookmarks.items.BookmarkItemsFragment
import fr.free.nrw.commons.bookmarks.locations.BookmarkLocationsFragment
import fr.free.nrw.commons.bookmarks.pictures.BookmarkPicturesFragment
import fr.free.nrw.commons.category.CategoryImagesCallback
import fr.free.nrw.commons.category.GridViewAdapter
import fr.free.nrw.commons.contributions.MainActivity
import fr.free.nrw.commons.databinding.FragmentFeaturedRootBinding
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment
import fr.free.nrw.commons.media.MediaDetailPagerFragment
import fr.free.nrw.commons.navtab.NavTab
import timber.log.Timber

class BookmarkListRootFragment() : CommonsDaggerSupportFragment(),
    MediaDetailPagerFragment.MediaDetailProvider,
    AdapterView.OnItemClickListener, CategoryImagesCallback {

    private var mediaDetails: MediaDetailPagerFragment? = null
    var listFragment: Fragment? = null
    private var bookmarksPagerAdapter: BookmarksPagerAdapter? = null
    private var _binding: FragmentFeaturedRootBinding? = null
    private val binding get() = _binding!!

    constructor(bundle: Bundle, adapter: BookmarksPagerAdapter) : this() {
        val title = bundle.getString("categoryName")
        val order = bundle.getInt("order")
        val orderItem = bundle.getInt("orderItem")
        listFragment = when (order) {
            0 -> BookmarkPicturesFragment()
            1 -> BookmarkLocationsFragment()
            3 -> BookmarkCategoriesFragment()
            else -> listFragment
        }
        if (orderItem == 2) {
            listFragment = BookmarkItemsFragment()
        }
        listFragment?.arguments = Bundle().apply { putString("categoryName", title) }
        bookmarksPagerAdapter = adapter
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
        val transaction = childFragmentManager.beginTransaction()
        when {
            fragment.isAdded && otherFragment != null -> {
                transaction.hide(otherFragment).show(fragment)
            }
            fragment.isAdded && otherFragment == null -> {
                transaction.show(fragment)
            }
            !fragment.isAdded && otherFragment != null -> {
                transaction.hide(otherFragment).add(R.id.explore_container, fragment)
            }
            else -> {
                transaction.replace(R.id.explore_container, fragment)
            }
        }
        transaction.addToBackStack("CONTRIBUTION_LIST_FRAGMENT_TAG").commit()
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
        Timber.d("on media clicked")
    }

    override fun getMediaAtPosition(i: Int): Media? {
        return bookmarksPagerAdapter?.mediaAdapter?.let { it.getItem(i) as Media }
    }

    override fun getTotalMediaCount(): Int {
        return bookmarksPagerAdapter?.mediaAdapter?.count ?: 0
    }

    override fun getContributionStateAt(position: Int): Int? = null

    override fun refreshNominatedMedia(index: Int) {
        if (mediaDetails != null && listFragment?.isVisible == false) {
            removeFragment(mediaDetails!!)
            mediaDetails = MediaDetailPagerFragment.newInstance(false, true)
            (parentFragment as BookmarkFragment).setScroll(false)
            setFragment(mediaDetails!!, listFragment!!)
            mediaDetails!!.showImage(index)
        }
    }

    override fun viewPagerNotifyDataSetChanged() {
        mediaDetails?.notifyDataSetChanged()
    }

    fun backPressed(): Boolean {
        mediaDetails?.let { details ->
            if (details.isVisible) {
                (parentFragment as BookmarkFragment).setupTabLayout()
                val removed = details.removedItems
                removeFragment(details)
                (parentFragment as BookmarkFragment).setScroll(true)
                setFragment(listFragment!!, details)
                (activity as MainActivity).showTabs()
                if (listFragment is BookmarkPicturesFragment) {
                    val adapter = (listFragment as BookmarkPicturesFragment).getAdapter() as GridViewAdapter
                    for (i in removed) {
                        adapter.remove(adapter.getItem(i))
                    }
                    details.clearRemoved()
                }
            } else {
                moveToContributionsFragment()
            }
        } ?: moveToContributionsFragment()
        return false
    }

    fun moveToContributionsFragment() {
        (activity as MainActivity).setSelectedItemId(NavTab.CONTRIBUTIONS.code())
        (activity as MainActivity).showTabs()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        Timber.d("on media clicked")
        binding.exploreContainer.visibility = View.VISIBLE
        (parentFragment as BookmarkFragment).binding.tabLayout.visibility = View.GONE
        mediaDetails = MediaDetailPagerFragment.newInstance(false, true)
        (parentFragment as BookmarkFragment).setScroll(false)
        setFragment(mediaDetails!!, listFragment!!)
        mediaDetails!!.showImage(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackStackChanged() {}
}

