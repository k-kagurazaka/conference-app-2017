package io.github.droidkaigi.confsched2017.repository.feedbacks

import com.github.gfx.android.orma.annotation.OnConflict
import io.github.droidkaigi.confsched2017.model.SessionFeedback
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SessionFeedbackLocalDataSource @Inject constructor(private val orma: OrmaHolder) {

    fun save(coroutineContext: CoroutineContext, sessionFeedback: SessionFeedback): Job = launch(coroutineContext) {
        orma.database.transactionSync {
            orma.database.prepareInsertIntoSessionFeedback(OnConflict.REPLACE).execute(sessionFeedback)
        }
    }

    fun find(coroutineContext: CoroutineContext, sessionId: Int): Deferred<SessionFeedback?> = async(coroutineContext) {
        orma.database.relationOfSessionFeedback().selector().sessionIdEq(sessionId).valueOrNull()
    }
}
