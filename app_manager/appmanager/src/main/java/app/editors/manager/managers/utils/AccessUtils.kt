package app.editors.manager.managers.utils

import app.documents.core.model.cloud.Access
import lib.editors.gbase.rx.EditAccess

fun Access.toEditAccess(): EditAccess {
    return when (this) {
        Access.Comment -> EditAccess.Comments
        Access.FormFiller -> EditAccess.FillingForms
        Access.Review -> EditAccess.TrackedChanges
        Access.CustomFilter -> EditAccess.ReadOnly
        Access.Read -> EditAccess.ReadOnly
        Access.Restrict -> EditAccess.ReadOnly
        else -> EditAccess.None
    }
}