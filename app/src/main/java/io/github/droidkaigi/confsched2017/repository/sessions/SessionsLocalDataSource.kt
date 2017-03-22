package io.github.droidkaigi.confsched2017.repository.sessions

import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import kotlinx.coroutines.experimental.*
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SessionsLocalDataSource @Inject constructor(private val orma: OrmaHolder) : SessionsDataSource {

    override fun findAll(coroutineContext: CoroutineContext, locale: Locale): Deferred<List<Session>> =
            async(coroutineContext) {
                sessionRelation().selector().toList()
            }

    override fun find(coroutineContext: CoroutineContext, sessionId: Int, locale: Locale): Deferred<Session?> =
            async(coroutineContext) {
                sessionRelation().selector().idEq(sessionId).valueOrNull()
            }

    override fun deleteAll() {
        sessionRelation().deleter().execute()
        speakerRelation().deleter().execute()
        topicRelation().deleter().execute()
        placeRelation().deleter().execute()
    }

    override fun updateAllAsync(sessions: List<Session>): Job = launch(CommonPool) {
        orma.database.transactionSync {
            val relation = sessionRelation()
            for (session in sessions) {
                relation.upsert(session)
            }
        }
    }

    private fun sessionRelation() = orma.database.relationOfSession()

    private fun speakerRelation() = orma.database.relationOfSpeaker()

    private fun placeRelation() = orma.database.relationOfRoom()

    private fun topicRelation() = orma.database.relationOfTopic()

}
