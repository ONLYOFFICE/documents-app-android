package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.storage.preference.NetworkSettings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class SettingsModule {

    @Provides
    @Singleton
    fun providesNetworkSettings(context: Context) = NetworkSettings(context)

}