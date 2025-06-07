package fr.free.nrw.commons.explore.recentsearches

import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import androidx.annotation.NonNull
import fr.free.nrw.commons.BuildConfig
import fr.free.nrw.commons.data.DBOpenHelper
import fr.free.nrw.commons.di.CommonsDaggerContentProvider
import javax.inject.Inject
import timber.log.Timber

class RecentSearchesContentProvider : CommonsDaggerContentProvider() {

    @Inject lateinit var dbOpenHelper: DBOpenHelper

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder().apply { tables = TABLE_NAME }
        val db = dbOpenHelper.readableDatabase
        val cursor: Cursor = when (uriMatcher.match(uri)) {
            RECENT_SEARCHES -> queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
            RECENT_SEARCHES_ID -> queryBuilder.query(db, ALL_FIELDS, "_id = ?", arrayOf(uri.lastPathSegment), null, null, sortOrder)
            else -> throw IllegalArgumentException("Unknown URI" + uri)
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues): Uri {
        val sqlDB = dbOpenHelper.writableDatabase
        val id = when (uriMatcher.match(uri)) {
            RECENT_SEARCHES -> sqlDB.insert(TABLE_NAME, null, values)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return Uri.parse("$BASE_URI/$id")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbOpenHelper.readableDatabase
        val rows = when (uriMatcher.match(uri)) {
            RECENT_SEARCHES_ID -> {
                Timber.d("Deleting recent searches id %s", uri.lastPathSegment)
                db.delete(TABLE_NAME, "_id = ?", arrayOf(uri.lastPathSegment))
            }
            else -> throw IllegalArgumentException("Unknown URI" + uri)
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rows
    }

    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        val sqlDB = dbOpenHelper.writableDatabase
        sqlDB.beginTransaction()
        when (uriMatcher.match(uri)) {
            RECENT_SEARCHES -> values.forEach { sqlDB.insert(TABLE_NAME, null, it) }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        sqlDB.setTransactionSuccessful()
        sqlDB.endTransaction()
        context!!.contentResolver.notifyChange(uri, null)
        return values.size
    }

    override fun update(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<out String>?): Int {
        val sqlDB = dbOpenHelper.writableDatabase
        val rowsUpdated = when (uriMatcher.match(uri)) {
            RECENT_SEARCHES_ID -> if (selection.isNullOrEmpty()) {
                val id = uri.lastPathSegment!!.toInt()
                sqlDB.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
            } else {
                throw IllegalArgumentException("Parameter `selection` should be empty when updating an ID")
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri with type ${uriMatcher.match(uri)}")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    companion object {
        private const val RECENT_SEARCHES = 1
        private const val RECENT_SEARCHES_ID = 2
        private const val BASE_PATH = "recent_searches"
        val BASE_URI: Uri = Uri.parse("content://${BuildConfig.RECENT_SEARCH_AUTHORITY}/$BASE_PATH")
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(BuildConfig.RECENT_SEARCH_AUTHORITY, BASE_PATH, RECENT_SEARCHES)
            addURI(BuildConfig.RECENT_SEARCH_AUTHORITY, "$BASE_PATH/#", RECENT_SEARCHES_ID)
        }
        val ALL_FIELDS = RecentSearchesDao.Table.ALL_FIELDS
        const val TABLE_NAME = RecentSearchesDao.Table.TABLE_NAME
        const val COLUMN_ID = RecentSearchesDao.Table.COLUMN_ID

        fun uriForId(id: Int): Uri = Uri.parse("$BASE_URI/$id")
    }
}
