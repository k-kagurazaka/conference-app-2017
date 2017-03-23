package io.github.droidkaigi.confsched2017.repository.contributors

import com.sys1yagi.kmockito.any
import com.sys1yagi.kmockito.invoked
import com.sys1yagi.kmockito.mock
import com.sys1yagi.kmockito.verify
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.model.Contributor
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.mockito.Mockito.never

class ContributorsRepositoryTest {

    private companion object {
        val CONTRIBUTORS = listOf(
                Contributor(
                        name = "Alice",
                        contributions = 10
                ),
                Contributor(
                        name = "Bob",
                        contributions = 20
                ),
                Contributor(
                        name = "Charlie",
                        contributions = 30
                )
        )
    }

    private val localDataSource = mock<ContributorsLocalDataSource>()
    private val remoteDataSource = mock<ContributorsRemoteDataSource>()

    private val repository = ContributorsRepository(localDataSource, remoteDataSource)

    @Test
    @Throws(Exception::class)
    fun findAllFromEmptyRepository(): Unit = runBlocking {
        val expected = async(context) { listOf<Contributor>() }.apply { await() }
        localDataSource.findAll(any()).invoked.thenReturn(expected)
        remoteDataSource.findAll(any()).invoked.thenReturn(expected)
        val contributors = repository.findAll(context).await()
        contributors.isEmpty().should be true
    }

    @Test
    @Throws(Exception::class)
    fun updateLocalWhenRemoteReturns(): Unit = runBlocking<Unit> {
        val empty = async(context) { listOf<Contributor>() }.apply { await() }
        val expected = async(context) { CONTRIBUTORS }.apply { await() }
        localDataSource.findAll(any()).invoked.thenReturn(empty)
        remoteDataSource.findAll(any()).invoked.thenReturn(expected)
        repository.findAll(context).await()
        localDataSource.verify().updateAllAsync(CONTRIBUTORS)
    }

    @Test
    @Throws(Exception::class)
    fun returnCache(): Unit = runBlocking<Unit> {
        val empty = async(context) { listOf<Contributor>() }.apply { await() }
        val expected = async(context) { CONTRIBUTORS }.apply { await() }
        localDataSource.findAll(any()).invoked.thenReturn(empty)
        remoteDataSource.findAll(any()).invoked.thenReturn(expected)

        repository.findAll(context).await()
        repository.findAll(context).await()
        localDataSource.verify(never()).findAll(any())
        remoteDataSource.verify().findAll(any())
    }

    @Test
    @Throws(Exception::class)
    fun findAllFromLocalDataSourceWhenNotDirty(): Unit = runBlocking<Unit> {
        val empty = async(context) { listOf<Contributor>() }.apply { await() }
        val expected = async(context) { CONTRIBUTORS }.apply { await() }
        localDataSource.findAll(any()).invoked.thenReturn(expected)
        remoteDataSource.findAll(any()).invoked.thenReturn(empty)
        repository.setDirty(false)

        repository.findAll(context).await()
        localDataSource.verify().findAll(any())
        remoteDataSource.verify(never()).findAll(any())
    }

    @Test
    @Throws(Exception::class)
    fun findAllFromRemoteDataSourceWhenLocalDataSourceReturnsEmptyResult(): Unit = runBlocking<Unit> {
        val empty = async(context) { listOf<Contributor>() }.apply { await() }
        val expected = async(context) { CONTRIBUTORS }.apply { await() }
        localDataSource.findAll(any()).invoked.thenReturn(empty)
        remoteDataSource.findAll(any()).invoked.thenReturn(expected)
        repository.setDirty(false)

        repository.findAll(context).await()
        localDataSource.verify().findAll(any())
        remoteDataSource.verify().findAll(any())
    }

}
