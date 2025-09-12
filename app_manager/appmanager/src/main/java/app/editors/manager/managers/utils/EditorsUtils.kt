package app.editors.manager.managers.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.documents.core.model.cloud.Access
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.tools.FileGroup
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.EditorsType
import lib.toolkit.base.managers.utils.StringUtils

object EditorsUtils {

    fun getLocalEditorIntent(
        context: Context,
        uri: Uri,
        editType: EditType,
        access: Access
    ): Intent {
        val extension = FileExtensions.Companion.fromPath(uri.path.orEmpty())
        return getEditorIntent(
            context = context,
            uri = uri,
            extension = extension,
            editType = editType,
            access = access
        )
    }

    fun getDocumentServerEditorIntent(
        context: Context,
        data: String,
        extension: String,
        editType: EditType,
        access: Access,
        roomId: String? = null,
        fileId: String? = null
    ): Intent {
        return getEditorIntent(
            context = context,
            uri = null,
            info = data,
            extension = FileExtensions.Companion.fromExtension(extension),
            editType = editType,
            access = access,
            roomId = roomId,
            fileId = fileId
        )
    }

    private fun getEditorIntent(
        context: Context,
        uri: Uri?,
        extension: FileExtensions,
        editType: EditType,
        access: Access,
        info: String? = null,
        roomId: String? = null,
        fileId: String? = null
    ): Intent {
        val type = when (extension.group) {
            FileGroup.DOCUMENT -> EditorsType.DOCS
            FileGroup.SHEET -> EditorsType.CELLS
            FileGroup.PRESENTATION -> EditorsType.PRESENTATION
            FileGroup.PDF -> EditorsType.PDF
            else -> throw RuntimeException("invalid extension")
        }

        return getIntent(
            context = context,
            uri = uri,
            info = info,
            editType = if (extension in arrayOf(FileExtensions.HWP, FileExtensions.HWPX)) {
                EditType.View()
            } else {
                editType
            },
            access = access,
            roomId = roomId,
            fileId = fileId
        ).apply {
            val className = when (type) {
                EditorsType.DOCS -> EditorsContract.EDITOR_DOCUMENTS
                EditorsType.CELLS -> EditorsContract.EDITOR_CELLS
                EditorsType.PRESENTATION -> EditorsContract.EDITOR_SLIDES
                EditorsType.PDF -> if (editType is EditType.View) {
                    EditorsContract.PDF
                } else {
                    EditorsContract.EDITOR_DOCUMENTS
                }
            }
            setClassName(context, className)
        }
    }

    private fun getIntent(
        context: Context,
        uri: Uri?,
        editType: EditType,
        access: Access,
        info: String? = null,
        roomId: String? = null,
        fileId: String? = null
    ): Intent {
        return Intent().apply {
            data = uri
            action = Intent.ACTION_VIEW
            info?.let { putExtra(EditorsContract.KEY_DOC_SERVER, info) }
            putExtra(EditorsContract.KEY_HELP_URL, StringUtils.getHelpUrl(context))
            putExtra(EditorsContract.KEY_EDIT_TYPE, editType)
            putExtra(EditorsContract.KEY_EDIT_ACCESS, access.toEditAccess())
            putExtra(EditorsContract.EXTRA_ROOM_ID, roomId)
            putExtra(EditorsContract.EXTRA_ITEM_ID, fileId)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}