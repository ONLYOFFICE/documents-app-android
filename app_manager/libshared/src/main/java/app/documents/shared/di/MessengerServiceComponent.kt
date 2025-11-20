package app.documents.shared.di

import app.documents.shared.MessengerService
import dagger.Subcomponent

@MessengerServiceScope
@Subcomponent
interface MessengerServiceComponent {

    fun inject(service: MessengerService)

    @Subcomponent.Factory
    interface Factory {
        fun create(): MessengerServiceComponent
    }
}