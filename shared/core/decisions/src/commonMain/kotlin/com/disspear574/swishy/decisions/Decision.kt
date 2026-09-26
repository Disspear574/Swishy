package com.disspear574.swishy.decisions

/** Verdict on a frame: kept, trashed or moved to an album. */
enum class Decision {
    KEPT,
    TRASHED,

    // Hidden from months and collections, but still shown in the album's own deck.
    MOVED,
}
