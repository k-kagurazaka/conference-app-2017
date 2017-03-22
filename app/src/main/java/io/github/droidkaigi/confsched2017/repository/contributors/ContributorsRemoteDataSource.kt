package io.github.droidkaigi.confsched2017.repository.contributors

import io.github.droidkaigi.confsched2017.api.DroidKaigiClient
import io.github.droidkaigi.confsched2017.model.Contributor
import kotlinx.coroutines.experimental.Deferred
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class ContributorsRemoteDataSource @Inject internal constructor(private val client: DroidKaigiClient) {

    internal fun findAll(coroutineContext: CoroutineContext): Deferred<List<Contributor>> =
            client.getContributors(coroutineContext)

}
