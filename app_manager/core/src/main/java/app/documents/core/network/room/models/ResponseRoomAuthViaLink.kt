package app.documents.core.network.room.models

data class ResponseRoomAuthViaLink(val id: String? = null, val status: Int) {

    companion object {

        const val STATUS_OK = 0
        const val STATUS_FAILED = 4
    }
}