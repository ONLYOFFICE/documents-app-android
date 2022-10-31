package app.editors.manager.storages.googledrive.googledrive.login

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GoogleDriveLoginServiceProvider (
    private val googleDriveLoginService: GoogleDriveLoginService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
): IGoogleDriveLoginServiceProvider {

    override fun getToken(map: Map<String, String>): Single<TokenResponse> {
        return googleDriveLoginService.getToken(map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}