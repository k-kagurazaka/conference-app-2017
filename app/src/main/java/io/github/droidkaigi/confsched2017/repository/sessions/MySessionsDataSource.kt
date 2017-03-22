package io.github.droidkaigi.confsched2017.repository.sessions

import io.github.droidkaigi.confsched2017.model.MySession
import io.github.droidkaigi.confsched2017.model.Session
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlin.coroutines.experimental.CoroutineContext

interface MySessionsDataSource {

    fun findAll(coroutineContext: CoroutineContext): Deferred<List<MySession>>

    fun save(coroutineContext: CoroutineContext, session: Session): Job

    fun delete(coroutineContext: CoroutineContext, session: Session): Job

    fun exists(sessionId: Int): Boolean

}
