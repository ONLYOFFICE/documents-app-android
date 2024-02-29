package app.editors.manager.mvp.presenters.storages

import app.documents.core.di.dagger.CoreModule
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.documents.core.network.storages.googledrive.api.GoogleDriveResponse
import app.documents.core.network.storages.googledrive.api.GoogleDriveService
import app.documents.core.network.storages.googledrive.login.GoogleDriveLoginProvider
import app.documents.core.network.storages.googledrive.login.GoogleDriveLoginService
import app.documents.core.network.storages.googledrive.models.User
import app.documents.core.network.storages.googledrive.models.resonse.UserResponse
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import app.documents.core.network.common.NetworkClient
import lib.toolkit.base.managers.utils.AccountData
import okhttp3.MediaType
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

class GoogleDriveSignInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getUserInfo(code: String) {
        var token = TokenResponse("", "")
        val client = NetworkClient.getOkHttpBuilder(isSslOn = true, isCiphers = false).apply {
            protocols(listOf(Protocol.HTTP_1_1))
            readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        }.build()

        disposable = GoogleDriveLoginProvider(
            Retrofit.Builder()
                .client(client)
                .baseUrl(GoogleDriveUtils.GOOGLE_DRIVE_AUTH_URL)
                .addConverterFactory(CoreModule.json.asConverterFactory(MediaType.get(ApiContract.VALUE_CONTENT_TYPE)))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(GoogleDriveLoginService::class.java)
        )
            .getToken(code = code).flatMap { tokenResponse ->
                token = tokenResponse

                GoogleDriveProvider(
                    Retrofit.Builder()
                        .client(client)
                        .baseUrl(GoogleDriveUtils.GOOGLE_DRIVE_BASE_URL)
                        .addConverterFactory(CoreModule.json.asConverterFactory(MediaType.get(ApiContract.VALUE_CONTENT_TYPE)))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build()
                        .create(GoogleDriveService::class.java))
                    .getUserInfo("Bearer ${token.accessToken}")
            }.subscribe { loginResponse ->
                when (loginResponse) {
                    is GoogleDriveResponse.Success -> {
                        createUser(
                            (loginResponse.response as UserResponse).user!!,
                            token.accessToken,
                            token.refreshToken
                        )
                    }

                    is GoogleDriveResponse.Error -> {
                        throw loginResponse.error
                    }
                }
            }
    }

    private fun createUser(user: User, accessToken: String, refreshToken: String) {
        val cloudAccount = CloudAccount(
            id = user.permissionId,
            isWebDav = false,
            isGoogleDrive = true,
            portal = "drive.google.com",
            webDavPath = "",
            avatarUrl = user.photoLink,
            webDavProvider = "",
            login = user.emailAddress,
            scheme = "https://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = user.displayName
        )

        val accountData = AccountData(
            portal = cloudAccount.portal ?: "",
            scheme = cloudAccount.scheme ?: "",
            displayName = user.displayName,
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider ?: "",
            accessToken = accessToken,
            refreshToken = refreshToken,
            webDav = cloudAccount.webDavPath,
            email = user.emailAddress,
        )

        saveAccount(cloudAccount, accountData, accessToken)
    }

}