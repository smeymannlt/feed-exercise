package com.lightricks.feedexercise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, entities = [FeedDbEntity::class])
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedEntitiesDao(): FeedEntitiesDao

    companion object {
        fun build(context: Context) =
            Room.databaseBuilder(context, FeedDatabase::class.java, "Feed.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
