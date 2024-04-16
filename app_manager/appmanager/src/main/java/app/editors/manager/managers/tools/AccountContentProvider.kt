package app.editors.manager.managers.tools

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import app.documents.core.account.AccountManager
import app.documents.core.account.AccountRepository
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.migration.CloudAccountWithTokenAndPassword
import app.documents.core.migration.OldCloudAccount
import app.documents.core.migration.toCloudAccountWithTokenAndPassword
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.app.App
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.CryptUtils


class AccountContentProvider : ContentProvider() {

    companion object {

        private const val AUTHORITY = "com.onlyoffice.accounts"
        private const val PATH = "accounts"
        private const val TIME = "time"

        private const val ALL_ID = 0
        private const val ACCOUNT_ID = 1
        private const val TIME_ID = 2

        private const val CLOUD_ACCOUNT_KEY = "account"
        private const val TIME_KEY = "time"
    }

    private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH, ALL_ID)
        addURI(AUTHORITY, "$PATH/*", ACCOUNT_ID)
        addURI(AUTHORITY, TIME, TIME_ID)
    }

    private var cloudDataSource: CloudDataSource? = null
    private var accountRepository: AccountRepository? = null

    override fun onCreate(): Boolean {
        App.getApp().refreshLoginComponent(null)
        accountRepository = App.getApp().loginComponent.accountRepository
        cloudDataSource = App.getApp().appComponent.cloudDataSource
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?
    ): Cursor? {
        return cloudDataSource?.let { dataSource ->
            runBlocking {
                when (uriMatcher.match(uri)) {
                    ACCOUNT_ID -> getMatrixCursor(
                        uri,
                        dataSource.getAccount(uri.lastPathSegment.orEmpty())
                    )

                    ALL_ID -> selectionArgs?.get(0)?.let { arg ->
                        getMatrixCursor(uri, dataSource.getAccountByLogin(arg))
                    } ?: run {
                        getMatrixCursor(uri, *dataSource.getAccounts().toTypedArray())
                    }

                    TIME_ID -> getMatrixCursor(uri, *dataSource.getAccounts().toTypedArray())?.apply {
                        extras = bundleOf(TIME_KEY to cloudDataSource?.initTimestamp)
                    }

                    else -> null
                }
            }
        }
    }

    override fun getType(uri: Uri): String {
        return CLOUD_ACCOUNT_KEY
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return try {
            val data = if (values?.containsKey(CLOUD_ACCOUNT_KEY) == true) {
                val json = values.getAsString(CLOUD_ACCOUNT_KEY)
                Json.decodeFromString<OldCloudAccount>(json).toCloudAccountWithTokenAndPassword()
            } else {
                getAccountWithTokenAndPassword(values)
            }

            insertAccount(data)
            Uri.parse("content://$AUTHORITY/$PATH/${data.cloudAccount.id}")
        } catch (_: Exception) {
            null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?, extras: Bundle?): Uri? {
        return try {
            val data = extras?.let { bundle ->
                if (extras.containsKey(CLOUD_ACCOUNT_KEY)) {
                    val json = extras.getString(CLOUD_ACCOUNT_KEY).orEmpty()
                    Json.decodeFromString<OldCloudAccount>(json).toCloudAccountWithTokenAndPassword()
                } else {
                    getAccountWithTokenAndPassword(bundle)
                }
            }

            insertAccount(requireNotNull(data))
            Uri.parse("content://$AUTHORITY/$PATH/${data.cloudAccount.id}")
        } catch (_: Exception) {
            null
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return if (uriMatcher.match(uri) == ACCOUNT_ID) {
            runBlocking { cloudDataSource?.deleteAccount(uri.lastPathSegment.orEmpty()) ?: -1 }
        } else -1
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return runBlocking {
            val data = getAccountWithTokenAndPassword(values)
            accountRepository?.updateAccountData(
                data.cloudAccount.id,
                data.cloudAccount.accountName,
                data.token,
                data.password
            )
            cloudDataSource?.updateAccount(data.cloudAccount) ?: -1
        }
    }

    private fun insertAccount(cloudAccount: CloudAccountWithTokenAndPassword) {
        runBlocking { accountRepository?.addAccounts(listOf(cloudAccount)) }
    }

    private fun getMatrixCursor(uri: Uri, vararg accounts: CloudAccount?): MatrixCursor? {
        if (accounts.filterNotNull().isEmpty()) {
            return null
        }

        return context?.let { context ->
            val accountManager = AccountManager(context)
            val cursor = MatrixCursor(
                arrayOf(
                    "id",
                    "login",
                    "portal",
                    "serverVersion",
                    "scheme",
                    "name",
                    "provider",
                    "avatarUrl",
                    "isSslCiphers",
                    "isSslState",
                    "isAdmin",
                    "isVisitor",
                    "token",
                    "password",
                    "expires"
                )
            )

            for (account in accounts) {
                if (account == null) continue
                cursor.newRow().apply {
                    add("id", account.id)
                    add("login", account.login)
                    add("portal", account.portal.url)
                    add("serverVersion", account.portal.version.serverVersion)
                    add("scheme", account.portal.scheme.value)
                    add("name", account.name)
                    add("provider", account.socialProvider)
                    add("avatarUrl", account.avatarUrl)
                    add("isSslCiphers", account.portal.settings.isSslCiphers)
                    add("isSslState", account.portal.settings.isSslState)
                    add("isAdmin", account.isAdmin)
                    add("isVisitor", account.isVisitor)
                    add("expires", "-1")
                    add(
                        "token",
                        CryptUtils.encryptAES128(accountManager.getToken(account.accountName), account.id)
                    )
                    add(
                        "password",
                        CryptUtils.encryptAES128(accountManager.getPassword(account.accountName), account.id)
                    )
                }
            }
            cursor.also { it.setNotificationUri(context.contentResolver, uri) }
        }
    }

    private fun getAccountWithTokenAndPassword(values: ContentValues?): CloudAccountWithTokenAndPassword {
        return OldCloudAccount(
            id = values?.getAsString("id").orEmpty(),
            login = values?.getAsString("login"),
            portal = values?.getAsString("portal"),
            serverVersion = values?.getAsString("serverVersion").orEmpty(),
            scheme = values?.getAsString("scheme"),
            name = values?.getAsString("name"),
            provider = values?.getAsString("provider"),
            avatarUrl = values?.getAsString("avatarUrl"),
            isSslCiphers = values?.getAsBoolean("isSslCiphers") ?: false,
            isSslState = values?.getAsBoolean("isSslState") ?: true,
            isOnline = false,
            isWebDav = false,
            isOneDrive = false,
            isDropbox = false,
            isAdmin = values?.getAsBoolean("isAdmin") ?: false,
            isVisitor = values?.getAsBoolean("isVisitor") ?: false,
            token = values?.getAsString("token").orEmpty(),
            password = values?.getAsString("password").orEmpty(),
            expires = values?.getAsString("expires").orEmpty()
        ).toCloudAccountWithTokenAndPassword()
    }

    private fun getAccountWithTokenAndPassword(extras: Bundle): CloudAccountWithTokenAndPassword {
        return OldCloudAccount(
            id = extras.getString("id").orEmpty(),
            login = extras.getString("login"),
            portal = extras.getString("portal"),
            serverVersion = extras.getString("serverVersion").orEmpty(),
            scheme = extras.getString("scheme"),
            name = extras.getString("name"),
            provider = extras.getString("provider"),
            avatarUrl = extras.getString("avatarUrl"),
            isSslCiphers = extras.getBoolean("isSslCiphers", false),
            isSslState = extras.getBoolean("isSslState", true),
            isOnline = false,
            isWebDav = false,
            isOneDrive = false,
            isDropbox = false,
            isAdmin = extras.getBoolean("isAdmin", false),
            isVisitor = extras.getBoolean("isVisitor", false),
            token = extras.getString("token").orEmpty(),
            password = extras.getString("password").orEmpty(),
            expires = extras.getString("expires").orEmpty()
        ).toCloudAccountWithTokenAndPassword()
    }
}