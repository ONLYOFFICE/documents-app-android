package app.editors.manager.di.module

import app.documents.core.di.module.AccountModule
import app.documents.core.di.module.LoginModule
import app.documents.core.di.module.RecentModule
import app.documents.core.di.module.SettingsModule
import dagger.Module

@Module(includes = [ToolModule::class, SettingsModule::class, AccountModule::class, RecentModule::class, LoginModule::class])
class AppModule