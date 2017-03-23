package io.github.droidkaigi.confsched2017.repository.sessions

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.model.MySession
import io.github.droidkaigi.confsched2017.util.DummyCreator
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MySessionsRepositoryTest {

    private lateinit var completedJob: Job

    @Before
    fun setUp() = runBlocking {
        completedJob = launch(context) {}.apply { join() }
    }

    @Test
    @Throws(Exception::class)
    fun findAll(): Unit = runBlocking<Unit> {
        val expected = async(context) { List(10) { i -> DummyCreator.newMySession(i) } }.apply { await() }
        val localDataSource = mock<MySessionsLocalDataSource> {
            on { findAll(any()) } doReturn expected
        }
        val repository = MySessionsRepository(localDataSource)

        val mySessions = repository.findAll(context).await()
        mySessions.should be expected.await()
        verify(localDataSource).findAll(any())

        // check if found sessions are cached
        repository.findAll(context).await()
        verify(localDataSource).findAll(any())
    }

    @Test
    @Throws(Exception::class)
    fun save(): Unit = runBlocking<Unit> {
        val localDataSource = mock<MySessionsLocalDataSource> {
            on { save(any(), any()) } doReturn completedJob
        }
        val repository = MySessionsRepository(localDataSource)

        val session = DummyCreator.newSession(1)
        repository.save(context, session).join()

        // check if session is cached
        val mySessions = repository.findAll(context).await()
        mySessions.should be listOf(MySession(session = session))
        verify(localDataSource, Mockito.never()).findAll(any())
    }

    @Test
    @Throws(Exception::class)
    fun delete(): Unit = runBlocking {
        val localDataSource = mock<MySessionsLocalDataSource> {
            on { save(any(), any()) } doReturn completedJob
            on { delete(any(), any()) } doReturn completedJob
        }
        val repository = MySessionsRepository(localDataSource)

        val session1 = DummyCreator.newSession(1)
        val session2 = DummyCreator.newSession(2)

        // ready caches
        repository.save(context, session1).join()
        repository.save(context, session2).join()
        repository.delete(context, session1).join()

        // check if cached session1 is deleted
        val mySessions = repository.findAll(context).await()
        mySessions.should be listOf(MySession(session = session2))
    }

    @Test
    @Throws(Exception::class)
    fun isExist() {
        val localDataSource = mock<MySessionsLocalDataSource> {
            on { exists(1) } doReturn false
            on { exists(2) } doReturn true
        }
        val repository = MySessionsRepository(localDataSource)

        repository.exists(1).should be false
        repository.exists(2).should be true
    }

}
