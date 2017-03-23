package io.github.droidkaigi.confsched2017.view.activity

import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.view.View
import io.github.droidkaigi.confsched2017.R
import io.github.droidkaigi.confsched2017.databinding.ActivitySplashBinding
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository
import io.github.droidkaigi.confsched2017.util.FpsMeasureUtil
import io.github.droidkaigi.confsched2017.util.ThreadDispatcher
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SplashActivity : BaseActivity() {

    @Inject
    internal lateinit var dispatcher: ThreadDispatcher

    @Inject
    internal lateinit var cancellation: Job

    @Inject
    internal lateinit var sessionsRepository: SessionsRepository

    @Inject
    internal lateinit var mySessionsRepository: MySessionsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        DataBindingUtil.setContentView<ActivitySplashBinding>(this, R.layout.activity_splash)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(android.R.id.content).systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onStart() {
        super.onStart()
        loadSessionsForCache()

        // Starting new Activity normally will not destroy this Activity, so set this up in start/stop cycle
        FpsMeasureUtil.play(application)
    }

    override fun onStop() {
        super.onStop()
        cancellation.cancel()

        // Stop tracking the frame rate.
        FpsMeasureUtil.finish()
    }

    private fun loadSessionsForCache(): Job = dispatcher.asyncUI {
        val threadPool = CommonPool + cancellation

        try {
            // Start to pre-cache sessions
            val sessions = sessionsRepository.findAll(threadPool, Locale.getDefault())
            val mySessions = mySessionsRepository.findAll(threadPool)

            // Wait for 1.5s
            delay(MINIMUM_LOADING_TIME)

            // Wait for completing pre-cache
            sessions.await()
            mySessions.await()

            Timber.tag(TAG).d("Succeeded in loading sessions.")
        } catch (e: CancellationException) {
            Timber.tag(TAG).d(e, "Cancelled to load sessions.")
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Failed to load sessions.")
        }

        if (isFinishing) return@asyncUI
        startActivity(MainActivity.createIntent(this@SplashActivity))
        this@SplashActivity.finish()
    }

    companion object {

        private val TAG = SplashActivity::class.java.simpleName

        private val MINIMUM_LOADING_TIME = 1500L
    }

}
