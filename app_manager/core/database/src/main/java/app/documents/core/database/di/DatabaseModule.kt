package app.documents.core.database.di

import android.content.Context
import androidx.room.Room
import app.documents.core.database.database.CloudDatabase
import app.documents.core.database.database.RecentDatabase
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.datasource.CloudDataSourceImpl
import app.documents.core.database.datasource.RecentDataSource
import app.documents.core.database.datasource.RecentDataSourceImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesCloudDatabase(context: Context): CloudDatabase {
        return Room
            .databaseBuilder(context, CloudDatabase::class.java, CloudDatabase.NAME)
            .build()
    }

    @Provides
    @Singleton
    fun providesRecentDatabase(context: Context): RecentDatabase {
        return Room
            .databaseBuilder(context, RecentDatabase::class.java, RecentDatabase.NAME)
            .build()
    }

    @Provides
    @Singleton
    fun provideCloudDataSource(cloudDatabase: CloudDatabase): CloudDataSource {
        return CloudDataSourceImpl(cloudDatabase)
    }

    @Provides
    @Singleton
    fun provideRecentDataSource(recentDatabase: RecentDatabase): RecentDataSource {
        return RecentDataSourceImpl(recentDatabase)
    }
}