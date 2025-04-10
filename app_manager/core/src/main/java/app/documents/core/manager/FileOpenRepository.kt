package app.documents.core.manager

import android.net.Uri
import app.documents.core.model.cloud.Recent
import app.documents.core.network.manager.models.explorer.CloudFile
import lib.toolkit.base.managers.utils.EditType

interface FileOpenRepository {

    fun openLocalFile(cloudFile: CloudFile, editType: EditType)

    fun openLocalFile(uri: Uri, extension: String, editType: EditType)

    fun openCloudFile(id: String, editType: EditType)

    fun openRecentFile(recent: Recent, editType: EditType)
}