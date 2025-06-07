package fr.free.nrw.commons.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import fr.free.nrw.commons.contributions.ContributionController
import fr.free.nrw.commons.contributions.MainActivity
import fr.free.nrw.commons.databinding.FragmentBookmarksBinding
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment
import fr.free.nrw.commons.kvstore.JsonKvStore
import fr.free.nrw.commons.theme.BaseActivity
import javax.inject.Inject
import javax.inject.Named

class BookmarkFragment : CommonsDaggerSupportFragment() {

    private lateinit var supportFragmentManager: FragmentManager
    private lateinit var adapter: BookmarksPagerAdapter
    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var controller: ContributionController

    @Inject
    @Named("default_preferences")
    lateinit var applicationKvStore: JsonKvStore

    companion object {
        fun newInstance(): BookmarkFragment = BookmarkFragment().apply { retainInstance = true }
    }

    fun setScroll(canScroll: Boolean) {
        _binding?.viewPagerBookmarks?.setCanScroll(canScroll)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        supportFragmentManager = childFragmentManager
        adapter = BookmarksPagerAdapter(supportFragmentManager, requireContext(),
            applicationKvStore.getBoolean("login_skipped"))
        binding.viewPagerBookmarks.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPagerBookmarks)
        (activity as MainActivity).showTabs()
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setupTabLayout()
        return binding.root
    }

    fun setupTabLayout() {
        binding.tabLayout.visibility = View.VISIBLE
        if (adapter.count == 1) {
            binding.tabLayout.visibility = View.GONE
        }
    }

    fun onBackPressed() {
        val item = adapter.getItem(binding.tabLayout.selectedTabPosition) as BookmarkListRootFragment
        if (item.backPressed()) return
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

