package app.documents.core.di.koin

import app.documents.core.di.dagger.AccountModule
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

//val accountModule = module {
//    single { AccountModule.providesAccountDataBase(context = androidApplication()) }
//    single { AccountModule.providesAccountDao(db = get()) }
//    single { AccountModule.provideAccount(accountDao = get()) }
//}