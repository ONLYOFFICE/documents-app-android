package app.documents.core.di.koin

import org.koin.dsl.module

val shareModule = module {
//    single(named("token")) { ShareModule.provideToken(context = get(), cloudAccount = get()) }
//    single { ShareModule.provideShareService(okHttpClient = get(), settings = get()) }
//    single { ShareModule.provideShareRepository(shareService = get()) }
//    single {
//        ShareModule.provideOkHttpClient(
//            token = get(qualifier = named("token")),
//            settings = get(),
//            context = androidContext()
//        )
//    }
}