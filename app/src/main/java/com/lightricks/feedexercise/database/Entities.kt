package com.lightricks.feedexercise.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FeedItems")
data class FeedDbEntity(
    @PrimaryKey val id: String,
    val thumbnailUrl: String,
    val isPremium: Boolean
)