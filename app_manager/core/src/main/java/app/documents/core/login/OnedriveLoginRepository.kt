package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.login.response.OnedriveUserResponse
import app.documents.core.network.ONEDRIVE_PORTAL_URL
import app.documents.core.network.login.OnedriveLoginDataSource

interface OnedriveLoginRepository : StorageLoginRepository

internal class OnedriveLoginRepositoryImpl(
    accountRepository: AccountRepository,
    onedriveLoginDataSource: OnedriveLoginDataSource
) : StorageLoginRepositoryImpl<OnedriveUserResponse, OnedriveLoginDataSource>(
    accountRepository = accountRepository,
    storageLoginDataSource = onedriveLoginDataSource
), OnedriveLoginRepository {

    override fun mapToCloudAccount(user: OnedriveUserResponse): CloudAccount {
        return CloudAccount(
            id = requireNotNull(user.id),
            portalUrl = ONEDRIVE_PORTAL_URL,
            login = user.mail.orEmpty(),
            name = user.displayName,
            portal = CloudPortal(
                url = ONEDRIVE_PORTAL_URL,
                provider = PortalProvider.Onedrive,
                settings = PortalSettings(
                    isSslState = true,
                    isSslCiphers = false
                )
            )
        )
    }
}