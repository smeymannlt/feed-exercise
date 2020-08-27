package com.lightricks.feedexercise.network

import io.reactivex.Single

class MockFeedApiService : FeedApiService() {
    override fun fetchStream(): Single<FeedData> = Single.just(feedData)

    companion object {
        val feedData = FeedData((0..9).map {
            FeedData.Metadata(
                configuration = "Config_$it",
                id = "ID$it",
                isNew = it and 2 != 0,
                isPremium = it and 1 != 0,
                templateName = "Template$it",
                templateThumbnailUrl = "abc$it"
            )
        })
    }
}

