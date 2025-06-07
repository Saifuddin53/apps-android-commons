package fr.free.nrw.commons.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import fr.free.nrw.commons.R
import fr.free.nrw.commons.ViewPagerAdapter
import fr.free.nrw.commons.contributions.MainActivity
import fr.free.nrw.commons.databinding.FragmentExploreBinding
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment
import fr.free.nrw.commons.kvstore.JsonKvStore
import fr.free.nrw.commons.theme.BaseActivity
import fr.free.nrw.commons.utils.ActivityUtils
import javax.inject.Inject
import javax.inject.Named

class ExploreFragment : CommonsDaggerSupportFragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var featuredRootFragment: ExploreListRootFragment
    private lateinit var mobileRootFragment: ExploreListRootFragment
    private lateinit var mapRootFragment: ExploreMapRootFragment

    @Inject
    @Named("default_preferences")
    lateinit var applicationKvStore: JsonKvStore

    private var prevZoom: Double = 0.0
    private var prevLatitude: Double = 0.0
    private var prevLongitude: Double = 0.0

    fun setScroll(canScroll: Boolean) {
        _binding?.viewPager?.setCanScroll(canScroll)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        loadNearbyMapData()
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        viewPagerAdapter = ViewPagerAdapter(
            childFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                binding.viewPager.setCanScroll(position != 2)
            }
        })
        setTabs()
        setHasOptionsMenu(true)
        if (isCameFromNearbyMap()) {
            binding.viewPager.currentItem = 2
        }
        return binding.root
    }

    private fun setTabs() {
        val fragments = mutableListOf<Fragment>()
        val titles = mutableListOf<String>()
        val featuredArguments = Bundle().apply { putString("categoryName", "Featured_pictures_on_Wikimedia_Commons") }
        val mobileArguments = Bundle().apply { putString("categoryName", "Uploaded_with_Mobile/Android") }
        val mapArguments = Bundle().apply { putString("categoryName", "Map") }
        if (isCameFromNearbyMap()) {
            mapArguments.putDouble("prev_zoom", prevZoom)
            mapArguments.putDouble("prev_latitude", prevLatitude)
            mapArguments.putDouble("prev_longitude", prevLongitude)
        }
        featuredRootFragment = ExploreListRootFragment(featuredArguments)
        mobileRootFragment = ExploreListRootFragment(mobileArguments)
        mapRootFragment = ExploreMapRootFragment(mapArguments)
        fragments += listOf(featuredRootFragment, mobileRootFragment, mapRootFragment)
        titles += listOf(
            getString(R.string.explore_tab_title_featured).uppercase(),
            getString(R.string.explore_tab_title_mobile).uppercase(),
            getString(R.string.explore_tab_title_map).uppercase()
        )
        (activity as MainActivity).showTabs()
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        viewPagerAdapter.setTabData(fragments, titles)
        viewPagerAdapter.notifyDataSetChanged()
    }

    private fun loadNearbyMapData() {
        arguments?.let {
            prevZoom = it.getDouble("prev_zoom")
            prevLatitude = it.getDouble("prev_latitude")
            prevLongitude = it.getDouble("prev_longitude")
        }
    }

    fun isCameFromNearbyMap(): Boolean = prevZoom != 0.0 || prevLatitude != 0.0 || prevLongitude != 0.0

    fun onBackPressed(): Boolean {
        return when (binding.tabLayout.selectedTabPosition) {
            0 -> if (featuredRootFragment.backPressed()) {
                (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false); true
            } else false
            1 -> if (mobileRootFragment.backPressed()) {
                (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false); true
            } else false
            else -> if (mapRootFragment.backPressed()) {
                (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false); true
            } else false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!applicationKvStore.getBoolean("login_skipped")) {
            inflater.inflate(R.menu.explore_fragment_menu, menu)
            val others = menu.findItem(R.id.list_item_show_in_nearby)
            others.isVisible = binding.viewPager.currentItem == 2
            binding.viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    others.isVisible = position == 2
                }
            })
        } else {
            inflater.inflate(R.menu.menu_search, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_search -> {
            ActivityUtils.startActivityWithFlags(activity, SearchActivity::class.java)
            true
        }
        R.id.list_item_show_in_nearby -> {
            mapRootFragment.loadNearbyMapFromExplore()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

