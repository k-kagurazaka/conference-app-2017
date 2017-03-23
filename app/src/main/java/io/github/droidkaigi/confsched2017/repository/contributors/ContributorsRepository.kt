package io.github.droidkaigi.confsched2017.repository.contributors

import io.github.droidkaigi.confsched2017.model.Contributor
import io.github.droidkaigi.confsched2017.util.Mockable
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Mockable
@Singleton
class ContributorsRepository @Inject constructor(
        private val localDataSource: ContributorsLocalDataSource,
        private val remoteDataSource: ContributorsRemoteDataSource
) {

    private var cachedContributors: MutableMap<String, Contributor> = LinkedHashMap()

    private var isDirty: Boolean = true

    fun findAll(coroutineContext: CoroutineContext): Deferred<List<Contributor>> = async(coroutineContext) {
        if (cachedContributors.isNotEmpty() && !isDirty) {
            return@async ArrayList(cachedContributors.values)
        }

        if (isDirty) {
            return@async findAllFromRemote(context)
        } else {
            return@async findAllFromLocal(context)
        }
    }

    fun setDirty(isDirty: Boolean) {
        this.isDirty = isDirty
    }

    private suspend fun findAllFromRemote(coroutineContext: CoroutineContext): List<Contributor> {
        val contributors = remoteDataSource.findAll(coroutineContext).await()
        refreshCache(contributors)
        localDataSource.updateAllAsync(contributors)
        return contributors
    }

    private suspend fun findAllFromLocal(coroutineContext: CoroutineContext): List<Contributor> {
        val contributors = localDataSource.findAll(coroutineContext).await()
        if (contributors.isEmpty()) {
            return findAllFromRemote(coroutineContext)
        } else {
            refreshCache(contributors)
            return contributors
        }
    }

    private fun refreshCache(contributors: List<Contributor>) {
        cachedContributors.clear()
        for (contributor in contributors) {
            cachedContributors.put(contributor.name, contributor)
        }
        isDirty = false
    }

}
