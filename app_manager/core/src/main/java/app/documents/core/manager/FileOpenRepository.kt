package app.documents.core.manager

import android.net.Uri
import app.documents.core.model.cloud.Recent
import app.documents.core.network.manager.models.explorer.CloudFile
import kotlinx.coroutines.flow.Flow
import lib.toolkit.base.managers.utils.EditType

interface FileOpenRepository {

    val resultFlow: Flow<FileOpenResult>

    fun openLocalFile(cloudFile: CloudFile, editType: EditType)

    fun openLocalFile(uri: Uri, extension: String, editType: EditType)

    fun openCloudFile(cloudFile: CloudFile, editType: EditType)

    fun openCloudFile(id: String, editType: EditType)

    fun openCloudFile(id: String, portal: String, token: String, editType: EditType)

    fun openRecentFile(recent: Recent, editType: EditType)
}