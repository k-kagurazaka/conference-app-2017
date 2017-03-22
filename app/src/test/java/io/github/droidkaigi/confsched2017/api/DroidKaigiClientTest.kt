package io.github.droidkaigi.confsched2017.api

import com.sys1yagi.kmockito.invoked
import com.sys1yagi.kmockito.mock
import com.sys1yagi.kmockito.verify
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.api.service.DroidKaigiService
import io.github.droidkaigi.confsched2017.api.service.GithubService
import io.github.droidkaigi.confsched2017.api.service.GoogleFormService
import io.github.droidkaigi.confsched2017.util.DummyCreator
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import retrofit2.mock.Calls
import java.util.*

class DroidKaigiClientTest {

    private val droidKaigiService = mock<DroidKaigiService>()

    private val githubService = mock<GithubService>()

    private val googleFormService = mock<GoogleFormService>()

    private val client = DroidKaigiClient(droidKaigiService, githubService, googleFormService)

    @Test
    @Throws(Exception::class)
    fun getSessions() = runBlocking<Unit> {
        val expected = Array(10) { DummyCreator.newSession(it) }.toList()
        droidKaigiService.getSessionsJa().invoked.thenReturn(Calls.response(expected))
        droidKaigiService.getSessionsEn().invoked.thenReturn(Calls.response(expected))

        run {
            val sessions = client.getSessions(context, Locale.JAPANESE).await()
            sessions.should be expected
        }

        run {
            val sessions = client.getSessions(context, Locale.ENGLISH).await()
            sessions.should be expected
        }

        droidKaigiService.verify().getSessionsJa()
        droidKaigiService.verify().getSessionsEn()
    }

    @Test
    @Throws(Exception::class)
    fun getContributors() = runBlocking<Unit> {
        val expected = Array(10) { DummyCreator.newContributor(it) }.toList()
        githubService.getContributors("DroidKaigi", "conference-app-2017", 1, 100)
                .invoked.thenReturn(Calls.response(expected))

        val contributors = client.getContributors(context).await()
        contributors.should be expected
    }

}
