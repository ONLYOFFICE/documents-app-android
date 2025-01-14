package app.editors.manager.mvp.models.ui

import app.documents.core.model.cloud.Access

data class AccessUI(
    val access: Access,
    val title: Int,
    val icon: Int,
)