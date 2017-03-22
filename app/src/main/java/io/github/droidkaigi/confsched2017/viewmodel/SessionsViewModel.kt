package io.github.droidkaigi.confsched2017.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import io.github.droidkaigi.confsched2017.BR
import io.github.droidkaigi.confsched2017.model.Room
import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository
import io.github.droidkaigi.confsched2017.util.DateUtil
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SessionsViewModel @Inject constructor(
        private val navigator: Navigator,
        private val sessionsRepository: SessionsRepository,
        private val mySessionsRepository: MySessionsRepository
) : BaseObservable(), ViewModel {

    var rooms: List<Room>? = null
        private set

    var stimes: List<Date>? = null
        private set

    val loadingVisibility: Int
        @Bindable get() = rooms?.let { View.GONE } ?: View.VISIBLE

    override fun destroy() {
        // Do nothing
    }

    fun getSessions(coroutineContext: CoroutineContext, locale: Locale, context: Context): Deferred<List<SessionViewModel>> =
            async(coroutineContext) {
                val sessionsDeferred = sessionsRepository.findAll(this.context, locale)
                val mySessionsDeferred = mySessionsRepository.findAll(this.context)

                val sessions = sessionsDeferred.await()
                val rooms = extractRooms(sessions)
                this@SessionsViewModel.rooms = rooms
                this@SessionsViewModel.stimes = extractStimes(sessions)

                val mySessionMap = mySessionsDeferred.await().associateBy { it.session.id }
                notifyPropertyChanged(BR.loadingVisibility)

                val viewModels = sessions.map {
                    val isMySession = mySessionMap.contains(it.id)
                    SessionViewModel(it, context, navigator, rooms.size, isMySession, mySessionsRepository)
                }
                return@async adjustViewModels(viewModels, context)
            }

    private fun adjustViewModels(sessionViewModels: List<SessionViewModel>, context: Context): List<SessionViewModel> {
        // Prepare sessions map
        val sessionMap = hashMapOf<String, SessionViewModel>()
        for (viewModel in sessionViewModels) {
            // In the case of Welcome talk and lunch time, set dummy room
            val roomName = viewModel.roomName.takeIf(String::isNotEmpty) ?: rooms!![0].name
            sessionMap.put(generateStimeRoomKey(viewModel.stime!!, roomName), viewModel)
        }

        val adjustedViewModels = arrayListOf<SessionViewModel>()

        // Format date that user can see. Ex) 9, March
        var lastFormattedDate: String? = null
        for (stime in stimes!!) {
            if (lastFormattedDate == null) {
                lastFormattedDate = DateUtil.getMonthDate(stime, context)
            }

            val sameTimeViewModels = arrayListOf<SessionViewModel>()
            var maxRowSpan = 1
            var i = 0
            val size = rooms!!.size
            while (i < size) {
                val room = rooms!![i]
                val viewModel = sessionMap[generateStimeRoomKey(stime, room.name)]
                if (viewModel != null) {
                    if (lastFormattedDate != viewModel.formattedDate) {
                        // Change the date
                        lastFormattedDate = viewModel.formattedDate
                        // Add empty row which divides the days
                        adjustedViewModels.add(SessionViewModel.createEmpty(1, rooms!!.size))
                    }
                    sameTimeViewModels.add(viewModel)

                    if (viewModel.rowSpan > maxRowSpan) {
                        maxRowSpan = viewModel.rowSpan
                    }

                    var j = 1
                    val colSize = viewModel.colSpan
                    while (j < colSize) {
                        // If the col size is over 1, skip next loop.
                        i++
                        j++
                    }
                } else {
                    val empty = SessionViewModel.createEmpty(1)
                    sameTimeViewModels.add(empty)
                }
                i++
            }

            val copiedTmpViewModels = ArrayList(sameTimeViewModels)
            for (tmpViewModel in sameTimeViewModels) {
                val rowSpan = tmpViewModel.rowSpan
                if (rowSpan < maxRowSpan) {
                    // Fill for empty cell
                    copiedTmpViewModels.add(SessionViewModel.createEmpty(maxRowSpan - rowSpan))
                }
            }

            adjustedViewModels.addAll(copiedTmpViewModels)
        }

        return adjustedViewModels
    }

    private fun generateStimeRoomKey(stime: Date, roomName: String): String =
            "${DateUtil.getLongFormatDate(stime)}_$roomName"

    private fun extractStimes(sessions: List<Session>): List<Date> =
            sessions.asSequence()
                    .map { it.stime }
                    .sorted()
                    .distinct()
                    .toList()

    private fun extractRooms(sessions: List<Session>): List<Room> =
            sessions.asSequence()
                    .map { it.room }
                    .filterNotNull()
                    .filter { it.id != 0 }
                    .sortedBy(Room::name)
                    .distinct()
                    .toList()

}
