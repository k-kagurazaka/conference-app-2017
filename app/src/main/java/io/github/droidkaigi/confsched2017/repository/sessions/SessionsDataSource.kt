package io.github.droidkaigi.confsched2017.repository.sessions

import io.github.droidkaigi.confsched2017.model.Session
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

interface SessionsDataSource {

    fun findAll(coroutineContext: CoroutineContext, locale: Locale): Deferred<List<Session>>

    fun find(coroutineContext: CoroutineContext, sessionId: Int, locale: Locale): Deferred<Session?>

    fun updateAllAsync(sessions: List<Session>): Job

    fun deleteAll()
}
