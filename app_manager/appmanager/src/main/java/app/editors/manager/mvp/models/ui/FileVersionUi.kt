package app.editors.manager.mvp.models.ui

import app.documents.core.network.manager.models.explorer.CloudFile
import java.util.Date

data class FileVersionUi(
    val fileId: String,
    val title: String,
    val version: Int,
    val versionGroup: String,
    val date: Date,
    val initiatorDisplayName: String,
    val comment: String,
    val fileExst: String,
    val viewUrl: String,
    val isCurrentVersion: Boolean,
    val editAccess: Boolean,
    val file: CloudFile
)

fun CloudFile.toFileVersionUi() = FileVersionUi(
    fileId = id,
    title = "${title.removeSuffix(fileExst)} (v.$version)$fileExst",
    version = version,
    versionGroup = versionGroup,
    date = updated,
    initiatorDisplayName = updatedBy.displayName,
    comment = comment,
    fileExst = fileExst,
    viewUrl = "$viewUrl&version=$version",
    isCurrentVersion = false,
    editAccess = security?.editHistory == true,
    file = this
)
