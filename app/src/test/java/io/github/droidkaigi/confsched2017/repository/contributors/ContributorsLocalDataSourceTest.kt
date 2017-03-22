package io.github.droidkaigi.confsched2017.repository.contributors

import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.model.Contributor
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ContributorsLocalDataSourceTest {

    private lateinit var orma: OrmaHolder

    @Before
    fun setUp() {
        orma = OrmaHolder()
    }

    @Test
    fun findAllWhenEmpty() = runBlocking {
        withTimeout(10, TimeUnit.SECONDS) {
            val contributors = ContributorsLocalDataSource(orma).findAll(context).await()
            contributors.isEmpty().should be true
        }
    }

    @Test
    fun findAllWhenNotEmpty() = runBlocking {
        orma.database.apply {
            insertIntoContributor(Contributor(
                    name = "Alice",
                    contributions = 10
            ))
        }

        withTimeout(10, TimeUnit.SECONDS) {
            val contributors = ContributorsLocalDataSource(orma).findAll(context).await()
            contributors.size.should be 1
            contributors[0].name.should be "Alice"
            contributors[0].contributions.should be 10
        }
    }

    @Test
    fun updateAllAsyncAsInsert() = runBlocking {
        ContributorsLocalDataSource(orma).updateAllAsync(listOf(
                Contributor(
                        name = "Alice",
                        contributions = 10
                ),
                Contributor(
                        name = "Bob",
                        contributions = 20
                ))).join()

        orma.database.selectFromContributor().toList().run {
            size.should be 2
            this[1].name.should be "Bob"
        }
    }

    @Test
    fun updateAllAsyncAsUpdate() = runBlocking {
        orma.database.apply {
            insertIntoContributor(Contributor(
                    name = "Alice",
                    contributions = 10
            ))
        }

        ContributorsLocalDataSource(orma).updateAllAsync(listOf(
                Contributor(
                        name = "Alice",
                        contributions = 100
                )
        )).join()

        orma.database.selectFromContributor().toList().run {
            size.should be 1
            this[0].contributions.should be 100
        }
    }

}
