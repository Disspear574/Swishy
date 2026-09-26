package com.disspear574.swishy.media

/** Platform handles that expect functions cannot take as parameters; filled once at startup. */
expect object MediaContext {

    val isReady: Boolean
}
