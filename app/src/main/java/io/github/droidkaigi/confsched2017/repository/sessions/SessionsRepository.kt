package io.github.droidkaigi.confsched2017.repository.sessions

import android.support.annotation.VisibleForTesting
import io.github.droidkaigi.confsched2017.model.Session
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Singleton
class SessionsRepository @Inject constructor(
        private val localDataSource: SessionsLocalDataSource,
        private val remoteDataSource: SessionsRemoteDataSource
) : SessionsDataSource {

    @VisibleForTesting internal var cachedSessions: MutableMap<Int, Session> = LinkedHashMap()

    private var isDirty: Boolean = true

    override fun findAll(coroutineContext: CoroutineContext, locale: Locale): Deferred<List<Session>> =
            async(coroutineContext) {
                if (hasCacheSessions()) {
                    return@async ArrayList(cachedSessions.values)
                }

                if (isDirty) {
                    return@async findAllFromRemote(context, locale)
                } else {
                    return@async findAllFromLocal(context, locale)
                }
            }

    override fun find(coroutineContext: CoroutineContext, sessionId: Int, locale: Locale): Deferred<Session?> =
            async(coroutineContext) {
                if (hasCacheSession(sessionId)) {
                    return@async cachedSessions[sessionId]
                }

                if (isDirty) {
                    return@async remoteDataSource.find(context, sessionId, locale).await()
                } else {
                    return@async localDataSource.find(context, sessionId, locale).await()
                }
            }

    override fun updateAllAsync(sessions: List<Session>): Job = localDataSource.updateAllAsync(sessions)

    /**
     * Clear all caches. only for debug purposes
     */
    override fun deleteAll() {
        cachedSessions.clear()
        localDataSource.deleteAll()
        isDirty = true
    }

    fun setDirty(isDirty: Boolean) {
        this.isDirty = isDirty
    }

    private suspend fun findAllFromLocal(coroutineContext: CoroutineContext, locale: Locale): List<Session> {
        val sessions = localDataSource.findAll(coroutineContext, locale).await()
        if (sessions.isEmpty()) {
            return findAllFromRemote(coroutineContext, locale)
        } else {
            refreshCache(sessions)
            return sessions
        }
    }

    private suspend fun findAllFromRemote(coroutineContext: CoroutineContext, locale: Locale): List<Session> {
        val sessions = remoteDataSource.findAll(coroutineContext, locale).await()
        refreshCache(sessions)
        updateAllAsync(sessions) // fire & forget
        return sessions
    }

    private fun refreshCache(sessions: List<Session>) {
        cachedSessions.clear()
        for (session in sessions) {
            cachedSessions.put(session.id, session)
        }
        isDirty = false
    }

    internal fun hasCacheSessions(): Boolean {
        return cachedSessions.isNotEmpty() && !isDirty
    }

    internal fun hasCacheSession(sessionId: Int): Boolean {
        return cachedSessions.containsKey(sessionId) && !isDirty
    }

}
