package io.github.droidkaigi.confsched2017.repository.sessions

import io.github.droidkaigi.confsched2017.api.DroidKaigiClient
import io.github.droidkaigi.confsched2017.model.Session
import kotlinx.coroutines.experimental.*
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SessionsRemoteDataSource @Inject constructor(private val client: DroidKaigiClient) : SessionsDataSource {

    override fun findAll(coroutineContext: CoroutineContext, locale: Locale): Deferred<List<Session>> =
            async(coroutineContext) {
                client.getSessions(context, locale).await()
                        .apply {
                            // API returns some sessions which have empty room info.
                            for (session in this) {
                                if (session.room?.name.isNullOrEmpty()) {
                                    session.room = null
                                }
                            }
                        }
            }

    override fun find(coroutineContext: CoroutineContext, sessionId: Int, locale: Locale): Deferred<Session?> =
            async(coroutineContext) {
                findAll(context, locale).await().firstOrNull { it.id == sessionId }
            }

    override fun updateAllAsync(sessions: List<Session>): Job = launch(CommonPool) {
        // Do nothing
    }

    override fun deleteAll() {
        // Do nothing
    }
}
