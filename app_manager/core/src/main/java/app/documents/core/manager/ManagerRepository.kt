package app.documents.core.manager

interface ManagerRepository {

    suspend fun updateDocumentServerVersion()
}