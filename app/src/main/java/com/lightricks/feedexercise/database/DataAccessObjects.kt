package com.lightricks.feedexercise.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface FeedEntitiesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(items: List<FeedDbEntity>): Completable

    @Query("SELECT * FROM FeedItems ORDER BY id")
    fun loadAll(): LiveData<List<FeedDbEntity>>

    @Query("DELETE FROM FeedItems")
    fun deleteAll(): Completable
}