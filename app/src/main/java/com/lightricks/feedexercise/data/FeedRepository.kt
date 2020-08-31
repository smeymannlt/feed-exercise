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
 *
 * Don't construct directly, use [FeedRepository.createIfAbsent] instead
 *
 */
class FeedRepository private constructor(
    private val database: FeedDatabase,
    private val api: FeedApiService
) {
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

    val feedItems = Transformations.map(
        database.feedEntitiesDao().loadAll()
    ) { items ->
        items.map { item ->
            item.toFeedItem()
        }
    }

    /**
     * Call this on system shutdown. E.g on logout.
     */
    @Suppress("unused")
    fun destroy() {
        instance = null
    }

    private fun FeedData.Metadata.toDbEntity() =
        FeedDbEntity(id, FeedApiService.uriForItem(this).toString(), this.isPremium)

    private fun FeedDbEntity.toFeedItem() = FeedItem(id, thumbnailUrl, isPremium)

    companion object {
        private var instance: FeedRepository? = null

        /**
         * Create the feed repository singleton, which will happen if not created before.
         *
         * @param creator a lambda that provides a pair of the database and the API handlers
         *
         * @return an instance of [FeedRepository]
         */
        fun createIfAbsent(creator: () -> Pair<FeedDatabase, FeedApiService>) = instance
            ?: creator.invoke().let { FeedRepository(it.first, it.second) }.also { instance = it }
    }
}