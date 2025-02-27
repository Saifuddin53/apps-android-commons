package fr.free.nrw.commons

import android.app.Application
import android.content.ContentProviderClient
import android.content.Context
import androidx.collection.LruCache
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import fr.free.nrw.commons.data.DBOpenHelper
import fr.free.nrw.commons.di.CommonsApplicationComponent
import fr.free.nrw.commons.di.CommonsApplicationModule
import fr.free.nrw.commons.di.DaggerCommonsApplicationComponent
import fr.free.nrw.commons.kvstore.JsonKvStore
import fr.free.nrw.commons.location.LocationServiceManager

class TestCommonsApplication : Application() {
    private var mockApplicationComponent: CommonsApplicationComponent? = null

    override fun onCreate() {
        if (mockApplicationComponent == null) {
            mockApplicationComponent =
                DaggerCommonsApplicationComponent
                    .builder()
                    .appModule(MockCommonsApplicationModule(this))
                    .build()
        }
        super.onCreate()
        setTheme(R.style.Theme_AppCompat)
        context = applicationContext
    }

    companion object {
        private var context: Context? = null

        fun getContext(): Context? = context
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class MockCommonsApplicationModule(appContext: Context) : CommonsApplicationModule(appContext) {

    val defaultSharedPreferences: JsonKvStore = mock()
    val locationServiceManager: LocationServiceManager = mock()
    val mockDbOpenHelper: DBOpenHelper = mock()
    val lruCache: LruCache<String, String> = mock()
    val gson: Gson = Gson()
    val categoryClient: ContentProviderClient = mock()
    val contributionClient: ContentProviderClient = mock()
    val modificationClient: ContentProviderClient = mock()
    val uploadPrefs: JsonKvStore = mock()

    override fun provideCategoryContentProviderClient(context: Context): ContentProviderClient = categoryClient

    override fun provideContributionContentProviderClient(context: Context): ContentProviderClient = contributionClient

    override fun provideModificationContentProviderClient(context: Context): ContentProviderClient = modificationClient

    override fun providesDefaultKvStore(context: Context, gson: Gson): JsonKvStore = defaultSharedPreferences

    override fun provideLocationServiceManager(context: Context): LocationServiceManager = locationServiceManager

    override fun provideDBOpenHelper(context: Context): DBOpenHelper = mockDbOpenHelper

    override fun provideLruCache(): LruCache<String, String> = lruCache
}
