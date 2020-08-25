package com.lightricks.feedexercise.ui.feed

import android.util.Log
import androidx.lifecycle.*
import com.lightricks.feedexercise.data.FeedItem
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.util.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * This view model manages the data for [FeedFragment].
 */
open class FeedViewModel : ViewModel() {
    private var disposableRequest: Disposable? = null

    private val isLoading = MutableLiveData<Boolean>()
    private val isEmpty = MutableLiveData<Boolean>()
    private val feedItems = MediatorLiveData<List<FeedItem>>()
    private val networkErrorEvent = MutableLiveData<Event<String>>()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getIsEmpty(): LiveData<Boolean> = isEmpty
    fun getFeedItems(): LiveData<List<FeedItem>> = feedItems
    fun getNetworkErrorEvent(): LiveData<Event<String>> = networkErrorEvent

    init {
        refresh()
    }

    @Suppress("unused")
    fun destroy() {
        disposableRequest?.takeUnless { it.isDisposed }?.dispose()
    }

    fun refresh() {
        disposableRequest = FeedApiService().fetchStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ feedData ->
                isLoading.postValue(false)
                val items = feedData.metadata.map {
                    FeedItem(it.id, FeedApiService.uriForItem(it).toString(), it.isPremium)
                }
                isEmpty.postValue(items.isEmpty())
                feedItems.postValue(items)
            }, { throwable ->
                isLoading.postValue(false)
                networkErrorEvent.postValue(Event("Sorry, there was an error"))
                Log.w(LOG_TAG, "Error on fetching stream $throwable")
            })
    }

    companion object {
        private const val LOG_TAG = "FeedVM"
    }
}

/**
 * This class creates instances of [FeedViewModel].
 * It's not necessary to use this factory at this stage. But if we will need to inject
 * dependencies into [FeedViewModel] in the future, then this is the place to do it.
 */
class FeedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            throw IllegalArgumentException("factory used with a wrong class")
        }
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel() as T
    }
}