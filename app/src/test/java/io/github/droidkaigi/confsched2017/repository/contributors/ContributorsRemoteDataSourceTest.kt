package io.github.droidkaigi.confsched2017.repository.contributors

import com.sys1yagi.kmockito.any
import com.sys1yagi.kmockito.invoked
import com.sys1yagi.kmockito.mock
import com.taroid.knit.should
import io.github.droidkaigi.confsched2017.api.DroidKaigiClient
import io.github.droidkaigi.confsched2017.model.Contributor
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class ContributorsRemoteDataSourceTest {

    private val client = mock<DroidKaigiClient>()

    @Test
    fun findAll() = runBlocking {
        val expected = async(context) {
            listOf(Contributor(
                    name = "Alice",
                    contributions = 10
            ))
        }.apply {
            await()
        }

        client.getContributors(any()).invoked.thenReturn(expected)

        val contributors = ContributorsRemoteDataSource(client).findAll(context).await()
        contributors.size.should be 1
        contributors[0].name.should be "Alice"
    }
}
