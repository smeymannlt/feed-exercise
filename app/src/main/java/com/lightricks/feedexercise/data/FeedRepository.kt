package com.lightricks.feedexercise.data

import android.content.Context
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
class FeedRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api by lazy { FeedApiService() }
    private val database = FeedDatabase.build(appContext)

    fun refresh(): Completable {
        // Fetch
        val fetchResult = api.fetchStream()

        // Store and return
        return fetchResult.flatMapCompletable { feedData ->
            if (feedData.metadata.isNotEmpty()) {
                database.feedEntitiesDao().insert(feedData.toDbEntities())
            } else {
                Completable.error(RuntimeException("Failure"))
            }
        }
    }

    val feedItems = Transformations.map(
        database.feedEntitiesDao().loadAll()
    ) { items ->
        items.map { item -> item.toFeedItem() }
    }

    private fun FeedData.toDbEntities() = metadata.map {
        FeedDbEntity(it.id, FeedApiService.uriForItem(it).toString(), it.isPremium)
    }

    private fun FeedDbEntity.toFeedItem() = FeedItem(id, thumbnailUrl, isPremium)
}