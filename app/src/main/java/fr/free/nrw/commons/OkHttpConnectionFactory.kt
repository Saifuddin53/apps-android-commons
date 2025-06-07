package fr.free.nrw.commons

import androidx.annotation.NonNull
import fr.free.nrw.commons.wikidata.cookies.CommonsCookieJar
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.List
import java.util.concurrent.TimeUnit

object OkHttpConnectionFactory {
    private const val CACHE_DIR_NAME = "okhttp-cache"
    private const val NET_CACHE_SIZE = 64 * 1024 * 1024L

    @JvmField
    var CLIENT: OkHttpClient? = null

    @NonNull
    fun getClient(cookieJar: CommonsCookieJar): OkHttpClient {
        if (CLIENT == null) {
            CLIENT = createClient(cookieJar)
        }
        return CLIENT!!
    }

    @NonNull
    private fun createClient(cookieJar: CommonsCookieJar): OkHttpClient {
        val cache = CommonsApplication.getInstance()?.let {
            Cache(File(it.cacheDir, CACHE_DIR_NAME), NET_CACHE_SIZE)
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .cache(cache)
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(getLoggingInterceptor())
            .addInterceptor(UnsuccessfulResponseInterceptor())
            .addInterceptor(CommonHeaderRequestInterceptor())
            .build()
    }

    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor().setLevel(Level.BASIC)
        httpLoggingInterceptor.redactHeader("Authorization")
        httpLoggingInterceptor.redactHeader("Cookie")
        return httpLoggingInterceptor
    }

    private class CommonHeaderRequestInterceptor : Interceptor {
        @NonNull
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request().newBuilder()
                .header("User-Agent", CommonsApplication.getInstance().userAgent)
                .build()
            return chain.proceed(request)
        }
    }

    class UnsuccessfulResponseInterceptor : Interceptor {
        private val SUPPRESS_ERROR_LOG = "x-commons-suppress-error-log"
        val SUPPRESS_ERROR_LOG_HEADER = "$SUPPRESS_ERROR_LOG: true"
        private val DO_NOT_INTERCEPT: List<String> = Collections.singletonList(
            "api.php?format=json&formatversion=2&errorformat=plaintext&action=upload&ignorewarnings=1"
        )
        private val ERRORS_PREFIX = "{\"error"

        @NonNull
        override fun intercept(chain: Interceptor.Chain): Response {
            val rq = chain.request()
            val suppressErrors = rq.headers().names().contains(SUPPRESS_ERROR_LOG)
            val request = rq.newBuilder().removeHeader(SUPPRESS_ERROR_LOG).build()
            val rsp = chain.proceed(request)
            if (isExcludedUrl(chain.request())) {
                return rsp
            }
            if (rsp.isSuccessful) {
                try {
                    rsp.peekBody(ERRORS_PREFIX.length.toLong()).use { responseBody ->
                        if (ERRORS_PREFIX == responseBody.string()) {
                            rsp.body?.use { body ->
                                throw IOException(body.string())
                            }
                        }
                    }
                } catch (e: IOException) {
                    if (suppressErrors) {
                        Timber.d(e, "Suppressed (known / expected) error")
                    } else {
                        Timber.e(e)
                    }
                }
                return rsp
            }
            throw HttpStatusException(rsp)
        }

        private fun isExcludedUrl(request: Request): Boolean {
            val requestUrl = request.url.toString()
            for (url in DO_NOT_INTERCEPT) {
                if (requestUrl.contains(url)) {
                    return true
                }
            }
            return false
        }
    }

    class HttpStatusException(rsp: Response) : IOException() {
        private val code: Int = rsp.code
        private val url: String = rsp.request.url.uri().toString()
        fun code(): Int = code
        override val message: String
            get() = "Code: $code, URL: $url"
    }
}

