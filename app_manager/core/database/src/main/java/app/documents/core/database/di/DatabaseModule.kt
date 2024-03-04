package app.documents.core.database.di

import android.content.Context
import androidx.room.Room
import app.documents.core.database.dao.RecentDao
import app.documents.core.database.database.CloudDatabase
import app.documents.core.database.database.RecentDatabase
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.datasource.CloudDataSourceImpl
import dagger.Module
import dagger.Provides

@Module
object DatabaseModule {

    @Provides
    fun providesCloudDatabase(context: Context): CloudDatabase {
        return Room
            .databaseBuilder(context, CloudDatabase::class.java, CloudDatabase.TAG)
            .build()
    }
//
//    @Provides
//    fun providesAccountDao(cloudDatabase: CloudDatabase): AccountDao {
//        return cloudDatabase.accountDao
//    }

    @Provides
    fun provideCloudDataSource(cloudDatabase: CloudDatabase): CloudDataSource {
        return CloudDataSourceImpl(cloudDatabase)
    }

    @Provides
    fun providesRecentDao(context: Context): RecentDao = Room // todo change to datasource
        .databaseBuilder(context, RecentDatabase::class.java, RecentDatabase.TAG)
        .build()
        .recentDao

}