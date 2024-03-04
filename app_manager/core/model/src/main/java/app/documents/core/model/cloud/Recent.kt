package app.documents.core.model.cloud

data class Recent(
    val id: Int,
    val idFile: String = "",
    val path: String = "",
    val name: String = "",
    val date: Long = 0,
    val size: Long = 0,
    val ownerId: String? = null,
    val source: Provider? = null
)