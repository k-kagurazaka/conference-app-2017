package io.github.droidkaigi.confsched2017.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import com.android.databinding.library.baseAdapters.BR
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.model.Session
import io.github.droidkaigi.confsched2017.model.SessionFeedback
import io.github.droidkaigi.confsched2017.repository.feedbacks.SessionFeedbackRepository
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository
import io.github.droidkaigi.confsched2017.util.asyncUI
import io.github.droidkaigi.confsched2017.view.helper.Navigator
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class SessionFeedbackViewModel @Inject internal constructor(
        private val sessionsRepository: SessionsRepository,
        private val sessionFeedbackRepository: SessionFeedbackRepository,
        private val navigator: Navigator,
        private val cancellation: Job
) : BaseObservable(), ViewModel {

    var session: Session? = null

    @get:Bindable
    var sessionTitle: String? = null
        set(sessionTitle) {
            field = sessionTitle
            notifyPropertyChanged(BR.sessionTitle)
        }

    @get:Bindable
    var relevancy: Int = 0
        set(relevancy) {
            field = relevancy
            if (sessionFeedback.relevancy != relevancy) {
                sessionFeedback.relevancy = relevancy
            }
            notifyPropertyChanged(BR.relevancy)
        }

    @get:Bindable
    var asExpected: Int = 0
        set(asExpected) {
            field = asExpected
            if (sessionFeedback.asExpected != asExpected) {
                sessionFeedback.asExpected = asExpected
            }
            notifyPropertyChanged(BR.asExpected)
        }

    @get:Bindable
    var difficulty: Int = 0
        set(difficulty) {
            field = difficulty
            if (sessionFeedback.difficulty != difficulty) {
                sessionFeedback.difficulty = difficulty
            }
            notifyPropertyChanged(BR.difficulty)
        }

    @get:Bindable
    var knowledgeable: Int = 0
        set(knowledgeable) {
            field = knowledgeable
            if (sessionFeedback.knowledgeable != knowledgeable) {
                sessionFeedback.knowledgeable = knowledgeable
            }
            notifyPropertyChanged(BR.knowledgeable)
        }

    @get:Bindable
    var comment: String? = null
        set(comment) {
            field = comment
            if (comment != null && comment != sessionFeedback.comment) {
                sessionFeedback.comment = comment
            }
            notifyPropertyChanged(BR.comment)
        }

    @get:Bindable
    var loadingVisibility = View.GONE
        set(loadingVisibility) {
            field = loadingVisibility
            notifyPropertyChanged(BR.loadingVisibility)
        }

    @get:Bindable
    var isSubmitButtonEnabled = true
        set(submitButtonEnabled) {
            field = submitButtonEnabled
            notifyPropertyChanged(BR.submitButtonEnabled)
        }

    private var callback: Callback? = null

    private lateinit var sessionFeedback: SessionFeedback

    fun findSession(sessionId: Int) = launch(UI) {
        try {
            val session = sessionsRepository.find(CommonPool + cancellation, sessionId, Locale.getDefault()).await()
            session?.let { initSessionFeedback(it) } ?: throw RuntimeException()
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to find session.")
        }
    }

    private suspend fun initSessionFeedback(session: Session) {
        this.session = session
        sessionTitle = session.title

        sessionFeedback = sessionFeedbackRepository.findFromCache(CommonPool + cancellation, session.id).await() ?:
                SessionFeedback.create(
                        session, this.relevancy, this.asExpected, this.difficulty, this.knowledgeable, this.comment)

        relevancy = sessionFeedback.relevancy
        asExpected = sessionFeedback.asExpected
        difficulty = sessionFeedback.difficulty
        knowledgeable = sessionFeedback.knowledgeable
        comment = sessionFeedback.comment

        isSubmitButtonEnabled = !sessionFeedback.isSubmitted

        callback?.onSessionFeedbackInitialized(sessionFeedback)
    }

    override fun destroy() {
        async(CommonPool) { sessionFeedbackRepository.saveToCache(context, sessionFeedback) }
        cancellation.cancel()
        callback = null
    }

    fun onClickSubmitFeedbackButton(@Suppress("UNUSED_PARAMETER") view: View) {
        if (sessionFeedback.isAllFilled) {
            navigator.showConfirmDialog(R.string.session_feedback_confirm_title,
                    R.string.session_feedback_confirm_message,
                    object : Navigator.ConfirmDialogListener {
                        override fun onClickPositiveButton() {
                            submit(sessionFeedback)
                        }

                        override fun onClickNegativeButton() {
                            // Do nothing
                        }
                    })
        } else {
            callback?.onErrorUnFilled()
        }
    }

    private fun submit(sessionFeedback: SessionFeedback) = asyncUI {
        loadingVisibility = View.VISIBLE
        isSubmitButtonEnabled = false

        try {
            sessionFeedbackRepository.submit(CommonPool + cancellation, sessionFeedback).join()
            sessionFeedback.isSubmitted = true
            callback?.onSuccessSubmit()
        } catch (e: CancellationException) {
            // Do nothing
        } catch (e: Throwable) {
            isSubmitButtonEnabled = true
            callback?.onErrorSubmit()
        } finally {
            loadingVisibility = View.GONE
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    interface Callback {

        fun onSuccessSubmit()

        fun onErrorSubmit()

        fun onErrorUnFilled()

        fun onSessionFeedbackInitialized(sessionFeedback: SessionFeedback)
    }

    private companion object {

        val TAG: String = SessionFeedbackViewModel::class.java.simpleName
    }
}
