package com.disspear574.swishy.decisions.db

import com.disspear574.swishy.media.ImageHash

class HashStore internal constructor(private val dao: ImageHashDao) {

    suspend fun all(): Map<String, ImageHash> =
        dao.all().associate { entity -> entity.assetId to ImageHash(entity.dHash, entity.pHash) }

    suspend fun put(hashes: Map<String, ImageHash>) {
        if (hashes.isEmpty()) return
        dao.putAll(
            hashes.map { (id, hash) ->
                ImageHashEntity(assetId = id, dHash = hash.dHash, pHash = hash.pHash)
            },
        )
    }
}

fun createHashStore(database: SwishyDatabase): HashStore = HashStore(database.imageHashes())
