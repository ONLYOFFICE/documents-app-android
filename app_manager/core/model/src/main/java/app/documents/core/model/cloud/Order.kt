package app.documents.core.model.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Order(

    @SerialName("order")
    val order: String,

    @SerialName("entryId")
    val entryId: String,

    @SerialName("entryType")
    val entryType: Int
) {

    companion object {

        const val ENTRY_TYPE_FILE = 2
        const val ENTRY_TYPE_FOLDER = 1
    }
}