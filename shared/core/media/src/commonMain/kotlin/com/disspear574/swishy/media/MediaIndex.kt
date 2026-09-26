package com.disspear574.swishy.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The library read once and shared by all screens; file sizes are filled in later, in batches. */
class MediaIndex(
    private val library: MediaLibrary,
    private val scope: CoroutineScope,
) {

    private val loadLock = Mutex()

    private val _assets = MutableStateFlow<List<MediaAsset>?>(null)
    val assets: StateFlow<List<MediaAsset>?> = _assets.asStateFlow()

    private val _sizes = MutableStateFlow<Map<String, Long>>(emptyMap())
    val sizes: StateFlow<Map<String, Long>> = _sizes.asStateFlow()

    suspend fun load() {
        loadLock.withLock {
            if (_assets.value != null) return
            val scanned = library.allAssets()
            _assets.value = scanned
            _sizes.value = scanned.filter { it.sizeBytes > 0 }.associate { it.id to it.sizeBytes }
            if (scanned.any { it.sizeBytes <= 0 }) {
                scope.launch { fillSizes(scanned) }
            }
        }
    }

    fun invalidate() {
        _assets.value = null
        _sizes.value = emptyMap()
    }

    suspend fun sizeOf(id: String): Long {
        _sizes.value[id]?.let { return it }
        val size = library.sizeOf(listOf(id))[id] ?: 0
        if (size > 0) {
            _sizes.update { it + (id to size) }
        }
        return size
    }

    private suspend fun fillSizes(scanned: List<MediaAsset>) {
        scanned.asSequence()
            .filter { it.sizeBytes <= 0 }
            .map { it.id }
            .chunked(CHUNK)
            .forEach { chunk ->
                val batch = library.sizeOf(chunk)
                _sizes.update { it + batch }
            }
    }

    private companion object {
        const val CHUNK = 400
    }
}
