package fr.free.nrw.commons.bookmarks.pictures

import fr.free.nrw.commons.Media
import fr.free.nrw.commons.bookmarks.models.Bookmark
import fr.free.nrw.commons.media.MediaClient
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkPicturesController @Inject constructor(
    private val mediaClient: MediaClient,
    private val bookmarkDao: BookmarkPicturesDao
) {
    private var currentBookmarks: List<Bookmark> = emptyList()

    fun loadBookmarkedPictures(): Single<List<Media>> {
        val bookmarks = bookmarkDao.allBookmarks
        currentBookmarks = bookmarks
        return Observable.fromIterable(bookmarks)
            .flatMap { bookmark -> getMediaFromBookmark(bookmark) }
            .toList()
    }

    private fun getMediaFromBookmark(bookmark: Bookmark): Observable<Media> {
        return mediaClient.getMedia(bookmark.mediaName)
            .toObservable()
            .onErrorResumeNext(Observable.empty())
    }

    fun needRefreshBookmarkedPictures(): Boolean {
        val bookmarks = bookmarkDao.allBookmarks
        return bookmarks.size != currentBookmarks.size
    }

    fun stop() {
        // noop
    }
}

