package app.documents.core.manager

import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.Explorer

internal class ManagerRepositoryImpl(
    private val cloudPortal: CloudPortal?,
    private val cloudDataSource: CloudDataSource,
    private val managerService: ManagerService
) : ManagerRepository {

    override suspend fun updateDocumentServerVersion() {
        val documentServerVersion = managerService.getSettings().response.documentServer

        if (cloudPortal != null && documentServerVersion != null) {
            cloudDataSource.insertOrUpdatePortal(
                cloudPortal.copy(version = PortalVersion(documentServerVersion = documentServerVersion))
            )
        }
    }

    override suspend fun getExplorer(folderId: String, options: Map<String, String>): Explorer {
        return managerService.getExplorer(folderId, options).response
    }
}