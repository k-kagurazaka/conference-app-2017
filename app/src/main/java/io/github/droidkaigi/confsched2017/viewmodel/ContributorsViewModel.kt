package io.github.droidkaigi.confsched2017.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.annotation.StringRes
import android.view.View
import io.github.droidkaigi.confsched2017.BR
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.di.scope.FragmentScope
import io.github.droidkaigi.confsched2017.repository.contributors.ContributorsRepository
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import io.github.droidkaigi.confsched2017.view.helper.ResourceResolver
import io.github.droidkaigi.confsched2017.util.ThreadDispatcher
import kotlinx.coroutines.experimental.*
import timber.log.Timber
import javax.inject.Inject

@FragmentScope
class ContributorsViewModel @Inject constructor(
        private val resourceResolver: ResourceResolver,
        private val navigator: Navigator,
        private val toolbarViewModel: ToolbarViewModel,
        private val contributorsRepository: ContributorsRepository,
        private val dispatcher: ThreadDispatcher,
        private val cancellation: Job
) : BaseObservable(), ViewModel {

    val contributorViewModels: ObservableList<ContributorViewModel> = ObservableArrayList()

    @get:Bindable
    var loadingVisibility: Int = 0
        private set(visibility) {
            field = visibility
            notifyPropertyChanged(BR.loadingVisibility)
        }

    @get:Bindable
    var refreshing: Boolean = false
        private set(refreshing) {
            field = refreshing
            notifyPropertyChanged(BR.refreshing)
        }

    private var callback: Callback? = null

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun start(): Job = loadContributors(false)

    override fun destroy() {
        cancellation.cancel()
        this.callback = null
    }

    fun onSwipeRefresh(): Job = loadContributors(true)

    fun retry() {
        loadContributors(false)
    }

    fun onClickRepositoryMenu() {
        navigator.navigateToWebPage("https://github.com/DroidKaigi/conference-app-2017")
    }

    private fun loadContributors(refresh: Boolean): Job = dispatcher.asyncUI {
        if (refresh) {
            contributorsRepository.setDirty(true)
        } else {
            loadingVisibility = View.VISIBLE
        }

        try {
            val threadPool = CommonPool + cancellation
            val contributors = contributorsRepository.findAll(threadPool).await()
            val viewModels = async(threadPool) { contributors.map { ContributorViewModel(navigator, it) } }
            renderContributors(viewModels)
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            loadingVisibility = View.GONE
            callback?.showError(R.string.contributors_load_failed)
            Timber.tag(TAG).e(e, "Failed to show contributors.")
        }
    }

    private suspend fun renderContributors(contributorViewModels: Deferred<List<ContributorViewModel>>) {
        this.contributorViewModels.clear()
        val viewModels = contributorViewModels.await()
        this.contributorViewModels.addAll(viewModels)

        val title = resourceResolver.getString(R.string.contributors) +
                " " + resourceResolver.getString(R.string.contributors_people, viewModels.size)
        toolbarViewModel.toolbarTitle = title

        loadingVisibility = View.GONE
        refreshing = false
    }

    interface Callback {

        fun showError(@StringRes textRes: Int)
    }

    private companion object {

        val TAG: String = ContributorsViewModel::class.java.simpleName
    }
}
