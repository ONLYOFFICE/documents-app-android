package app.documents.shared.di

interface MessengerServiceApp {

    fun createMessengerServiceComponent(): MessengerServiceComponent

    fun destroyMessengerServiceComponent()
}