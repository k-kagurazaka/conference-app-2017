package io.github.droidkaigi.confsched2017.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.view.View
import io.github.droidkaigi.confsched2017.BR
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository
import io.github.droidkaigi.confsched2017.util.AlarmUtil
import io.github.droidkaigi.confsched2017.util.DateUtil
import io.github.droidkaigi.confsched2017.view.activity.MainActivity
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.*

class SessionViewModel(
        private val session: Session? = null,
        context: Context? = null,
        private val navigator: Navigator? = null,
        roomCount: Int = 0,
        isMySession: Boolean = false,
        private val mySessionsRepository: MySessionsRepository? = null
) : BaseObservable(), ViewModel {

    val shortStime: String = session?.stime?.let { DateUtil.getHourMinute(it) } ?: ""

    val formattedDate: String = session?.stime?.let { DateUtil.getMonthDate(it, context) } ?: ""

    val title: String = session?.title ?: ""

    val speakerName: String = session?.speaker?.name ?: ""

    internal val roomName: String = session?.room?.name ?: ""

    val languageId: String = session?.lang?.toUpperCase() ?: ""

    val languageVisibility: Int = if (session?.lang != null) View.VISIBLE else View.GONE

    val minutes: String = session?.durationMin?.let { context?.getString(R.string.session_minutes, it) } ?: ""

    var rowSpan = 1
        private set

    var colSpan = 1
        private set

    var titleMaxLines = 3
        private set

    var speakerNameMaxLines = 1
        private set

    @get:Bindable
    var checkVisibility: Int = if (isMySession) View.VISIBLE else View.GONE
        private set(visibility) {
            field = visibility
            notifyPropertyChanged(BR.checkVisibility)
        }

    var isClickable: Boolean = false
        private set

    var backgroundResId: Int = R.drawable.bg_empty_session
        @DrawableRes private set

    var topicColorResId: Int = android.R.color.transparent
        @ColorRes private set

    var normalSessionItemVisibility: Int = View.GONE
        private set

    internal val stime: Date? = session?.stime

    init {
        session?.let { session ->
            // Break time is over 30 min, but one row is good
            if (session.durationMin > 30 && !session.isBreak) {
                this.rowSpan = this.rowSpan * 2
                this.titleMaxLines = this.titleMaxLines * 2
                this.speakerNameMaxLines = this.speakerNameMaxLines * 3
            }

            this.colSpan = decideColSpan(session, roomCount)

            if (session.isBreak) {
                this.isClickable = false
                this.backgroundResId = R.drawable.bg_empty_session
                this.topicColorResId = android.R.color.transparent
            } else {
                this.isClickable = true
                this.backgroundResId = if (session.isLiveAt(Date())) R.drawable.clickable_purple else R.drawable.clickable_white
                this.topicColorResId = TopicColor.from(session.topic).middleColorResId
            }

            this.normalSessionItemVisibility = if (!session.isBreak && !session.isDinner) View.VISIBLE else View.GONE
        }
    }

    fun showSessionDetail(@Suppress("UNUSED_PARAMETER") view: View) {
        if (navigator != null && session != null) {
            navigator.navigateToSessionDetail(session, MainActivity::class.java)
        }
    }

    fun checkSession(view: View): Boolean {
        if (session == null || mySessionsRepository == null) {
            return false
        }

        launch(CommonPool) {
            try {
                if (mySessionsRepository.exists(session.id)) {
                    mySessionsRepository.delete(context, session).join()
                    checkVisibility = View.GONE
                    AlarmUtil.unregisterAlarm(view.context, session)
                } else {
                    mySessionsRepository.save(context, session).join()
                    checkVisibility = View.VISIBLE
                    AlarmUtil.registerAlarm(view.context, session)
                }
            } catch (e: CancellationException) {
                // Do nothing
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Failed to toggle my session")
            }
        }
        return true
    }

    override fun destroy() {
        // Nothing to do
    }

    private fun decideColSpan(session: Session, roomCount: Int): Int {
        if (session.isCeremony) {
            return 3
        } else if (session.isBreak || session.isDinner) {
            return roomCount
        } else {
            return 1
        }
    }

    companion object {

        private val TAG = SessionViewModel::class.java.simpleName

        @JvmOverloads internal fun createEmpty(rowSpan: Int, colSpan: Int = 1): SessionViewModel =
                SessionViewModel().apply {
                    this.rowSpan = rowSpan
                    this.colSpan = colSpan
                }
    }
}
