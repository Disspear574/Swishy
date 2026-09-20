package com.disspear574.swishy.decisions.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey val assetId: String,
    val decision: String,
    val decidedAt: Long,
    val sizeBytes: Long,
)
