package io.github.droidkaigi.confsched2017.repository

import android.content.Context
import io.github.droidkaigi.confsched2017.model.OrmaDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrmaHolder @Inject constructor(context: Context) {

    val database: OrmaDatabase = OrmaDatabase.builder(context).build()
}
