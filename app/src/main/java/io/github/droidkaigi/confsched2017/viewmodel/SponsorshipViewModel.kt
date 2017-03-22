package io.github.droidkaigi.confsched2017.viewmodel

import android.databinding.BaseObservable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import io.github.droidkaigi.confsched2017.model.Sponsorship
import io.github.droidkaigi.confsched2017.view.helper.Navigator

class SponsorshipViewModel(
        private val navigator: Navigator,
        val sponsorship: Sponsorship
) : BaseObservable(), ViewModel {

    val category: String = sponsorship.category

    val sponsorViewModels: ObservableList<SponsorViewModel> = ObservableArrayList<SponsorViewModel>().apply {
        val viewModels = sponsorship.sponsors.map { SponsorViewModel(navigator, it) }
        addAll(viewModels)
    }

    override fun destroy() {
        // No-op
    }
}
