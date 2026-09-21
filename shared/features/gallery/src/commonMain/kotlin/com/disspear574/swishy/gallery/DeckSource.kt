package com.disspear574.swishy.gallery

import com.disspear574.swishy.media.AlbumKind
import com.disspear574.swishy.media.MonthKey

sealed interface DeckSource {
    data class Month(val key: MonthKey) : DeckSource
    data object Mix : DeckSource
    data class Album(val kind: AlbumKind) : DeckSource
}
