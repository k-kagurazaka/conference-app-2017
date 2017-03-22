package io.github.droidkaigi.confsched2017.util

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

annotation class Mockable

fun asyncUI(block: suspend CoroutineScope.() -> Unit) {
    launch(UI, block = block)
}
