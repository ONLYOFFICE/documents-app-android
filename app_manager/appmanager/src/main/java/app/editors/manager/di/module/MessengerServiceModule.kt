package app.editors.manager.di.module

import app.documents.shared.di.MessengerServiceDependencies
import app.editors.manager.di.component.AppComponent
import dagger.Binds
import dagger.Module

@Module
interface MessengerServiceModule {

    @Binds
    fun bindServiceDependencies(appComponent: AppComponent): MessengerServiceDependencies
}