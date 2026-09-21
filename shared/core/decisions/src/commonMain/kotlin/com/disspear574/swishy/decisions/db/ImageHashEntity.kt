package com.disspear574.swishy.decisions.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

@Entity(tableName = "image_hashes")
data class ImageHashEntity(
    @PrimaryKey val assetId: String,
    val dHash: Long,
    val pHash: Long,
)

@Dao
interface ImageHashDao {

    @Query("SELECT * FROM image_hashes")
    suspend fun all(): List<ImageHashEntity>

    @Upsert
    suspend fun putAll(entities: List<ImageHashEntity>)

    @Query("DELETE FROM image_hashes")
    suspend fun clear()
}
