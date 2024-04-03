package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.login.response.DropboxUserResponse
import app.documents.core.network.DROPBOX_PORTAL_URL
import app.documents.core.network.login.DropboxLoginDataSource

interface DropboxLoginRepository : StorageLoginRepository

internal class DropboxLoginRepositoryImpl(
    accountRepository: AccountRepository,
    dropboxLoginDataSource: DropboxLoginDataSource
) : StorageLoginRepositoryImpl<DropboxUserResponse, DropboxLoginDataSource>(
    accountRepository = accountRepository,
    storageLoginDataSource = dropboxLoginDataSource
), DropboxLoginRepository {

    override fun mapToCloudAccount(user: DropboxUserResponse): CloudAccount {
        return CloudAccount(
            id = requireNotNull(user.accountId),
            portalUrl = DROPBOX_PORTAL_URL,
            avatarUrl = user.profilePhotoUrl.orEmpty(),
            login = user.email.orEmpty(),
            name = user.name?.displayName.orEmpty(),
            portal = CloudPortal(
                url = DROPBOX_PORTAL_URL,
                provider = PortalProvider.Dropbox,
                settings = PortalSettings(
                    isSslState = true,
                    isSslCiphers = false
                )
            )
        )
    }
}