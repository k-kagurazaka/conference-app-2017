package io.github.droidkaigi.confsched2017.pref

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPrefsHolder @Inject constructor(private val context: Context) {
    val prefs: DefaultPrefs
        get() = DefaultPrefs.get(context)
}
