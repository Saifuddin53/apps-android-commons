package fr.free.nrw.commons.explore

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxSearchView
import fr.free.nrw.commons.Media
import fr.free.nrw.commons.R
import fr.free.nrw.commons.ViewPagerAdapter
import fr.free.nrw.commons.category.CategoryImagesCallback
import fr.free.nrw.commons.databinding.ActivitySearchBinding
import fr.free.nrw.commons.explore.categories.search.SearchCategoryFragment
import fr.free.nrw.commons.explore.depictions.search.SearchDepictionsFragment
import fr.free.nrw.commons.explore.media.SearchMediaFragment
import fr.free.nrw.commons.explore.models.RecentSearch
import fr.free.nrw.commons.explore.recentsearches.RecentSearchesDao
import fr.free.nrw.commons.explore.recentsearches.RecentSearchesFragment
import fr.free.nrw.commons.media.MediaDetailPagerFragment
import fr.free.nrw.commons.theme.BaseActivity
import fr.free.nrw.commons.utils.FragmentUtils
import fr.free.nrw.commons.utils.ViewUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

class SearchActivity : BaseActivity(), CategoryImagesCallback {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: ViewPagerAdapter

    @Inject
    lateinit var recentSearchesDao: RecentSearchesDao

    private val recentSearchesFragment by lazy { RecentSearchesFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupSearchView()
        setupViewPager()
    }

    private fun setupSearchView() {
        RxView.clicks(binding.searchBackIcon).subscribe { finish() }
        RxSearchView.queryTextChanges(binding.searchView)
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                if (!TextUtils.isEmpty(query)) {
                    val text = query.toString().lowercase(Locale.getDefault())
                    recentSearchesDao.addRecentSearch(RecentSearch(text, Date()))
                    (adapter.getItem(binding.viewPager.currentItem) as? SearchMediaFragment)?.searchFor(text)
                    (adapter.getItem(1) as? SearchCategoryFragment)?.searchFor(text)
                    (adapter.getItem(2) as? SearchDepictionsFragment)?.searchFor(text)
                }
            }
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.setTabData(
            listOf(
                SearchMediaFragment(),
                SearchCategoryFragment(),
                SearchDepictionsFragment()
            ),
            listOf(
                getString(R.string.media_tab_title),
                getString(R.string.categories_title),
                getString(R.string.depicts)
            )
        )
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    override fun getMediaAtPosition(i: Int): Media? =
        (adapter.getItem(binding.viewPager.currentItem) as? MediaDetailPagerFragment.MediaDetailProvider)
            ?.getMediaAtPosition(i)
}

