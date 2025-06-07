package fr.free.nrw.commons

import androidx.annotation.NonNull
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class TestWebServer {
    companion object {
        const val TIMEOUT_DURATION = 5
        val TIMEOUT_UNIT: TimeUnit = TimeUnit.SECONDS
    }

    private val server = MockWebServer()

    @Throws(IOException::class)
    fun setUp() {
        server.start()
    }

    @Throws(IOException::class)
    fun tearDown() {
        server.shutdown()
    }

    fun getUrl(): String = getUrl("")

    fun getUrl(path: String): String = server.url(path).url().toString()

    val requestCount: Int
        get() = server.requestCount

    fun enqueue(body: String) {
        enqueue(MockResponse().setBody(body))
    }

    fun enqueue(response: MockResponse) {
        server.enqueue(response)
    }

    @Throws(InterruptedException::class)
    fun takeRequest(): RecordedRequest {
        val req = server.takeRequest(TIMEOUT_DURATION.toLong(), TIMEOUT_UNIT)
            ?: throw InterruptedException("Timeout elapsed.")
        return req
    }
}
