package com.disspear574.swishy.gallery

import com.disspear574.swishy.media.AlbumKind
import com.disspear574.swishy.media.MonthKey

/** Where the deck takes its photos from. */
sealed interface DeckSource {
    data class Month(val key: MonthKey) : DeckSource
    data object Mix : DeckSource
    data class Album(val kind: AlbumKind) : DeckSource

    /** A user album; the title travels with the id so the screen needs no library lookup. */
    data class UserAlbum(val id: String, val title: String) : DeckSource
}
