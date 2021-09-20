package app.documents.core.di.module

import android.content.Context
import app.documents.core.settings.NetworkSettings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class SettingsModule {

    @Provides
    @Singleton
    fun providesNetworkSettings(context: Context) = NetworkSettings(context)

}