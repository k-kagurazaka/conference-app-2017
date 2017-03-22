package io.github.droidkaigi.confsched2017.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.view.View
import io.github.droidkaigi.confsched2017.BR
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository
import io.github.droidkaigi.confsched2017.util.asyncUI
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import timber.log.Timber
import javax.inject.Inject

class MySessionsViewModel @Inject internal constructor(
        private val navigator: Navigator,
        private val mySessionsRepository: MySessionsRepository,
        private var cancellation: Job
) : BaseObservable(), ViewModel {

    val mySessionViewModels: ObservableList<MySessionViewModel> = ObservableArrayList()

    @get:Bindable
    var emptyViewVisibility: Int = 0
        private set

    @get:Bindable
    var recyclerViewVisibility: Int = 0
        private set

    override fun destroy() {
        cancellation.cancel()
        cancellation = Job()
    }

    fun start(context: Context) = asyncUI {
        try {
            val viewModels = mySessionsRepository.findAll(CommonPool + cancellation)
                    .await()
                    .asSequence()
                    .sortedBy { it.session.stime }
                    .map { MySessionViewModel(context, navigator, it) }
                    .toList()
            renderMySessions(viewModels)
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to show my sessions.")
        }
    }

    private fun renderMySessions(mySessionViewModels: List<MySessionViewModel>) {
        if (this.mySessionViewModels.size == mySessionViewModels.size) {
            return
        }
        this.mySessionViewModels.clear()
        this.mySessionViewModels.addAll(mySessionViewModels)
        this.emptyViewVisibility = if (this.mySessionViewModels.size > 0) View.GONE else View.VISIBLE
        this.recyclerViewVisibility = if (this.mySessionViewModels.size > 0) View.VISIBLE else View.GONE
        notifyPropertyChanged(BR.emptyViewVisibility)
        notifyPropertyChanged(BR.recyclerViewVisibility)
    }

    private companion object {

        val TAG: String = MySessionsViewModel::class.java.simpleName
    }
}
