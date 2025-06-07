package fr.free.nrw.commons.explore.depictions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import fr.free.nrw.commons.Media
import fr.free.nrw.commons.R
import fr.free.nrw.commons.Utils
import fr.free.nrw.commons.ViewPagerAdapter
import fr.free.nrw.commons.bookmarks.items.BookmarkItemsDao
import fr.free.nrw.commons.category.CategoryImagesCallback
import fr.free.nrw.commons.databinding.ActivityWikidataItemDetailsBinding
import fr.free.nrw.commons.explore.depictions.child.ChildDepictionsFragment
import fr.free.nrw.commons.explore.depictions.media.DepictedImagesFragment
import fr.free.nrw.commons.explore.depictions.parent.ParentDepictionsFragment
import fr.free.nrw.commons.media.MediaDetailPagerFragment
import fr.free.nrw.commons.theme.BaseActivity
import fr.free.nrw.commons.upload.structure.depictions.DepictModel
import fr.free.nrw.commons.upload.structure.depictions.DepictedItem
import fr.free.nrw.commons.wikidata.WikidataConstants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WikidataItemDetailsActivity : BaseActivity(), CategoryImagesCallback {

    private lateinit var binding: ActivityWikidataItemDetailsBinding
    private val disposable = CompositeDisposable()

    @Inject lateinit var bookmarkItemsDao: BookmarkItemsDao

    private val depictModel by lazy { intent.getParcelableExtra<DepictedItem>(INTENT_EXTRA_ITEM) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWikidataItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.setTabData(
            listOf<Fragment>(
                DepictedImagesFragment.newInstance(depictModel?.entityId.orEmpty()),
                ParentDepictionsFragment.newInstance(depictModel?.entityId.orEmpty()),
                ChildDepictionsFragment.newInstance(depictModel?.entityId.orEmpty())
            ),
            listOf(
                getString(R.string.images),
                getString(R.string.parent_classifications),
                getString(R.string.child_classifications)
            )
        )
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_wikidata_page, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.share_item -> {
            share()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun share() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, WikidataConstants.getWikidataMobileUrl(depictModel?.entityId.orEmpty()))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
    }

    override fun getMediaAtPosition(i: Int): Media? = null

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    companion object {
        private const val INTENT_EXTRA_ITEM = "item"

        fun start(context: Context, item: DepictedItem) {
            val intent = Intent(context, WikidataItemDetailsActivity::class.java)
            intent.putExtra(INTENT_EXTRA_ITEM, item)
            context.startActivity(intent)
        }
    }
}

