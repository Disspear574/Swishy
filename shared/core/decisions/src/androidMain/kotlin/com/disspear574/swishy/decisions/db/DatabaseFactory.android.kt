package com.disspear574.swishy.decisions.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object DecisionsContext {

    @Volatile
    internal var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
    }
}

actual fun databaseBuilder(): RoomDatabase.Builder<SwishyDatabase> {
    val context = requireNotNull(DecisionsContext.appContext) {
        "DecisionsContext.install() не вызван до создания базы"
    }
    return Room.databaseBuilder<SwishyDatabase>(
        context = context,
        name = context.getDatabasePath(DATABASE_NAME).absolutePath,
    )
}

internal actual fun nowMillis(): Long = System.currentTimeMillis()

private const val DATABASE_NAME = "swishy.db"
