package app.editors.manager.managers.utils

import app.documents.core.model.cloud.Access
import lib.toolkit.base.managers.utils.EditorsContract

fun Access.toEditAccess(): Int {
    return when (this) {
        Access.Comment -> EditorsContract.INTERNAL_EDIT_ACCESS_COMMENT
        Access.FormFiller -> EditorsContract.INTERNAL_EDIT_ACCESS_FILLING_FORMS
        Access.Review -> EditorsContract.INTERNAL_EDIT_ACCESS_TRACKED_CHANGES
        Access.CustomFilter -> EditorsContract.INTERNAL_EDIT_ACCESS_CUSTOM_FILTER
        Access.Read -> EditorsContract.INTERNAL_EDIT_ACCESS_READ
        Access.Restrict -> EditorsContract.INTERNAL_EDIT_ACCESS_RESTRICT
        else -> EditorsContract.INTERNAL_EDIT_ACCESS_EDIT
    }
}