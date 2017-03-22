package io.github.droidkaigi.confsched2017.api

import io.github.droidkaigi.confsched2017.api.service.DroidKaigiService
import io.github.droidkaigi.confsched2017.api.service.GithubService
import io.github.droidkaigi.confsched2017.api.service.GoogleFormService
import io.github.droidkaigi.confsched2017.model.SessionFeedback
import io.github.droidkaigi.confsched2017.util.Mockable
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Mockable
@Singleton
class DroidKaigiClient @Inject constructor(
        private val droidKaigiService: DroidKaigiService,
        private val githubService: GithubService,
        private val googleFormService: GoogleFormService
) {

    fun getSessions(coroutineContext: CoroutineContext, locale: Locale) = async(coroutineContext) {
        when (locale) {
            Locale.JAPANESE -> droidKaigiService.getSessionsJa()
            else -> droidKaigiService.getSessionsEn()
        }.getResultOrThrow()
    }

    fun getContributors(coroutineContext: CoroutineContext) = async(coroutineContext) {
        githubService.getContributors("DroidKaigi", "conference-app-2017", INCLUDE_ANONYMOUS, MAX_PER_PAGE)
                .getResultOrThrow()
    }

    fun submitSessionFeedback(coroutineContext: CoroutineContext, sessionFeedback: SessionFeedback): Job =
            launch(coroutineContext) {
                googleFormService.submitSessionFeedback(
                        sessionFeedback.sessionId,
                        sessionFeedback.sessionTitle,
                        sessionFeedback.relevancy,
                        sessionFeedback.asExpected,
                        sessionFeedback.difficulty,
                        sessionFeedback.knowledgeable,
                        sessionFeedback.comment
                ).execute().let {
                    if (!it.isSuccessful) throw RuntimeException()
                }
            }

    private fun <T> Call<T>.getResultOrThrow(): T {
        val response = execute()
        return if (response.isSuccessful) response.body() else throw RuntimeException()
    }

    companion object {

        private val INCLUDE_ANONYMOUS = 1

        private val MAX_PER_PAGE = 100
    }
}
