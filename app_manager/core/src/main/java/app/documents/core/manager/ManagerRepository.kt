package app.documents.core.manager

import app.documents.core.network.manager.models.explorer.Explorer

interface ManagerRepository {

    suspend fun updateDocumentServerVersion()

    suspend fun getExplorer(folderId: String, options: Map<String, String> = mapOf()): Explorer
}