package io.github.droidkaigi.confsched2017.repository

import io.github.droidkaigi.confsched2017.model.OrmaDatabase
import org.robolectric.RuntimeEnvironment
import javax.inject.Singleton

@Singleton
class OrmaHolder {

    val database: OrmaDatabase = OrmaDatabase.builder(RuntimeEnvironment.application).name(null).build()
}
