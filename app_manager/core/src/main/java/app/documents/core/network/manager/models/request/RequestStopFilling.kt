package app.documents.core.network.manager.models.request

data class RequestStopFilling(
    val formId: Int,
    val action: Int = 0
)