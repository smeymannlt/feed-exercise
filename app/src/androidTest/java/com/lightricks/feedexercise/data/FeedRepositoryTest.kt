package com.lightricks.feedexercise.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.FeedData
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
        repository = FeedRepository(dBase, MockFeedApiService())
    }

    @After
    fun destroyDb() {
        dBase.close()
    }

    @Test
    fun refresh() {
        repository.refresh().test().await()
        repository.feedItems.blockingObserve(listOf()).let {
            Truth.assertThat(it.size == MockFeedApiService.feedData.metadata.size).isTrue()
        }
    }
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
