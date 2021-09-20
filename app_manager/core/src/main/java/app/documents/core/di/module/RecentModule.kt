package app.documents.core.di.module

import app.documents.core.account.AccountsDataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RecentModule {

    @Provides
    @Singleton
    fun providesRecentDao(db: AccountsDataBase) = db.recentDao()

}