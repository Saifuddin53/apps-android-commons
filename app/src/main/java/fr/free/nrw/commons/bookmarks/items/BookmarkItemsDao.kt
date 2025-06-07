package fr.free.nrw.commons.bookmarks.items

import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.RemoteException
import fr.free.nrw.commons.bookmarks.items.BookmarkItemsContentProvider.BASE_URI
import fr.free.nrw.commons.category.CategoryItem
import fr.free.nrw.commons.upload.structure.depictions.DepictedItem
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import org.apache.commons.lang3.StringUtils

@Singleton
class BookmarkItemsDao @Inject constructor(
    @Named("bookmarksItem") private val clientProvider: Provider<ContentProviderClient>
) {

    val allBookmarksItems: List<DepictedItem>
        get() {
            val items = mutableListOf<DepictedItem>()
            val db = clientProvider.get()
            var cursor: Cursor? = null
            try {
                cursor = db.query(
                    BASE_URI,
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

    fun updateBookmarkItem(depictedItem: DepictedItem): Boolean {
        val exists = findBookmarkItem(depictedItem.id)
        if (exists) {
            deleteBookmarkItem(depictedItem)
        } else {
            addBookmarkItem(depictedItem)
        }
        return !exists
    }

    private fun addBookmarkItem(depictedItem: DepictedItem) {
        val db = clientProvider.get()
        try {
            db.insert(BASE_URI, toContentValues(depictedItem))
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            db.release()
        }
    }

    private fun deleteBookmarkItem(depictedItem: DepictedItem) {
        val db = clientProvider.get()
        try {
            db.delete(BookmarkItemsContentProvider.uriForName(depictedItem.id), null, null)
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        } finally {
            db.release()
        }
    }

    fun findBookmarkItem(depictedItemID: String?): Boolean {
        if (depictedItemID == null) return false
        val db = clientProvider.get()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                BASE_URI,
                Table.ALL_FIELDS,
                "${Table.COLUMN_ID}=?",
                arrayOf(depictedItemID),
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
    private fun fromCursor(cursor: Cursor): DepictedItem {
        val fileName = cursor.getString(cursor.getColumnIndex(Table.COLUMN_NAME))
        val description = cursor.getString(cursor.getColumnIndex(Table.COLUMN_DESCRIPTION))
        val imageUrl = cursor.getString(cursor.getColumnIndex(Table.COLUMN_IMAGE))
        val instanceListString = cursor.getString(cursor.getColumnIndex(Table.COLUMN_INSTANCE_LIST))
        val instanceList = stringToArray(instanceListString)
        val categoryNameListString = cursor.getString(cursor.getColumnIndex(Table.COLUMN_CATEGORIES_NAME_LIST))
        val categoryNameList = stringToArray(categoryNameListString)
        val categoryDescriptionListString = cursor.getString(cursor.getColumnIndex(Table.COLUMN_CATEGORIES_DESCRIPTION_LIST))
        val categoryDescriptionList = stringToArray(categoryDescriptionListString)
        val categoryThumbnailListString = cursor.getString(cursor.getColumnIndex(Table.COLUMN_CATEGORIES_THUMBNAIL_LIST))
        val categoryThumbnailList = stringToArray(categoryThumbnailListString)
        val categoryList = convertToCategoryItems(categoryNameList, categoryDescriptionList, categoryThumbnailList)
        val isSelected = java.lang.Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(Table.COLUMN_IS_SELECTED)))
        val id = cursor.getString(cursor.getColumnIndex(Table.COLUMN_ID))
        return DepictedItem(fileName, description, imageUrl, instanceList, categoryList, isSelected, id)
    }

    private fun convertToCategoryItems(
        categoryNameList: List<String>,
        categoryDescriptionList: List<String>,
        categoryThumbnailList: List<String>
    ): List<CategoryItem> {
        val categoryItems = mutableListOf<CategoryItem>()
        for (i in categoryNameList.indices) {
            categoryItems.add(
                CategoryItem(
                    categoryNameList[i],
                    categoryDescriptionList[i],
                    categoryThumbnailList[i],
                    false
                )
            )
        }
        return categoryItems
    }

    private fun stringToArray(listString: String): List<String> = listString.split(",")

    private fun arrayToString(list: List<String>?): String? = list?.let { StringUtils.join(it, ',') }

    private fun toContentValues(depictedItem: DepictedItem): ContentValues {
        val names = depictedItem.commonsCategories.map(CategoryItem::getName)
        val descriptions = depictedItem.commonsCategories.map(CategoryItem::getDescription)
        val thumbnails = depictedItem.commonsCategories.map(CategoryItem::getThumbnail)
        return ContentValues().apply {
            put(Table.COLUMN_NAME, depictedItem.name)
            put(Table.COLUMN_DESCRIPTION, depictedItem.description)
            put(Table.COLUMN_IMAGE, depictedItem.imageUrl)
            put(Table.COLUMN_INSTANCE_LIST, arrayToString(depictedItem.instanceOfs))
            put(Table.COLUMN_CATEGORIES_NAME_LIST, arrayToString(names))
            put(Table.COLUMN_CATEGORIES_DESCRIPTION_LIST, arrayToString(descriptions))
            put(Table.COLUMN_CATEGORIES_THUMBNAIL_LIST, arrayToString(thumbnails))
            put(Table.COLUMN_IS_SELECTED, depictedItem.isSelected)
            put(Table.COLUMN_ID, depictedItem.id)
        }
    }

    object Table {
        const val TABLE_NAME = "bookmarksItems"
        const val COLUMN_NAME = "item_name"
        const val COLUMN_DESCRIPTION = "item_description"
        const val COLUMN_IMAGE = "item_image_url"
        const val COLUMN_INSTANCE_LIST = "item_instance_of"
        const val COLUMN_CATEGORIES_NAME_LIST = "item_name_categories"
        const val COLUMN_CATEGORIES_DESCRIPTION_LIST = "item_description_categories"
        const val COLUMN_CATEGORIES_THUMBNAIL_LIST = "item_thumbnail_categories"
        const val COLUMN_IS_SELECTED = "item_is_selected"
        const val COLUMN_ID = "item_id"
        val ALL_FIELDS = arrayOf(
            COLUMN_NAME,
            COLUMN_DESCRIPTION,
            COLUMN_IMAGE,
            COLUMN_INSTANCE_LIST,
            COLUMN_CATEGORIES_NAME_LIST,
            COLUMN_CATEGORIES_DESCRIPTION_LIST,
            COLUMN_CATEGORIES_THUMBNAIL_LIST,
            COLUMN_IS_SELECTED,
            COLUMN_ID
        )
        const val DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS $TABLE_NAME"
        const val CREATE_TABLE_STATEMENT = "CREATE TABLE $TABLE_NAME (" +
            "$COLUMN_NAME STRING," +
            "$COLUMN_DESCRIPTION STRING," +
            "$COLUMN_IMAGE STRING," +
            "$COLUMN_INSTANCE_LIST STRING," +
            "$COLUMN_CATEGORIES_NAME_LIST STRING," +
            "$COLUMN_CATEGORIES_DESCRIPTION_LIST STRING," +
            "$COLUMN_CATEGORIES_THUMBNAIL_LIST STRING," +
            "$COLUMN_IS_SELECTED STRING," +
            "$COLUMN_ID STRING PRIMARY KEY" +
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
            if (f < 18) {
                f++
                onUpdate(db, f, to)
                return
            }
            if (f == 18) {
                onCreate(db)
                f++
                onUpdate(db, f, to)
            }
        }
    }
}

