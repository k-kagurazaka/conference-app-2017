package io.github.droidkaigi.confsched2017.repository.sessions

import io.github.droidkaigi.confsched2017.model.MySession
import io.github.droidkaigi.confsched2017.model.MySession_Relation
import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import io.github.droidkaigi.confsched2017.util.Mockable
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

@Mockable
class MySessionsLocalDataSource @Inject constructor(private val orma: OrmaHolder) : MySessionsDataSource {


    override fun findAll(coroutineContext: CoroutineContext): Deferred<List<MySession>> = async(coroutineContext) {
        if (mySessionRelation().isEmpty) {
            return@async emptyList<MySession>()
        } else {
            return@async mySessionRelation().selector().toList()
        }
    }

    override fun save(coroutineContext: CoroutineContext, session: Session): Job = launch(coroutineContext) {
        orma.database.transactionSync {
            mySessionRelation().deleter().sessionEq(session.id).execute()
            mySessionRelation().inserter().execute(MySession(session = session))
        }
    }

    override fun delete(coroutineContext: CoroutineContext, session: Session): Job = launch(coroutineContext) {
        mySessionRelation().deleter().sessionEq(session.id).execute()
    }

    override fun exists(sessionId: Int): Boolean {
        return !mySessionRelation().selector().sessionEq(sessionId).isEmpty
    }

    private fun mySessionRelation(): MySession_Relation {
        return orma.database.relationOfMySession()
    }
}
