package app.editors.manager.managers.providers

import io.reactivex.Single


sealed class OneDriveResponse {
    class Success(val response: Any) : OneDriveResponse()
    class Error(val error: Throwable) : OneDriveResponse()
}

interface IOneDriveServiceProvider {

    fun authorization(parameters: Map<String, String>): Single<OneDriveResponse>
    fun userInfo(): Single<OneDriveResponse>
    fun getFiles(): Single<OneDriveResponse>
    fun getRoot(): Single<OneDriveResponse>
}