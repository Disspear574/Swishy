package com.disspear574.swishy.decisions.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual fun databaseBuilder(): RoomDatabase.Builder<SwishyDatabase> {
    val documents: NSURL = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        ),
    ) { "Каталог документов недоступен" }

    return Room.databaseBuilder<SwishyDatabase>(
        name = requireNotNull(documents.path) { "Путь к каталогу документов пуст" } +
            "/" + DATABASE_NAME,
    )
}

internal actual fun nowMillis(): Long =
    (NSDate().timeIntervalSince1970 * MILLIS_IN_SECOND).toLong()

private const val DATABASE_NAME = "swishy.db"
private const val MILLIS_IN_SECOND = 1_000
