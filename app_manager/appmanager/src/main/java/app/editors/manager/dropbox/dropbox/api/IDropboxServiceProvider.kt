package app.editors.manager.dropbox.dropbox.api

import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import app.editors.manager.dropbox.mvp.models.response.ExplorerResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response

interface IDropboxServiceProvider {
    fun getFiles(request: ExplorerRequest): Single<DropboxResponse>
}