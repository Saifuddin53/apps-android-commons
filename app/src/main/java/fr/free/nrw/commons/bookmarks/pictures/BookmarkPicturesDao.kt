package fr.free.nrw.commons.bookmarks.pictures

import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.RemoteException
import fr.free.nrw.commons.bookmarks.models.Bookmark
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class BookmarkPicturesDao @Inject constructor(
    @Named("bookmarks") private val clientProvider: Provider<ContentProviderClient>
) {
    val allBookmarks: List<Bookmark>
        get() = getAllBookmarks()

    fun getAllBookmarks(): List<Bookmark> {
        val items = mutableListOf<Bookmark>()
        var cursor: Cursor? = null
        val db = clientProvider.get()
        try {
            cursor = db.query(
                BookmarkPicturesContentProvider.BASE_URI,
                Table.ALL_FIELDS,
                null,
                arrayOf(),
                null
            )
            while (cursor != null && cursor.moveToNext()) {
                items.add(fromCursor(cursor))
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            cursor?.close()
            db.release()
        }
        return items
    }

    fun updateBookmark(bookmark: Bookmark): Boolean {
        val exists = findBookmark(bookmark)
        if (exists) {
            deleteBookmark(bookmark)
        } else {
            addBookmark(bookmark)
        }
        return !exists
    }

    private fun addBookmark(bookmark: Bookmark) {
        val db = clientProvider.get()
        try {
            db.insert(BASE_URI, toContentValues(bookmark))
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            db.release()
        }
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        val db = clientProvider.get()
        try {
            val uri = bookmark.contentUri ?: throw RuntimeException("tried to delete item with no content URI")
            db.delete(uri, null, null)
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            db.release()
        }
    }

    fun findBookmark(bookmark: Bookmark?): Boolean {
        if (bookmark == null) return false
        var cursor: Cursor? = null
        val db = clientProvider.get()
        try {
            cursor = db.query(
                BookmarkPicturesContentProvider.BASE_URI,
                Table.ALL_FIELDS,
                "${Table.COLUMN_MEDIA_NAME}=?",
                arrayOf(bookmark.mediaName),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return true
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            cursor?.close()
            db.release()
        }
        return false
    }

    @SuppressLint("Range")
    fun fromCursor(cursor: Cursor): Bookmark {
        val fileName = cursor.getString(cursor.getColumnIndex(Table.COLUMN_MEDIA_NAME))
        return Bookmark(
            fileName,
            cursor.getString(cursor.getColumnIndex(Table.COLUMN_CREATOR)),
            BookmarkPicturesContentProvider.uriForName(fileName)
        )
    }

    private fun toContentValues(bookmark: Bookmark): ContentValues = ContentValues().apply {
        put(Table.COLUMN_MEDIA_NAME, bookmark.mediaName)
        put(Table.COLUMN_CREATOR, bookmark.mediaCreator)
    }

    object Table {
        const val TABLE_NAME = "bookmarks"
        const val COLUMN_MEDIA_NAME = "media_name"
        const val COLUMN_CREATOR = "media_creator"
        val ALL_FIELDS = arrayOf(COLUMN_MEDIA_NAME, COLUMN_CREATOR)
        const val DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS $TABLE_NAME"
        const val CREATE_TABLE_STATEMENT = "CREATE TABLE $TABLE_NAME (" +
            "$COLUMN_MEDIA_NAME STRING PRIMARY KEY," +
            "$COLUMN_CREATOR STRING" +
            ");"

        fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE_STATEMENT)
        }
        fun onDelete(db: SQLiteDatabase) {
            db.execSQL(DROP_TABLE_STATEMENT)
            onCreate(db)
        }
        fun onUpdate(db: SQLiteDatabase, from: Int, to: Int) {
            if (from == to) return
            var f = from
            if (f < 7) {
                f++
                onUpdate(db, f, to)
                return
            }
            if (f == 7) {
                onCreate(db)
                f++
                onUpdate(db, f, to)
                return
            }
            if (f == 8) {
                f++
                onUpdate(db, f, to)
                return
            }
        }
    }
}

