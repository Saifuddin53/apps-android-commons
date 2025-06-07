package fr.free.nrw.commons.bookmarks

import android.content.Context
import android.os.Bundle
import android.widget.ListAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import fr.free.nrw.commons.R
import fr.free.nrw.commons.bookmarks.pictures.BookmarkPicturesFragment

class BookmarksPagerAdapter(
    fm: FragmentManager,
    context: Context,
    onlyPictures: Boolean
) : FragmentPagerAdapter(fm) {

    private val pages = ArrayList<BookmarkPages>()

    init {
        val picturesBundle = Bundle().apply {
            putString("categoryName", context.getString(R.string.title_page_bookmarks_pictures))
            putInt("order", 0)
        }
        pages.add(BookmarkPages(BookmarkListRootFragment(picturesBundle, this),
            context.getString(R.string.title_page_bookmarks_pictures)))

        if (!onlyPictures) {
            val locationBundle = Bundle().apply {
                putString("categoryName", context.getString(R.string.title_page_bookmarks_locations))
                putInt("order", 1)
            }
            pages.add(BookmarkPages(BookmarkListRootFragment(locationBundle, this),
                context.getString(R.string.title_page_bookmarks_locations)))

            locationBundle.putInt("orderItem", 2)
            pages.add(BookmarkPages(BookmarkListRootFragment(locationBundle, this),
                context.getString(R.string.title_page_bookmarks_items)))
        }

        val categoriesBundle = Bundle().apply {
            putString("categoryName", context.getString(R.string.title_page_bookmarks_categories))
            putInt("order", 3)
        }
        pages.add(BookmarkPages(BookmarkListRootFragment(categoriesBundle, this),
            context.getString(R.string.title_page_bookmarks_categories)))
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Fragment = pages[position].page

    override fun getCount(): Int = pages.size

    override fun getPageTitle(position: Int): CharSequence? = pages[position].title

    val mediaAdapter: ListAdapter
        get() {
            val fragment = (pages[0].page as BookmarkListRootFragment).listFragment as BookmarkPicturesFragment
            return fragment.getAdapter()
        }

    fun requestPictureListUpdate() {
        val fragment = (pages[0].page as BookmarkListRootFragment).listFragment as BookmarkPicturesFragment
        fragment.onResume()
    }
}

