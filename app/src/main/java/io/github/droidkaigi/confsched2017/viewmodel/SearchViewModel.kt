package io.github.droidkaigi.confsched2017.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.view.View
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SearchViewModel @Inject internal constructor(
        private val navigator: Navigator,
        private val sessionsRepository: SessionsRepository,
        private val mySessionsRepository: MySessionsRepository
) : BaseObservable(), ViewModel {

    private var callback: Callback? = null

    override fun destroy() {
        this.callback = null
    }

    fun onClickCover(@Suppress("UNUSED_PARAMETER") view: View) {
        callback?.closeSearchResultList()
    }

    fun getSearchResultViewModels(coroutineContext: CoroutineContext, context: Context)
            : Deferred<List<SearchResultViewModel>> = async(coroutineContext) {
        val sessions = sessionsRepository.findAll(this.context, Locale.getDefault()).await()

        val filteredSessions = sessions.filter { it.isSession && it.speaker != null }

        val titleResults = filteredSessions.map {
            SearchResultViewModel.createTitleType(it, context, navigator, mySessionsRepository)
        }.toMutableList()

        val descriptionResults = filteredSessions.map {
            SearchResultViewModel.createDescriptionType(it, context, navigator, mySessionsRepository)
        }

        val speakerResults = filteredSessions.map {
            SearchResultViewModel.createSpeakerType(it, context, navigator, mySessionsRepository)
        }

        titleResults.addAll(descriptionResults)
        titleResults.addAll(speakerResults)

        return@async titleResults
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {

        fun closeSearchResultList()
    }

}
