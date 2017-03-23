package io.github.droidkaigi.confsched2017.viewmodel

import android.content.Context
import android.databinding.BaseObservable
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.view.View
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository
import io.github.droidkaigi.confsched2017.util.AlarmUtil
import io.github.droidkaigi.confsched2017.util.DateUtil
import io.github.droidkaigi.confsched2017.util.LocaleUtil
import io.github.droidkaigi.confsched2017.util.ThreadDispatcher
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.experimental.CoroutineContext

class SessionDetailViewModel @Inject constructor(
        private val context: Context,
        private val navigator: Navigator,
        private val sessionsRepository: SessionsRepository,
        private val mySessionsRepository: MySessionsRepository,
        private val dispatcher: ThreadDispatcher
) : BaseObservable(), ViewModel {

    var sessionTitle: String? = null
        private set

    var speakerImageUrl: String? = null
        private set

    var sessionVividColorResId = R.color.white
        @ColorRes private set

    var sessionPaleColorResId = R.color.white
        @ColorRes private set

    var topicThemeResId = R.color.white
        @StyleRes private set

    var languageResId: Int = R.string.lang_en
        @StringRes private set

    var sessionTimeRange: String? = null
        private set

    var session: Session? = null

    var isMySession: Boolean = false
        private set

    var tagContainerVisibility: Int = 0
        private set

    var speakerVisibility: Int = 0
        private set

    var slideIconVisibility: Int = 0
        private set

    var dashVideoIconVisibility: Int = 0
        private set

    var roomVisibility: Int = 0
        private set

    var topicVisibility: Int = 0
        private set

    var feedbackButtonVisiblity: Int = 0
        private set

    private var callback: Callback? = null

    fun loadSession(coroutineContext: CoroutineContext, sessionId: Int): Job = launch(coroutineContext) {
        sessionsRepository.find(context, sessionId, Locale.getDefault()).await()
                ?.let { applySession(it) }
    }

    override fun destroy() {
        this.callback = null
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun shouldShowShareMenuItem(): Boolean = !session?.shareUrl.isNullOrEmpty()

    fun onClickShareMenuItem() {
        // TODO
    }

    fun onClickFeedbackButton(@Suppress("UNUSED_PARAMETER") view: View) {
        session?.let { navigator.navigateToFeedbackPage(it) }
    }

    fun onClickSlideIcon(@Suppress("UNUSED_PARAMETER") view: View) {
        // TODO
    }

    fun onClickMovieIcon(@Suppress("UNUSED_PARAMETER") view: View) {
        // TODO
    }

    fun onClickFab(@Suppress("UNUSED_PARAMETER") view: View) = dispatcher.launchUI {
        session?.let {
            val selected = if (mySessionsRepository.exists(it.id)) {
                try {
                    mySessionsRepository.delete(CommonPool, it).join()
                    Timber.tag(TAG).d("Deleted my session")
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Failed to delete my session")
                }
                AlarmUtil.unregisterAlarm(this@SessionDetailViewModel.context, it)
                false
            } else {
                try {
                    mySessionsRepository.save(CommonPool, it).join()
                    Timber.tag(TAG).d("Saved my session")
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Failed to save my session")
                }
                AlarmUtil.registerAlarm(this@SessionDetailViewModel.context, it)
                true
            }

            callback?.onClickFab(selected)
        }
    }

    fun onOverScroll() {
        callback?.onOverScroll()
    }

    private fun applySession(session: Session) {
        this.session = session
        this.sessionTitle = session.title
        this.speakerImageUrl = session.speaker?.adjustedImageUrl
        val topicColor = TopicColor.from(session.topic)
        this.sessionVividColorResId = topicColor.vividColorResId
        this.sessionPaleColorResId = topicColor.paleColorResId
        this.topicThemeResId = topicColor.themeId
        this.sessionTimeRange = decideSessionTimeRange(context, session)
        this.isMySession = mySessionsRepository.exists(session.id)
        this.tagContainerVisibility = if (!session.isDinner) View.VISIBLE else View.GONE
        this.speakerVisibility = if (!session.isDinner) View.VISIBLE else View.GONE
        this.slideIconVisibility = if (session.slideUrl != null) View.VISIBLE else View.GONE
        this.dashVideoIconVisibility = if (session.movieUrl != null && session.movieDashUrl != null) View.VISIBLE else View.GONE
        this.roomVisibility = if (session.room != null) View.VISIBLE else View.GONE
        this.topicVisibility = if (session.topic != null) View.VISIBLE else View.GONE
        this.feedbackButtonVisiblity = if (!session.isDinner) View.VISIBLE else View.GONE
        this.languageResId = session.lang?.let { decideLanguageResId(Locale(it.toLowerCase())) } ?: R.string.lang_en
    }

    private fun decideLanguageResId(locale: Locale): Int = when (locale) {
        Locale.JAPANESE -> R.string.lang_ja
        else -> R.string.lang_en
    }

    private fun decideSessionTimeRange(context: Context, session: Session): String {
        val displaySTime = LocaleUtil.getDisplayDate(session.stime, context)
        val displayETime = LocaleUtil.getDisplayDate(session.etime, context)

        return context.getString(R.string.session_time_range,
                DateUtil.getLongFormatDate(displaySTime),
                DateUtil.getHourMinute(displayETime),
                DateUtil.getMinutes(displaySTime, displayETime))
    }

    interface Callback {

        fun onClickFab(selected: Boolean)

        fun onOverScroll()
    }

    companion object {

        private val TAG = SessionDetailViewModel::class.java.simpleName
    }
}
