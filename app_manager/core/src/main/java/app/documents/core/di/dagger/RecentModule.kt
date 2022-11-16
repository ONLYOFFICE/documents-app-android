package app.documents.core.di.dagger

import app.documents.core.storage.account.AccountsDataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RecentModule {

    @Provides
    @Singleton
    fun providesRecentDao(db: AccountsDataBase) = db.recentDao()

}