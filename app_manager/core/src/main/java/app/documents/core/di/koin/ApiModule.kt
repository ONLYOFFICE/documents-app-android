package app.documents.core.di.koin

import app.documents.core.storage.preference.NetworkSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val apiModule = module {
    single { NetworkSettings(context = androidContext()) }
}