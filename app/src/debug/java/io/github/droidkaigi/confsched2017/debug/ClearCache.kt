package io.github.droidkaigi.confsched2017.debug

import android.app.Activity
import android.widget.Toast
import com.tomoima.debot.strategy.DebotStrategy
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository
import io.github.droidkaigi.confsched2017.util.asyncUI
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class ClearCache @Inject constructor(private val sessionsRepository: SessionsRepository) : DebotStrategy() {

    override fun startAction(activity: Activity) = asyncUI {
        launch(CommonPool) {
            sessionsRepository.deleteAll()
        }.join()

        Toast.makeText(activity.applicationContext, "Cache Cleared", Toast.LENGTH_LONG).show()
    }
}
