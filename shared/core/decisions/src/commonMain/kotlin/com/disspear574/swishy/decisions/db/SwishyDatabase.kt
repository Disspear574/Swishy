package com.disspear574.swishy.decisions.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [DecisionEntity::class, ImageHashEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@ConstructedBy(SwishyDatabaseConstructor::class)
abstract class SwishyDatabase : RoomDatabase() {

    abstract fun decisions(): DecisionDao

    abstract fun imageHashes(): ImageHashDao
}

@Suppress("KotlinNoActualForExpectedDeclaration", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object SwishyDatabaseConstructor : RoomDatabaseConstructor<SwishyDatabase> {
    override fun initialize(): SwishyDatabase
}
