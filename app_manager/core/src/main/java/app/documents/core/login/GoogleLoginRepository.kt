package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.login.GoogleUser
import app.documents.core.network.GOOGLE_DRIVE_URL
import app.documents.core.network.login.GoogleLoginDataSource

interface GoogleLoginRepository : StorageLoginRepository

internal class GoogleLoginRepositoryImpl(
    accountRepository: AccountRepository,
    googleLoginDataSource: GoogleLoginDataSource
) : StorageLoginRepositoryImpl<GoogleUser, GoogleLoginDataSource>(
    accountRepository = accountRepository,
    storageLoginDataSource = googleLoginDataSource
), GoogleLoginRepository {

    override fun mapToCloudAccount(user: GoogleUser): CloudAccount {
        return CloudAccount(
            id = user.permissionId,
            portalUrl = GOOGLE_DRIVE_URL,
            avatarUrl = user.photoLink,
            login = user.emailAddress,
            name = user.displayName,
            portal = CloudPortal(
                url = GOOGLE_DRIVE_URL,
                provider = PortalProvider.GoogleDrive,
                settings = PortalSettings(
                    isSslState = true,
                    isSslCiphers = false
                )
            )
        )
    }
}