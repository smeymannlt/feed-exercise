package com.lightricks.feedexercise.data

import androidx.lifecycle.Transformations
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedDbEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.FeedData
import io.reactivex.Completable

/**
 * This is our data layer abstraction. Users of this class don't need to know
 * where the data actually comes from (network, database or somewhere else).
 */
class FeedRepository(private val database: FeedDatabase, private val api: FeedApiService) {
    fun refreshAsync(): Completable {
        // Fetch
        val fetchResult = api.fetchStream()

        // Store and return
        return fetchResult.flatMapCompletable { feedData ->
            if (feedData.metadata.isNotEmpty()) {
                database.feedEntitiesDao().insert(feedData.metadata.map { it.toDbEntity() })
            } else {
                Completable.error(RuntimeException("Failure"))
            }
        }
    }

    val feedItems
        get() = Transformations.map(
            database.feedEntitiesDao().loadAll()
        ) { items ->
            items.map { item ->
                item.toFeedItem()
            }
        }

    private fun FeedData.Metadata.toDbEntity() =
        FeedDbEntity(id, FeedApiService.uriForItem(this).toString(), this.isPremium)

    private fun FeedDbEntity.toFeedItem() = FeedItem(id, thumbnailUrl, isPremium)
}