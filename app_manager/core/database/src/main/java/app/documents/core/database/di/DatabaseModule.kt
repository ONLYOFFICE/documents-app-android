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

@Module
object DatabaseModule {

    @Provides
    fun providesCloudDatabase(context: Context): CloudDatabase {
        return Room
            .databaseBuilder(context, CloudDatabase::class.java, CloudDatabase.NAME)
            .build()
    }

    @Provides
    fun providesRecentDatabase(context: Context): RecentDatabase {
        return Room
            .databaseBuilder(context, RecentDatabase::class.java, RecentDatabase.NAME)
            .build()
    }

    @Provides
    fun provideCloudDataSource(cloudDatabase: CloudDatabase): CloudDataSource {
        return CloudDataSourceImpl(cloudDatabase)
    }

    @Provides
    fun provideRecentDataSource(recentDatabase: RecentDatabase): RecentDataSource {
        return RecentDataSourceImpl(recentDatabase)
    }
}