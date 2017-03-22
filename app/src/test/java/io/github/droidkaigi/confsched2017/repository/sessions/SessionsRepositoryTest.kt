package io.github.droidkaigi.confsched2017.repository.sessions

import com.sys1yagi.kmockito.invoked
import com.sys1yagi.kmockito.mock
import com.sys1yagi.kmockito.verify
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.api.DroidKaigiClient
import io.github.droidkaigi.confsched2017.model.OrmaDatabase
import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import io.github.droidkaigi.confsched2017.util.DummyCreator
import io.reactivex.Completable
import io.reactivex.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class SessionsRepositoryTest {

    @Test
    fun hasCacheSessions() {
        // false. cache is null.
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(OrmaHolder()),
                    SessionsRemoteDataSource(mock())
            )

            repository.hasCacheSessions().should be false
        }

        // false. repository has any cached session, but repository is dirty.
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(OrmaHolder()),
                    SessionsRemoteDataSource(mock())
            )
            repository.cachedSessions = mutableMapOf(0 to createSession(0))
            repository.setDirty(true)

            repository.hasCacheSessions().should be false
        }

        // true.
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(OrmaHolder()),
                    SessionsRemoteDataSource(mock())
            )
            repository.cachedSessions = mutableMapOf(0 to createSession(0))
            repository.setDirty(false)

            repository.hasCacheSessions().should be true
        }
    }
    // TODO rewrite test
/*
    @Test
    fun findAllRemoteRequestAndLocalCache() {
        val sessions = listOf(createSession(0))
        val client = mockDroidKaigiClient(sessions)
        val ormaDatabase = mock<OrmaDatabase>().apply {
            transactionAsCompletable(any()).invoked.thenReturn(Completable.complete())
        }
        val cachedSessions: Map<Int, Session> = spy(mutableMapOf())

        val repository = SessionsRepository(
                SessionsLocalDataSource(ormaDatabase),
                SessionsRemoteDataSource(client)
        ).apply {
            this.cachedSessions = cachedSessions
        }

        repository.findAll(Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertResult(sessions)
                    assertComplete()

                    client.verify().getSessions(eq(Locale.JAPANESE))
                    ormaDatabase.verify().transactionAsCompletable(any())
                    cachedSessions.verify(never()).values
                }

        repository.findAll(Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertThat(values().first().size).isEqualTo(1)
                    assertComplete()

                    cachedSessions.verify().values
                }
    }

    @Test
    fun findAllLocalCache() {
        val sessions = listOf(createSession(0))
        val client = mockDroidKaigiClient(sessions)
        val cachedSessions: Map<Int, Session> = mock()

        val repository = SessionsRepository(
                SessionsLocalDataSource(createOrmaDatabase()),
                SessionsRemoteDataSource(client)
        ).apply {
            this.cachedSessions = cachedSessions
        }

        repository.findAll(Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertResult(sessions)
                    assertComplete()

                    client.verify().getSessions(eq(Locale.JAPANESE))
                    cachedSessions.verify(never()).values
                }

        repository.setDirty(true)

        repository.findAll(Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertThat(values().first().size).isEqualTo(1)
                    assertComplete()

                    cachedSessions.verify(never()).values
                }
    }

    @Test
    fun hasCacheSession() {
        // false cachedSessions is null
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(createOrmaDatabase()),
                    SessionsRemoteDataSource(mock())
            )

            assertThat(repository.hasCacheSession(0)).isFalse()
        }

        // false sessionId not found
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(createOrmaDatabase()),
                    SessionsRemoteDataSource(mock())
            )
            repository.cachedSessions = mapOf(1 to createSession(0))
            repository.setDirty(false)

            assertThat(repository.hasCacheSession(0)).isFalse()
        }

        // false dirty
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(createOrmaDatabase()),
                    SessionsRemoteDataSource(mock())
            )
            repository.cachedSessions = mapOf(1 to createSession(0))
            repository.setDirty(true)

            assertThat(repository.hasCacheSession(1)).isFalse()
        }

        // true
        run {
            val repository = SessionsRepository(
                    SessionsLocalDataSource(createOrmaDatabase()),
                    SessionsRemoteDataSource(mock())
            )
            repository.cachedSessions = mapOf(1 to createSession(0))
            repository.setDirty(false)

            assertThat(repository.hasCacheSession(1)).isTrue()
        }
    }

    @Test
    fun findRemoteRequestSuccess() {
        val sessions = listOf(createSession(2), createSession(3))
        val client = mockDroidKaigiClient(sessions)

        val repository = SessionsRepository(
                SessionsLocalDataSource(createOrmaDatabase()),
                SessionsRemoteDataSource(client)
        )

        repository.find(3, Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertThat(values().first().id).isEqualTo(3)
                    assertComplete()
                }
    }

    @Test
    fun findRemoteRequestNotFound() {
        val sessions = listOf(createSession(1), createSession(2))
        val client = mockDroidKaigiClient(sessions)

        val repository = SessionsRepository(
                SessionsLocalDataSource(createOrmaDatabase()),
                SessionsRemoteDataSource(client)
        )

        repository.find(3, Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertNoValues()
                    assertComplete()
                }
    }

    @Test
    fun findHasSessions() {
        val cachedSessions: Map<Int, Session> = spy(mutableMapOf(1 to createSession(1)))

        val repository = SessionsRepository(
                SessionsLocalDataSource(createOrmaDatabase()),
                SessionsRemoteDataSource(mock())
        ).apply {
            this.cachedSessions = cachedSessions
        }

        repository.setDirty(false)
        repository.find(1, Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertThat(values().first().id).isEqualTo(1)
                    assertComplete()
                    cachedSessions.verify().get(eq(1))
                }
    }

    @Test
    fun findLocalStorage() {
        val ormaDatabase = createOrmaDatabase()
        ormaDatabase
                .insertIntoSession(createSession(12).apply {
                    title = "awesome session"
                    stime = Date()
                    etime = Date()
                    durationMin = 30
                    type = "android"
                })

        val cachedSessions: Map<Int, Session> = mock()
        val client: DroidKaigiClient = mock()
        val repository = SessionsRepository(
                SessionsLocalDataSource(ormaDatabase),
                SessionsRemoteDataSource(client)
        )

        repository.cachedSessions = cachedSessions
        repository.setDirty(false)
        repository.find(12, Locale.JAPANESE)
                .test()
                .run {
                    assertNoErrors()
                    assertThat(values().first().id).isEqualTo(12)
                    assertComplete()
                    cachedSessions.verify(never()).get(eq(12))
                    client.verify(never()).getSessions(any<Locale>())
                }
    }

    fun mockDroidKaigiClient(sessions: List<Session>) = mock<DroidKaigiClient>().apply {
        getSessions(any<Locale>()).invoked.thenReturn(
                Single.just(sessions)
        )
    }*/

    fun createSession(sessionId: Int) = DummyCreator.newSession(sessionId)

}
