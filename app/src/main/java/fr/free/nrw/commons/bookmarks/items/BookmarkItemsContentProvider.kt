package fr.free.nrw.commons.bookmarks.items

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import fr.free.nrw.commons.BuildConfig
import fr.free.nrw.commons.data.DBOpenHelper
import fr.free.nrw.commons.di.CommonsDaggerContentProvider
import javax.inject.Inject
import timber.log.Timber

class BookmarkItemsContentProvider : CommonsDaggerContentProvider() {

    @Inject
    lateinit var dbOpenHelper: DBOpenHelper

    override fun getType(uri: Uri): String? = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val queryBuilder = SQLiteQueryBuilder().apply { tables = TABLE_NAME }
        val db = dbOpenHelper.readableDatabase
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun update(
        uri: Uri,
        values: ContentValues,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val sqlDB = dbOpenHelper.writableDatabase
        val rowsUpdated = if (selection.isNullOrEmpty()) {
            val id = uri.lastPathSegment!!.toInt()
            sqlDB.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        } else {
            throw IllegalArgumentException("Parameter `selection` should be empty when updating an ID")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun insert(uri: Uri, values: ContentValues): Uri {
        val sqlDB = dbOpenHelper.writableDatabase
        val id = sqlDB.insert(TABLE_NAME, null, values)
        context!!.contentResolver.notifyChange(uri, null)
        return Uri.parse("$BASE_URI/$id")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbOpenHelper.readableDatabase
        Timber.d("Deleting bookmark name %s", uri.lastPathSegment)
        val rows = db.delete(TABLE_NAME, "item_id = ?", arrayOf(uri.lastPathSegment))
        context!!.contentResolver.notifyChange(uri, null)
        return rows
    }

    companion object {
        private const val BASE_PATH = "bookmarksItems"
        val BASE_URI: Uri = Uri.parse("content://${BuildConfig.BOOKMARK_ITEMS_AUTHORITY}/$BASE_PATH")
        const val TABLE_NAME = BookmarkItemsDao.Table.TABLE_NAME
        const val COLUMN_ID = BookmarkItemsDao.Table.COLUMN_ID

        fun uriForName(id: String): Uri = Uri.parse("$BASE_URI/$id")
    }
}

