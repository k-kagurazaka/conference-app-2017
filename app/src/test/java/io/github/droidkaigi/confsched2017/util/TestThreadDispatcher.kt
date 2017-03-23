package io.github.droidkaigi.confsched2017.util

import kotlinx.coroutines.experimental.Unconfined
import kotlin.coroutines.experimental.CoroutineContext

object TestThreadDispatcher : ThreadDispatcher() {
    override val UI: CoroutineContext
        get() = Unconfined

    override val worker: CoroutineContext
        get() = Unconfined
}
