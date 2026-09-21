package com.disspear574.swishy.gallery

import com.disspear574.swishy.decisions.db.HashStore
import com.disspear574.swishy.media.DuplicateGroup
import com.disspear574.swishy.media.HashedAsset
import com.disspear574.swishy.media.ImageHash
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.grayThumbnail
import com.disspear574.swishy.media.groupDuplicates
import com.disspear574.swishy.media.perceptualHash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DuplicateScanner(
    private val index: MediaIndex,
    private val hashStore: HashStore,
) {

    data class Progress(val done: Int, val total: Int) {
        val isFinished: Boolean get() = total > 0 && done >= total
    }

    private val _progress = MutableStateFlow(Progress(done = 0, total = 0))
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    private val _groups = MutableStateFlow<List<DuplicateGroup>?>(null)
    val groups: StateFlow<List<DuplicateGroup>?> = _groups.asStateFlow()

    suspend fun scan() {
        index.load()
        val assets = index.assets.value.orEmpty()
        val known = hashStore.all().toMutableMap()

        val missing = assets.filter { asset -> asset.id !in known }
        _progress.value = Progress(done = assets.size - missing.size, total = assets.size)

        val batch = mutableMapOf<String, ImageHash>()
        missing.forEach { asset ->
            grayThumbnail(asset.id, THUMBNAIL_SIDE)?.let { gray ->
                val hash = perceptualHash(gray, THUMBNAIL_SIDE)
                known[asset.id] = hash
                batch[asset.id] = hash
            }
            _progress.value = _progress.value.copy(done = _progress.value.done + 1)
            if (batch.size >= BATCH) {
                hashStore.put(batch.toMap())
                batch.clear()
            }
        }
        hashStore.put(batch.toMap())

        val hashed = assets.mapNotNull { asset ->
            known[asset.id]?.let { hash -> HashedAsset(asset.id, hash) }
        }
        _groups.value = groupDuplicates(hashed)
    }

    private companion object {
        const val THUMBNAIL_SIDE = 32

        const val BATCH = 200
    }
}
