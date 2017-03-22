package io.github.droidkaigi.confsched2017.repository.feedbacks

import io.github.droidkaigi.confsched2017.api.DroidKaigiClient
import io.github.droidkaigi.confsched2017.model.SessionFeedback
import kotlinx.coroutines.experimental.Job
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SessionFeedbackRemoteDataSource @Inject constructor(private val client: DroidKaigiClient) {

    fun submit(coroutineContext: CoroutineContext, sessionFeedback: SessionFeedback): Job =
            client.submitSessionFeedback(coroutineContext, sessionFeedback)
}
