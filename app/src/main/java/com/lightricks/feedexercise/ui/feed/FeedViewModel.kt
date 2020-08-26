package com.lightricks.feedexercise.ui.feed

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.lightricks.feedexercise.data.FeedItem
import com.lightricks.feedexercise.data.FeedRepository
import com.lightricks.feedexercise.util.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * This view model manages the data for [FeedFragment].
 */
open class FeedViewModel(context: Context) : ViewModel() {
    private var disposableRequest: Disposable? = null
    private val repository: FeedRepository = FeedRepository(context)

    private val isLoading = MutableLiveData<Boolean>()
    private val isEmpty = MutableLiveData<Boolean>()
    private val feedItems = MediatorLiveData<List<FeedItem>>()
    private val networkErrorEvent = MutableLiveData<Event<String>>()

    private val mediatorLiveData = MediatorLiveData<List<FeedItem>>()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getIsEmpty(): LiveData<Boolean> = isEmpty
    fun getFeedItems(): LiveData<List<FeedItem>> = feedItems
    fun getNetworkErrorEvent(): LiveData<Event<String>> = networkErrorEvent

    init {
        feedItems.addSource(repository.feedItems) { feedItems.postValue(it) }
        mediatorLiveData.addSource(feedItems) { isEmpty.postValue(it?.isEmpty() != false) }
        refresh()
    }

    @Suppress("unused")
    fun destroy() {
        disposableRequest?.takeUnless { it.isDisposed }?.dispose()
    }

    @SuppressLint("CheckResult")
    fun refresh() {
        repository.refresh()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                isLoading.postValue(false)
                val items = repository.feedItems.value
                feedItems.postValue(items)
            }, {
                isLoading.postValue(false)
                networkErrorEvent.postValue(Event("Sorry, there was an error"))
                Log.w(LOG_TAG, "Error on fetching stream $it")
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
class FeedViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            throw IllegalArgumentException("factory used with a wrong class")
        }
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel(context) as T
    }
}