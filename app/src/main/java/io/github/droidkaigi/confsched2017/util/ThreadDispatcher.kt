package io.github.droidkaigi.confsched2017.util

import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

abstract class ThreadDispatcher {
    protected abstract val UI: CoroutineContext

    protected abstract val worker: CoroutineContext

    fun launchUI(block: suspend CoroutineScope.() -> Unit): Unit {
        launch(UI, block = block)
    }

    fun asyncUI(block: suspend CoroutineScope.() -> Unit): Job = launch(UI, block = block)

    fun launchWorker(block: suspend CoroutineScope.() -> Unit): Job = launch(worker, block = block)

    fun <T> asyncWorker(block: suspend CoroutineScope.() -> T): Deferred<T> = async(worker, block = block)
}
