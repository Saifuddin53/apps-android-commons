package fr.free.nrw.commons.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import fr.free.nrw.commons.utils.model.NetworkConnectionType
import org.jetbrains.annotations.NotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class NetworkUtilsTest {

    @Before
    fun setUp() {
    }

    @Test
    fun testInternetConnectionEstablished() {
        val mockContext = getContext(true)
        val result = NetworkUtils.isInternetConnectionEstablished(mockContext)
        assertTrue(result)
    }

    @NotNull
    fun getContext(connectionEstablished: Boolean): Context {
        val mockContext: Context = mock(Context::class.java)
        val mockApplication: Application = mock(Application::class.java)
        val mockConnectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java)
        val mockNetworkInfo: NetworkInfo = mock(NetworkInfo::class.java)
        `when`(mockNetworkInfo.isConnectedOrConnecting).thenReturn(connectionEstablished)
        `when`(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)
        `when`(mockContext.applicationContext).thenReturn(mockApplication)
        return mockContext
    }

    @Test
    fun testInternetConnectionNotEstablished() {
        val mockContext = getContext(false)
        val result = NetworkUtils.isInternetConnectionEstablished(mockContext)
        assertFalse(result)
    }

    @Test
    fun testInternetConnectionStatusForNullContext() {
        val result = NetworkUtils.isInternetConnectionEstablished(null)
        assertFalse(result)
    }

    @Test
    fun testInternetConnectionForNullConnectivityManager() {
        val mockContext: Context = mock(Context::class.java)
        val mockApplication: Application = mock(Application::class.java)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(null)
        `when`(mockContext.applicationContext).thenReturn(mockApplication)
        val result = NetworkUtils.isInternetConnectionEstablished(mockContext)
        assertFalse(result)
    }

    @Test
    fun testWifiNetwork() {
        val mockContext: Context = mock(Context::class.java)
        val mockApplication: Application = mock(Application::class.java)
        val mockConnectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java)
        val mockNetworkInfo: NetworkInfo = mock(NetworkInfo::class.java)
        `when`(mockNetworkInfo.type).thenReturn(ConnectivityManager.TYPE_WIFI)
        `when`(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)
        `when`(mockApplication.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mock(TelephonyManager::class.java))
        `when`(mockContext.applicationContext).thenReturn(mockApplication)
        val networkType = NetworkUtils.getNetworkType(mockContext)
        assertEquals(NetworkConnectionType.WIFI, networkType)
    }

    @Test
    @Ignore("Fix these test with telemetry permission")
    fun testCellular2GNetwork() {
        val mockContext: Context = mock(Context::class.java)
        val mockApplication: Application = mock(Application::class.java)
        val mockConnectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java)
        val mockNetworkInfo: NetworkInfo = mock(NetworkInfo::class.java)
        `when`(mockNetworkInfo.type).thenReturn(ConnectivityManager.TYPE_MOBILE)
        `when`(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)

        val mockTelephonyManager: TelephonyManager = mock(TelephonyManager::class.java)
        `when`(mockTelephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_EDGE)

        `when`(mockApplication.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager)
        `when`(mockContext.applicationContext).thenReturn(mockApplication)

        val networkType = NetworkUtils.getNetworkType(mockContext)
        assertEquals(NetworkConnectionType.TWO_G, networkType)
    }

    @Test
    @Ignore("Fix these test with telemetry permission")
    fun testCellular3GNetwork() {
        val mockContext: Context = mock(Context::class.java)
        val mockApplication: Application = mock(Application::class.java)
        val mockConnectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java)
        val mockNetworkInfo: NetworkInfo = mock(NetworkInfo::class.java)
        `when`(mockNetworkInfo.type).thenReturn(ConnectivityManager.TYPE_MOBILE)
        `when`(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)

        val mockTelephonyManager: TelephonyManager = mock(TelephonyManager::class.java)
        `when`(mockTelephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_HSPA)

        `when`(mockApplication.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager)
        `when`(mockContext.applicationContext).thenReturn(mockApplication)

        val networkType = NetworkUtils.getNetworkType(mockContext)
        assertEquals(NetworkConnectionType.THREE_G, networkType)
    }

    @Test
    @Ignore("Fix these test with telemetry permission")
    fun testCellular4GNetwork() {
        val mockContext: Context = mock(Context::class.java)
        val mockApplication: Application = mock(Application::class.java)
        val mockConnectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java)
        val mockNetworkInfo: NetworkInfo = mock(NetworkInfo::class.java)
        `when`(mockNetworkInfo.type).thenReturn(ConnectivityManager.TYPE_MOBILE)
        `when`(mockConnectivityManager.activeNetworkInfo).thenReturn(mockNetworkInfo)
        `when`(mockApplication.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)

        val mockTelephonyManager: TelephonyManager = mock(TelephonyManager::class.java)
        `when`(mockTelephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)

        `when`(mockApplication.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager)
        `when`(mockContext.applicationContext).thenReturn(mockApplication)

        val networkType = NetworkUtils.getNetworkType(mockContext)
        assertEquals(NetworkConnectionType.FOUR_G, networkType)
    }
}
