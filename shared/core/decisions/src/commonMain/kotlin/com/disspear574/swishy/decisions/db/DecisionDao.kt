package com.disspear574.swishy.decisions.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DecisionDao {

    @Query("SELECT * FROM decisions")
    suspend fun all(): List<DecisionEntity>

    @Upsert
    suspend fun put(entity: DecisionEntity)

    @Query("DELETE FROM decisions WHERE assetId = :assetId")
    suspend fun remove(assetId: String)

    @Query("DELETE FROM decisions WHERE assetId IN (:assetIds)")
    suspend fun removeAll(assetIds: List<String>)

    @Query("DELETE FROM decisions")
    suspend fun clear()
}
