package app.documents.core.utils

interface FirebaseTool {

    suspend fun isCoauthoring(): Boolean
}