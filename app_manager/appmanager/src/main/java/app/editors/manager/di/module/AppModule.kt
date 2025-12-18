package app.editors.manager.di.module

import android.content.Context
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.providers.DropboxFileProvider
import app.documents.core.providers.GoogleDriveFileProvider
import app.documents.core.providers.OneDriveFileProvider
import app.documents.core.utils.FirebaseTool
import app.editors.manager.managers.providers.DropboxStorageHelper
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.providers.OneDriveStorageHelper
import app.editors.manager.managers.tools.FilterManager
import app.editors.manager.managers.utils.FirebaseUtils
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Singleton
import kotlin.coroutines.resume

@Module
object AppModule {

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

    @Provides
    @Singleton
    fun provideFirebaseTool(
        context: Context,
        cloudAccount: CloudAccount?
    ): FirebaseTool {
        return object : FirebaseTool {

            private var _isCoauthoring: Boolean? = null

            override suspend fun isCoauthoring(): Boolean {
                _isCoauthoring?.let { enabled ->
                    return enabled
                }
                val isCoauthoring = suspendCancellableCoroutine { continuation ->
                    if (cloudAccount == null) {
                        continuation.resume(false)
                        return@suspendCancellableCoroutine
                    }
                    FirebaseUtils.checkSdkVersion(
                        context = context,
                        account = cloudAccount,
                        onResult = { enabled ->
                            _isCoauthoring = enabled
                            continuation.resume(enabled)
                        }
                    )
                }
                return isCoauthoring
            }
        }
    }

    @Provides
    @Singleton
    fun provideFilterManager(): FilterManager = FilterManager()
}