package com.lightricks.feedexercise.network

import android.net.Uri
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

open class FeedApiService protected constructor() {
    private val jsonParser = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val feedFetcher = buildFeedFetcher()

    open fun fetchStream(): Single<FeedData> {
        return feedFetcher?.fetch() ?: Single.just(FeedData.EMPTY)
    }

    private fun buildFeedFetcher(): FeedFetcher? {
        return Retrofit.Builder().baseUrl("https://assets.swishvideoapp.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(MoshiConverterFactory.create(jsonParser)).build()
            .create(FeedFetcher::class.java)
    }

    companion object {
        fun uriForItem(item: FeedData.Metadata): Uri = if (item.templateThumbnailUrl.isNotBlank()) {
            Uri.parse("https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/")
                .buildUpon()
                .appendPath(item.templateThumbnailUrl).build()
        } else {
            Uri.EMPTY
        }

        private var instance: FeedApiService? = null
        fun get(): FeedApiService = instance ?: FeedApiService().also { instance = it }
        private const val LOG_TAG = "FeedApiService"
    }
}