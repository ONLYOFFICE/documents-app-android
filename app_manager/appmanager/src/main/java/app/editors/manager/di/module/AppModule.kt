package app.editors.manager.di.module

import app.documents.core.di.dagger.AccountModule
import dagger.Module

@Module(includes = [ToolModule::class, AccountModule::class])
class AppModule