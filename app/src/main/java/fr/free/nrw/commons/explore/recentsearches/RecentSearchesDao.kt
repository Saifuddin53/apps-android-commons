package fr.free.nrw.commons.explore.recentsearches

import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentValues
import android.database.Cursor
import android.os.RemoteException
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import fr.free.nrw.commons.explore.models.RecentSearch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import timber.log.Timber

class RecentSearchesDao @Inject constructor(
    @Named("recentsearch") private val clientProvider: Provider<ContentProviderClient>
) {

    fun save(recentSearch: RecentSearch) {
        val db = clientProvider.get()
        try {
            if (recentSearch.contentUri == null) {
                recentSearch.contentUri = db.insert(RecentSearchesContentProvider.BASE_URI, toContentValues(recentSearch))
            } else {
                db.update(recentSearch.contentUri!!, toContentValues(recentSearch), null, null)
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            db.release()
        }
    }

    fun deleteAll() {
        val db = clientProvider.get()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                RecentSearchesContentProvider.BASE_URI,
                Table.ALL_FIELDS,
                null,
                arrayOf(),
                Table.COLUMN_LAST_USED + " DESC"
            )
            while (cursor != null && cursor.moveToNext()) {
                try {
                    val recentSearch = find(fromCursor(cursor).query)
                    if (recentSearch?.contentUri == null) {
                        throw RuntimeException("tried to delete item with no content URI")
                    } else {
                        Timber.d("QUERY_NAME %s - delete tried", recentSearch.contentUri)
                        db.delete(recentSearch.contentUri!!, null, null)
                        Timber.d("QUERY_NAME %s - query deleted", recentSearch.query)
                    }
                } catch (e: RemoteException) {
                    Timber.e(e, "query deleted")
                    throw RuntimeException(e)
                } finally {
                    db.release()
                }
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            cursor?.close()
        }
    }

    fun delete(recentSearch: RecentSearch) {
        val db = clientProvider.get()
        try {
            if (recentSearch.contentUri == null) {
                throw RuntimeException("tried to delete item with no content URI")
            } else {
                db.delete(recentSearch.contentUri!!, null, null)
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            db.release()
        }
    }

    @Nullable
    fun find(name: String): RecentSearch? {
        val db = clientProvider.get()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                RecentSearchesContentProvider.BASE_URI,
                Table.ALL_FIELDS,
                Table.COLUMN_NAME + "=?",
                arrayOf(name),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return fromCursor(cursor)
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            cursor?.close()
            db.release()
        }
        return null
    }

    @NonNull
    fun recentSearches(limit: Int): List<String> {
        val items = mutableListOf<String>()
        val db = clientProvider.get()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                RecentSearchesContentProvider.BASE_URI,
                Table.ALL_FIELDS,
                null,
                arrayOf(),
                Table.COLUMN_LAST_USED + " DESC"
            )
            while (cursor != null && cursor.moveToNext() && cursor.position < limit) {
                items.add(fromCursor(cursor).query)
            }
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            cursor?.close()
            db.release()
        }
        return items
    }

    @NonNull
    @SuppressLint("Range")
    internal fun fromCursor(cursor: Cursor): RecentSearch = RecentSearch(
        RecentSearchesContentProvider.uriForId(cursor.getInt(cursor.getColumnIndex(Table.COLUMN_ID))),
        cursor.getString(cursor.getColumnIndex(Table.COLUMN_NAME)),
        java.util.Date(cursor.getLong(cursor.getColumnIndex(Table.COLUMN_LAST_USED)))
    )

    private fun toContentValues(recentSearch: RecentSearch): ContentValues = ContentValues().apply {
        put(Table.COLUMN_NAME, recentSearch.query)
        put(Table.COLUMN_LAST_USED, recentSearch.lastSearched.time)
    }

    object Table {
        const val TABLE_NAME = "recent_searches"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_LAST_USED = "last_used"

        val ALL_FIELDS = arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_LAST_USED)

        const val DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS $TABLE_NAME"

        const val CREATE_TABLE_STATEMENT = "CREATE TABLE $TABLE_NAME (" +
            "$COLUMN_ID INTEGER PRIMARY KEY," +
            "$COLUMN_NAME STRING," +
            "$COLUMN_LAST_USED INTEGER" +
            ");"

        fun onCreate(db: android.database.sqlite.SQLiteDatabase) {
            db.execSQL(CREATE_TABLE_STATEMENT)
        }

        fun onDelete(db: android.database.sqlite.SQLiteDatabase) {
            db.execSQL(DROP_TABLE_STATEMENT)
            onCreate(db)
        }

        fun onUpdate(db: android.database.sqlite.SQLiteDatabase, from: Int, to: Int) {
            if (from == to) return
            var f = from
            if (f < 6) {
                f++
                onUpdate(db, f, to)
                return
            }
            if (f == 6) {
                onCreate(db)
                f++
                onUpdate(db, f, to)
                return
            }
            if (f == 7) {
                f++
                onUpdate(db, f, to)
                return
            }
        }
    }
}
