package fr.free.nrw.commons.bookmarks.items

import fr.free.nrw.commons.upload.structure.depictions.DepictedItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkItemsController @Inject constructor() {

    @Inject
    lateinit var bookmarkItemsDao: BookmarkItemsDao

    fun loadFavoritesItems(): List<DepictedItem> = bookmarkItemsDao.allBookmarksItems
}

