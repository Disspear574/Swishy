package com.disspear574.swishy.media

data class HashedAsset(val id: String, val hash: ImageHash)

/** IDENTICAL means every hash in the group is equal; SIMILAR covers bursts and crops. */
enum class DuplicateKind {
    IDENTICAL,
    SIMILAR,
}

data class DuplicateGroup(val kind: DuplicateKind, val ids: List<String>)

fun groupDuplicates(assets: List<HashedAsset>): List<DuplicateGroup> {
    if (assets.size < 2) return emptyList()

    val union = DisjointSets(assets.size)

    val byHash = mutableMapOf<ImageHash, Int>()
    assets.forEachIndexed { index, asset ->
        val first = byHash.getOrPut(asset.hash) { index }
        if (first != index) union.join(first, index)
    }

    // One more band than the dHash threshold: pairs within the threshold always share a band.
    val byBand = mutableMapOf<Long, MutableList<Int>>()
    assets.forEachIndexed { index, asset ->
        for (band in 0 until BANDS) {
            val key = bandKey(asset.hash.dHash, band)
            byBand.getOrPut(key) { mutableListOf() }.add(index)
        }
    }
    byBand.values
        // Overflowing bands hold flat frames; their exact matches are already joined above.
        .filter { bucket -> bucket.size in 2..BAND_OVERFLOW }
        .forEach { bucket -> joinSimilar(assets, bucket, union) }

    return union.groups()
        .filter { it.size > 1 }
        .map { members ->
            val hashes = members.map { assets[it].hash }.toSet()
            DuplicateGroup(
                kind = if (hashes.size == 1) DuplicateKind.IDENTICAL else DuplicateKind.SIMILAR,
                ids = members.map { assets[it].id },
            )
        }
}

private fun joinSimilar(assets: List<HashedAsset>, bucket: List<Int>, union: DisjointSets) {
    for (i in bucket.indices) {
        for (j in i + 1 until bucket.size) {
            if (isSimilar(assets[bucket[i]].hash, assets[bucket[j]].hash)) {
                union.join(bucket[i], bucket[j])
            }
        }
    }
}

private fun isSimilar(first: ImageHash, second: ImageHash): Boolean =
    hammingDistance(first.dHash, second.dHash) <= DHASH_THRESHOLD &&
        hammingDistance(first.pHash, second.pHash) <= PHASH_THRESHOLD

private fun bandKey(hash: Long, band: Int): Long {
    val start = band * HASH_BITS / BANDS
    val width = (band + 1) * HASH_BITS / BANDS - start
    val slice = (hash ushr start) and ((1L shl width) - 1)
    // The band index is part of the key: equal bits in different bands are not a match.
    return slice * BANDS + band
}

private class DisjointSets(size: Int) {
    private val parent = IntArray(size) { it }

    fun find(item: Int): Int {
        var root = item
        while (parent[root] != root) root = parent[root]
        var walk = item
        while (parent[walk] != root) {
            val next = parent[walk]
            parent[walk] = root
            walk = next
        }
        return root
    }

    fun join(first: Int, second: Int) {
        val a = find(first)
        val b = find(second)
        if (a != b) parent[b] = a
    }

    fun groups(): List<List<Int>> {
        val members = mutableMapOf<Int, MutableList<Int>>()
        parent.indices.forEach { index ->
            members.getOrPut(find(index)) { mutableListOf() }.add(index)
        }
        return members.values.toList()
    }
}

private const val DHASH_THRESHOLD = 8
private const val PHASH_THRESHOLD = 10

private const val HASH_BITS = 64
private const val BANDS = DHASH_THRESHOLD + 1

internal const val BAND_OVERFLOW = 400
