package app.documents.shared.di

import app.documents.core.providers.RoomProvider

interface MessengerServiceDependencies {

    fun roomProvider(): RoomProvider
}