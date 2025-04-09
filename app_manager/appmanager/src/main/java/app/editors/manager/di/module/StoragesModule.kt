package app.editors.manager.di.module

import android.content.Context
import app.documents.core.providers.DropboxFileProvider
import app.documents.core.providers.GoogleDriveFileProvider
import app.documents.core.providers.OneDriveFileProvider
import app.editors.manager.managers.providers.DropboxStorageHelper
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.providers.OneDriveStorageHelper
import dagger.Module
import dagger.Provides

@Module
object StoragesModule {

    @Provides
    fun provideDropboxFileProvider(context: Context): DropboxFileProvider {
        return DropboxFileProvider(
            context = context,
            helper = DropboxStorageHelper()
        )
    }

    @Provides
    fun provideGoogleDriveFileProvider(context: Context): GoogleDriveFileProvider {
        return GoogleDriveFileProvider(
            context = context,
            helper = GoogleDriveStorageHelper()
        )
    }

    @Provides
    fun provideOneDriveFileProvider(context: Context): OneDriveFileProvider {
        return OneDriveFileProvider(
            context = context,
            helper = OneDriveStorageHelper()
        )
    }
}