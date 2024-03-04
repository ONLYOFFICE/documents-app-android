package app.editors.manager.di.module

import app.documents.core.di.dagger.AccountModule
import app.documents.core.di.dagger.SettingsModule
import dagger.Module

@Module(includes = [ToolModule::class, SettingsModule::class, AccountModule::class])
class AppModule