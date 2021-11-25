package app.editors.manager.googledrive.googledrive.api

import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.googledrive.mvp.models.request.RenameRequest
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response

interface IGoogleDriveServiceProvider {

    fun getFiles(map: Map<String, String>): Single<GoogleDriveResponse>
    fun delete(fileId: String): Single<Response<ResponseBody>>
    fun getFileInfo(fileId: String, map: Map<String, String> = mapOf("fields" to "id, capabilities/canDelete")): Single<GoogleDriveResponse>
    fun download(fileId: String, map: Map<String, String> = mapOf("alt" to "media")): Single<Response<ResponseBody>>
    fun rename(fileId: String, request: RenameRequest):Single<GoogleDriveResponse>
    fun move(fileId: String, map: Map<String, String?>): Single<GoogleDriveResponse>
    fun copy(fileId: String): Single<GoogleDriveResponse>
    fun create(request: CreateItemRequest): Single<GoogleDriveResponse>
    fun upload(request: CreateItemRequest, map: Map<String, String> = mapOf("uploadType" to "resumable")): Single<Response<ResponseBody>>
}