package com.disspear574.swishy.media

// No-op: files are local on Android, and the deck already decodes the next card at full size.
actual fun prefetchMedia(ids: List<String>) = Unit
