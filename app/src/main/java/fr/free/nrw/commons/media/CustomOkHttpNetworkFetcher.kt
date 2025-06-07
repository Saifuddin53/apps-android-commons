package fr.free.nrw.commons.media

import android.net.Uri
import android.os.Looper
import android.os.SystemClock
import com.facebook.imagepipeline.common.BytesRange
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.producers.BaseNetworkFetcher
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks
import com.facebook.imagepipeline.producers.Consumer
import com.facebook.imagepipeline.producers.FetchState
import com.facebook.imagepipeline.producers.NetworkFetcher
import com.facebook.imagepipeline.producers.ProducerContext
import fr.free.nrw.commons.CommonsApplication
import fr.free.nrw.commons.kvstore.JsonKvStore
import java.io.IOException
import java.util.concurrent.Executor
import javax.inject.Inject

class CustomOkHttpNetworkFetcher @Inject constructor(
    private val okHttpFetcher: NetworkFetcher<FetchState>,
    private val executor: Executor,
    private val kvStore: JsonKvStore
) : BaseNetworkFetcher<CustomOkHttpNetworkFetcher.OkHttpNetworkFetchState>() {

    override fun createFetchState(consumer: Consumer<EncodedImage>, context: ProducerContext): OkHttpNetworkFetchState {
        return OkHttpNetworkFetchState(consumer, context)
    }

    override fun fetch(fetchState: OkHttpNetworkFetchState, callback: Callback) {
        okHttpFetcher.fetch(fetchState, callback)
    }

    override fun onFetchCompletion(fetchState: OkHttpNetworkFetchState, byteSize: Int) {
        kvStore.putLong("last_fetch", SystemClock.uptimeMillis())
        okHttpFetcher.onFetchCompletion(fetchState, byteSize)
    }

    class OkHttpNetworkFetchState(consumer: Consumer<EncodedImage>, producerContext: ProducerContext) : FetchState(consumer, producerContext) {
        var submitTime: Long = 0
        var responseTime: Long = 0
        var fetchCompleteTime: Long = 0
    }
}

