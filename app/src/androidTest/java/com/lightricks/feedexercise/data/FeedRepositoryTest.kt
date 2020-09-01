package com.lightricks.feedexercise.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedDbEntity
import com.lightricks.feedexercise.network.MockFeedApiService
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {

    private lateinit var dBase: FeedDatabase
    private lateinit var repository: FeedRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @Before
    fun initDb() {
        dBase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            FeedDatabase::class.java
        ).build()
        repository = FeedRepository.createIfAbsent { dBase to MockFeedApiService() }
    }

    @After
    fun destroyDb() {
        dBase.close()
    }

    @Test
    fun storeToDbase() {
        repository.refreshAsync().test().await()
        dBase.feedEntitiesDao().loadAll().blockingObserve(listOf()).let {
            Truth.assertThat(it.size == MockFeedApiService.feedData.metadata.size).isTrue()
        }
    }

    @Test
    fun testLiveData() {
        val fakeData = (0..20).map { FeedDbEntity("FakeID_$it", "url_$it", it and 0x04 != 0) }
        dBase.feedEntitiesDao().deleteAll().test().await()
        dBase.feedEntitiesDao().insert(fakeData).test().await()
        repository.feedItems.blockingObserve(listOf()).let { appDataItems ->
            if (appDataItems?.size ?: 0 == 0) return@let
            Truth.assertThat(appDataItems.size).isEqualTo(fakeData.size)

            // Check contents
            val liveDataMap = appDataItems.map { it.id to it }.toMap()
            val fakeDataMap = fakeData.map { it.id to it }.toMap()

            // Check IDs
            Truth.assertThat(liveDataMap.keys == fakeDataMap.keys).isTrue()




            // Check values
            Truth.assertThat(liveDataMap.keys.all { liveDataMap[it]?.isEquivalentTo(fakeDataMap[it]) == true })
        }
    }

    private fun FeedItem.isEquivalentTo(dbEntity: FeedDbEntity?) = dbEntity != null
            && id == dbEntity.id
            && isPremium == dbEntity.isPremium
            && thumbnailUrl == dbEntity.thumbnailUrl
}

private fun <T> LiveData<T>.blockingObserve(defaultValue: T): T {
    var value: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            value = t ?: return
            latch.countDown()
            removeObserver(this)
        }
    }

    observeForever(observer)
    latch.await(5, TimeUnit.SECONDS)
    return value ?: defaultValue
}
