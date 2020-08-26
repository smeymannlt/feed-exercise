package com.lightricks.feedexercise.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * todo: add Room's Entity data class(es) here
 */
@Entity(tableName = "FeedItems")
data class FeedDbEntity(
    @PrimaryKey val id: String,
    val thumbnailUrl: String,
    val isPremium: Boolean
)