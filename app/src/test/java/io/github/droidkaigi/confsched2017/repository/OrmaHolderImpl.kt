package io.github.droidkaigi.confsched2017.repository

import io.github.droidkaigi.confsched2017.model.OrmaDatabase
import org.robolectric.RuntimeEnvironment

class TestOrmaHolder : OrmaHolder {

    override val database: OrmaDatabase = OrmaDatabase.builder(RuntimeEnvironment.application).name(null).build()
}
