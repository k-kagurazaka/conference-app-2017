package io.github.droidkaigi.confsched2017.repository.contributors

import com.github.gfx.android.orma.annotation.OnConflict
import io.github.droidkaigi.confsched2017.model.Contributor
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import kotlinx.coroutines.experimental.*
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class ContributorsLocalDataSource @Inject internal constructor(private val orma: OrmaHolder) {

    fun findAll(coroutineContext: CoroutineContext): Deferred<List<Contributor>> = async(coroutineContext) {
        orma.database.selectFromContributor().toList()
    }

    internal fun updateAllAsync(contributors: List<Contributor>): Job = launch(CommonPool) {
        orma.database.transactionSync {
            orma.database.prepareInsertIntoContributor(OnConflict.REPLACE).executeAll(contributors)
        }
    }
}
