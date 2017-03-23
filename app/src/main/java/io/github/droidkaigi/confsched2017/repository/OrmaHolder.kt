package io.github.droidkaigi.confsched2017.repository

import android.content.Context
import io.github.droidkaigi.confsched2017.model.OrmaDatabase
import javax.inject.Inject
import javax.inject.Singleton

interface OrmaHolder {

    val database: OrmaDatabase
}

@Singleton
class OrmaHolderImpl @Inject constructor(context: Context) : OrmaHolder {

    override val database: OrmaDatabase = OrmaDatabase.builder(context).build()
}
