package io.github.droidkaigi.confsched2017.viewmodel

import android.databinding.BaseObservable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.model.Sponsorship
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import io.github.droidkaigi.confsched2017.view.helper.ResourceResolver
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import timber.log.Timber
import javax.inject.Inject

class SponsorshipsViewModel @Inject constructor(
        private val resourceResolver: ResourceResolver,
        private val navigator: Navigator,
        private var cancellation: Job
) : BaseObservable(), ViewModel {

    val sponsorShipViewModels: ObservableList<SponsorshipViewModel> = ObservableArrayList()

    fun start(): Job = launch(UI) {
        try {
            val viewModels = async(CommonPool + cancellation) {
                val json = resourceResolver.loadJSONFromAsset(resourceResolver.getString(R.string.sponsors_file))
                val sponsorships = transformSponsorships(json)
                return@async convertToViewModel(sponsorships)
            }.await()

            renderSponsorships(viewModels)
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to show sponsors.")
        }
    }

    override fun destroy() {
        cancellation.cancel()
        cancellation = Job()
    }

    /**
     * Transforms from a valid json string to a List of [Sponsorship].

     * @param json A json representing a list of sponsors.
     * *
     * @return List of [Sponsorship].
     */
    private fun transformSponsorships(json: String): List<Sponsorship> {
        val gson = Gson()
        val listType = object : TypeToken<List<Sponsorship>>() {}.type
        return gson.fromJson<List<Sponsorship>>(json, listType)
    }

    private fun convertToViewModel(sponsorships: List<Sponsorship>): List<SponsorshipViewModel> =
            sponsorships.map { SponsorshipViewModel(navigator, it) }

    private fun renderSponsorships(sponsorships: List<SponsorshipViewModel>) {
        sponsorShipViewModels.clear()
        sponsorShipViewModels.addAll(sponsorships)
    }

    private companion object {

        val TAG: String = SponsorshipsViewModel::class.java.simpleName
    }
}
