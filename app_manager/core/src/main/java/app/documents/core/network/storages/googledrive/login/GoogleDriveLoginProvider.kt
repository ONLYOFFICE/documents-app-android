package app.documents.core.network.storages.googledrive.login

import app.documents.core.BuildConfig
import app.documents.core.network.common.contracts.StorageContract
import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GoogleDriveLoginProvider(private val googleDriveLoginService: GoogleDriveLoginService) {

    fun getToken(code: String): Single<TokenResponse> {
        val params = mapOf(
            StorageContract.ARG_CODE to code,
            StorageContract.ARG_CLIENT_ID to BuildConfig.GOOGLE_COM_CLIENT_ID,
            StorageContract.ARG_CLIENT_SECRET to BuildConfig.GOOGLE_COM_CLIENT_SECRET,
            StorageContract.ARG_REDIRECT_URI to BuildConfig.GOOGLE_COM_REDIRECT_URL,
            StorageContract.ARG_GRANT_TYPE to StorageContract.Google.VALUE_GRANT_TYPE_AUTH,
        )
        return googleDriveLoginService.getToken(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun refreshToken(refreshToken: String): Single<TokenResponse> {
        val params = mapOf(
            StorageContract.ARG_CLIENT_ID to BuildConfig.GOOGLE_COM_CLIENT_ID,
            StorageContract.ARG_CLIENT_SECRET to BuildConfig.GOOGLE_COM_CLIENT_SECRET,
            StorageContract.ARG_GRANT_TYPE to StorageContract.OneDrive.VALUE_GRANT_TYPE_REFRESH,
            StorageContract.ARG_REFRESH_TOKEN to refreshToken,
        )
        return googleDriveLoginService.getToken(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}