package io.github.droidkaigi.confsched2017.repository.sessions

import com.sys1yagi.kmockito.any
import com.sys1yagi.kmockito.invoked
import com.sys1yagi.kmockito.mock
import com.sys1yagi.kmockito.verify
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.model.MySession
import io.github.droidkaigi.confsched2017.util.DummyCreator
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.mockito.Mockito

class MySessionsRepositoryTest {

    private val localDataSource = mock<MySessionsLocalDataSource>()
    private val repository = MySessionsRepository(localDataSource)

    @Test
    @Throws(Exception::class)
    fun findAll() = runBlocking {
        val expected = async(context) { List(10) { i -> DummyCreator.newMySession(i) } }.apply { await() }
        localDataSource.findAll(any()).invoked.thenReturn(expected)

        val mySessions = repository.findAll(context).await()
        mySessions.should be expected.await()
        localDataSource.verify().findAll(any())

        // check if found sessions are cached
        repository.findAll(context).await()
        localDataSource.verify().findAll(any())
    }

    @Test
    @Throws(Exception::class)
    fun save() = runBlocking {
        val session = DummyCreator.newSession(1)
        localDataSource.save(context, session).invoked.thenReturn(Job())
        repository.save(context, session).join()

        // check if session is cached
        val mySessions = repository.findAll(context).await()
        mySessions.should be listOf(MySession(session = session))
        localDataSource.verify(Mockito.never()).findAll(any())
    }

    @Test
    @Throws(Exception::class)
    fun delete() = runBlocking {
        val session1 = DummyCreator.newSession(1)
        val session2 = DummyCreator.newSession(2)

        // ready caches
        repository.save(context, session1).join()
        repository.save(context, session2).join()

        localDataSource.delete(context, session1).invoked.thenReturn(Job())
        repository.delete(context, session1).join()

        // check if cached session1 is deleted
        val mySessions = repository.findAll(context).await()
        mySessions.should be listOf(MySession(session = session2))
    }

    @Test
    @Throws(Exception::class)
    fun isExist() {
        localDataSource.exists(1).invoked.thenReturn(false)
        repository.exists(1).should be false

        localDataSource.exists(1).invoked.thenReturn(true)
        repository.exists(1).should be true
    }

}
