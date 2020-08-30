package com.lightricks.feedexercise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, entities = [FeedDbEntity::class])
abstract class FeedDatabase protected constructor() : RoomDatabase() {
    abstract fun feedEntitiesDao(): FeedEntitiesDao

    companion object {
        private fun build(context: Context) =
            Room.databaseBuilder(context.applicationContext, FeedDatabase::class.java, "Feed.db")
                .fallbackToDestructiveMigration()
                .build()

        private var instance: FeedDatabase? = null
        fun get(context: Context) = instance ?: build(context).also { instance = it }
    }
}
