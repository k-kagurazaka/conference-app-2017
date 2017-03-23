package io.github.droidkaigi.confsched2017.repository.sessions

import io.github.droidkaigi.confsched2017.model.MySession
import io.github.droidkaigi.confsched2017.model.Session
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Singleton
class MySessionsRepository @Inject constructor(
        private val localDataSource: MySessionsLocalDataSource
) : MySessionsDataSource {

    private val cachedMySessions: MutableMap<Int, MySession> = LinkedHashMap()

    override fun findAll(coroutineContext: CoroutineContext): Deferred<List<MySession>> = async(coroutineContext) {
        if (cachedMySessions.isNotEmpty()) {
            return@async ArrayList(cachedMySessions.values)
        }

        val mySessions = localDataSource.findAll(context).await()
        refreshCache(mySessions)
        return@async mySessions
    }

    override fun save(coroutineContext: CoroutineContext, session: Session): Job = launch(coroutineContext) {
        cachedMySessions.put(session.id, MySession(session = session))
        localDataSource.save(context, session).join()
    }

    override fun delete(coroutineContext: CoroutineContext, session: Session): Job = launch(coroutineContext) {
        cachedMySessions.remove(session.id)
        localDataSource.delete(context, session).join()
    }

    override fun exists(sessionId: Int): Boolean = localDataSource.exists(sessionId)

    private fun refreshCache(mySessions: List<MySession>) {
        cachedMySessions.clear()
        for (mySession in mySessions) {
            cachedMySessions.put(mySession.session.id, mySession)
        }
    }
}
