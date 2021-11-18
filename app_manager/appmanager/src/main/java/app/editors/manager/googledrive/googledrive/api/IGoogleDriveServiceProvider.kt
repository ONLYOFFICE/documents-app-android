package app.editors.manager.googledrive.googledrive.api

import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.googledrive.mvp.models.request.RenameRequest
import io.reactivex.Single
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response

interface IGoogleDriveServiceProvider {

    fun getFiles(map: Map<String, String>): Single<GoogleDriveResponse>
    fun delete(fileId: String): Single<Response<ResponseBody>>
    fun getFileInfo(fileId: String, map: Map<String, String> = mapOf("fields" to "id, capabilities/canDelete")): Single<GoogleDriveResponse>
    fun rename(fileId: String, request: RenameRequest):Single<GoogleDriveResponse>
}