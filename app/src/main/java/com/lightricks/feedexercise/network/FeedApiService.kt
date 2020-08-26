package com.lightricks.feedexercise.network

import android.net.Uri
import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

internal interface FeedFetcher {
    @GET("Android/demo/feed.json")
    fun fetch(): Single<FeedData>
}

open class FeedApiService {
    @WorkerThread
    open fun fetchStream(): Single<FeedData> {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder().baseUrl("https://assets.swishvideoapp.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

        return retrofit.create(FeedFetcher::class.java).fetch()
    }

    companion object {
        fun uriForItem(item: FeedData.Metadata) = if (item.templateThumbnailUrl.isNotBlank()) {
            Uri.parse("https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/")
                .buildUpon()
                .appendPath(item.templateThumbnailUrl).build()
        } else {
            Uri.EMPTY
        }

        private const val LOG_TAG = "FeedApiService"
    }
}