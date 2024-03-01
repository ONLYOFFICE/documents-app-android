package app.documents.core.network.di

import app.documents.core.network.login.LoginModule
import dagger.Module

@Module(includes = [LoginModule::class])
object NetworkModule