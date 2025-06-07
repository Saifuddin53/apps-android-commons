package fr.free.nrw.commons

import androidx.annotation.NonNull
import fr.free.nrw.commons.wikidata.GsonUtil
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
abstract class MockWebServerTest {
    private lateinit var okHttpClient: OkHttpClient
    private val server = TestWebServer()

    @Before
    @Throws(Throwable::class)
    open fun setUp() {
        OkHttpConnectionFactory.CLIENT = createTestClient()
        okHttpClient = OkHttpConnectionFactory.CLIENT.newBuilder()
            .dispatcher(Dispatcher(ImmediateExecutorService()))
            .build()
        server.setUp()
    }

    @After
    @Throws(Throwable::class)
    open fun tearDown() {
        server.tearDown()
    }

    protected fun server(): TestWebServer = server

    @Throws(Throwable::class)
    protected fun enqueueFromFile(filename: String) {
        val json = TestFileUtil.readRawFile(filename)
        server.enqueue(json)
    }

    protected fun enqueue404() {
        val code = 404
        server.enqueue(MockResponse().setResponseCode(code).setBody("Not Found"))
    }

    protected fun enqueueMalformed() {
        server.enqueue("(╯°□°）╯︵ ┻━┻")
    }

    protected fun enqueueEmptyJson() {
        server.enqueue(MockResponse().setBody("{}"))
    }

    protected fun okHttpClient(): OkHttpClient = okHttpClient

    protected fun <T> service(clazz: Class<T>): T = service(clazz, server().getUrl())

    protected fun <T> service(clazz: Class<T>, url: String): T {
        return Retrofit.Builder()
            .baseUrl(url)
            .callbackExecutor(ImmediateExecutor())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonUtil.INSTANCE.defaultGson))
            .build()
            .create(clazz)
    }

    inner class ImmediateExecutorService : AbstractExecutorService() {
        override fun shutdown() {
            throw UnsupportedOperationException()
        }

        override fun shutdownNow(): MutableList<Runnable> {
            throw UnsupportedOperationException()
        }

        override fun isShutdown(): Boolean {
            throw UnsupportedOperationException()
        }

        override fun isTerminated(): Boolean {
            throw UnsupportedOperationException()
        }

        @Throws(InterruptedException::class)
        override fun awaitTermination(l: Long, unit: TimeUnit): Boolean {
            throw UnsupportedOperationException()
        }

        override fun execute(runnable: Runnable) {
            runnable.run()
        }
    }

    inner class ImmediateExecutor : Executor {
        override fun execute(runnable: Runnable) {
            runnable.run()
        }
    }
}
