package com.disspear574.swishy.decisions.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

expect fun databaseBuilder(): RoomDatabase.Builder<SwishyDatabase>

fun createDatabase(): SwishyDatabase = databaseBuilder()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()

suspend fun createDecisionStore(database: SwishyDatabase, scope: CoroutineScope): PersistentDecisionStore {
    val dao = database.decisions()
    return PersistentDecisionStore(dao = dao, scope = scope, initial = dao.all())
}
