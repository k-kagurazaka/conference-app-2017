package io.github.droidkaigi.confsched2017.repository.feedbacks

import io.github.droidkaigi.confsched2017.model.SessionFeedback
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Singleton
class SessionFeedbackRepository @Inject constructor(
        private val remoteDataSource: SessionFeedbackRemoteDataSource,
        private val localDataSource: SessionFeedbackLocalDataSource
) {

    fun submit(coroutineContext: CoroutineContext, sessionFeedback: SessionFeedback): Job =
            remoteDataSource.submit(coroutineContext, sessionFeedback)

    fun findFromCache(coroutineContext: CoroutineContext, sessionId: Int): Deferred<SessionFeedback?> =
            localDataSource.find(coroutineContext, sessionId)

    fun saveToCache(coroutineContext: CoroutineContext, sessionFeedback: SessionFeedback): Job =
            localDataSource.save(coroutineContext, sessionFeedback)

}
