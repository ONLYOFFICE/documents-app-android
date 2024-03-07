package app.documents.core.account

import android.accounts.Account
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import app.documents.core.database.datasource.CloudCursorDataSource
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import org.json.JSONObject
import javax.inject.Inject

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

    @Inject
    lateinit var cloudCursorDataSource: CloudCursorDataSource

    @Inject
    lateinit var cloudDataSource: CloudDataSource

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var accountPreferences: AccountPreferences

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        selection: String?,
        selectionArgs: Array<out String?>?,
        sortOrder: String?
    ): Cursor? {
        when (uriMatcher.match(uri)) {
            ALL_ID -> {
                return selectionArgs?.get(0)?.let { arg ->
                    cloudCursorDataSource.getCursorAccountsByLogin(arg)
                } ?: run {
                    cloudCursorDataSource.getCursorAccounts()
                }
            }
            ACCOUNT_ID -> {
                return cloudCursorDataSource.getCursorAccount(uri.lastPathSegment ?: "")
            }
            TIME_ID -> {
                return cloudCursorDataSource.getCursorAccounts()?.apply {
                    extras = bundleOf(TIME_KEY to cloudCursorDataSource.dbTimestamp)
                }
            }
        }
        return null
    }

    override fun getType(uri: Uri): String {
        return CLOUD_ACCOUNT_KEY
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val token: String
        val password: String
        val expires: String
        val isOnline: Boolean
        val account: CloudAccount = if (values?.containsKey(CLOUD_ACCOUNT_KEY) == true) {
            val json = values.getAsString(CLOUD_ACCOUNT_KEY)
            val list = parseTokenAndPasswordAndExpires(json)
            token = list[0]
            password = list[1]
            expires = list[2]
            isOnline = parseIsOnline(json)
            Json.decodeFromString(json)
        } else {
            token = values?.getAsString("token").orEmpty()
            password = values?.getAsString("password").orEmpty()
            expires = values?.getAsString("expires").orEmpty()
            isOnline = values?.getAsBoolean("isOnline") == true
            getAccount(values)
        }

        insertAccount(account)
        addSystemAccount(account, token, password, expires, isOnline)
        return Uri.parse("content://$AUTHORITY/$PATH/${account.id}")
    }

    override fun insert(uri: Uri, values: ContentValues?, extras: Bundle?): Uri? {
        var token = ""
        var password = ""
        var expires = ""
        var isOnline = false

        val account = extras?.let { bundle ->
            if (extras.containsKey(CLOUD_ACCOUNT_KEY)) {
                val json = extras.getString(CLOUD_ACCOUNT_KEY).orEmpty()
                val list = parseTokenAndPasswordAndExpires(extras.getString(CLOUD_ACCOUNT_KEY))
                token = list[0]
                password = list[1]
                expires = list[2]
                isOnline = parseIsOnline(extras.getString(CLOUD_ACCOUNT_KEY))
                Json.decodeFromString<CloudAccount?>(json)
            } else {
                token = extras.getString("token").orEmpty()
                password = extras.getString("password").orEmpty()
                expires = extras.getString("expires").orEmpty()
                isOnline = extras.getBoolean("isOnline")
                getAccount(bundle)
            }
        } ?: return null

        insertAccount(account)
        addSystemAccount(account, token, password, expires, isOnline)
        return Uri.parse("content://$AUTHORITY/$PATH/${account.id}")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        when (uriMatcher.match(uri)) {
            ACCOUNT_ID -> {
                runBlocking {
                    cloudCursorDataSource.getAccount(uri.lastPathSegment.orEmpty())?.let { account ->
                        return@let cloudCursorDataSource.deleteCursorAccount(account)
                    }
                }
            }
        }
        return -1
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return runBlocking {
            return@runBlocking cloudCursorDataSource.updateCursorAccount(getAccount(values))
        }
    }

    private fun insertAccount(account: CloudAccount) {
        runBlocking {
            cloudDataSource.insertOrUpdateAccount(account)
            cloudDataSource.insertOrUpdatePortal(account.portal)
        }
    }

    private fun addSystemAccount(
        cloudAccount: CloudAccount,
        token: String,
        password: String,
        expires: String,
        isOnline: Boolean
    ) {
        val account = Account(cloudAccount.accountName, context?.getString(R.string.account_type))
        val accountData = AccountData(
            portal = cloudAccount.portal.url,
            scheme = cloudAccount.portal.scheme.value,
            displayName = cloudAccount.name,
            userId = cloudAccount.id,
            email = cloudAccount.login,
            avatar = cloudAccount.avatarUrl,
            expires = expires
        )

        if (!accountManager.addAccount(account, password, accountData)) {
            accountManager.setAccountData(account, accountData)
            accountManager.setPassword(account, password)
        }
        accountManager.setToken(account, token)
        if (isOnline) {
            accountPreferences.onlineAccountId = cloudAccount.id
        }
    }

    private fun getAccount(values: ContentValues?): CloudAccount {
        return getAccount(
            id = values?.getAsString("id").orEmpty(),
            login = values?.getAsString("login").orEmpty(),
            portal = values?.getAsString("portal").orEmpty(),
            serverVersion = values?.getAsString("serverVersion").orEmpty(),
            scheme = values?.getAsString("scheme").orEmpty(),
            name = values?.getAsString("name").orEmpty(),
            avatarUrl = values?.getAsString("avatarUrl").orEmpty(),
            isSslCiphers = values?.getAsBoolean("isSslCiphers") ?: false,
            isSslState = values?.getAsBoolean("isSslState") ?: true,
            isAdmin = values?.getAsBoolean("isAdmin") ?: false,
            isVisitor = values?.getAsBoolean("isVisitor") ?: false
        )
    }

    private fun getAccount(extras: Bundle): CloudAccount {
        return getAccount(
            id = extras.getString("id").orEmpty(),
            login = extras.getString("login").orEmpty(),
            name = extras.getString("name").orEmpty(),
            portal = extras.getString("portal").orEmpty(),
            serverVersion = extras.getString("serverVersion").orEmpty(),
            scheme = extras.getString("scheme").orEmpty(),
            avatarUrl = extras.getString("avatarUrl").orEmpty(),
            isSslCiphers = extras.getBoolean("isSslCiphers", false),
            isSslState = extras.getBoolean("isSslState", true),
            isAdmin = extras.getBoolean("isAdmin", false),
            isVisitor = extras.getBoolean("isVisitor", false)
        )
    }

    private fun getAccount(
        id: String,
        login: String,
        name: String,
        portal: String,
        serverVersion: String,
        scheme: String,
        avatarUrl: String,
        isSslCiphers: Boolean,
        isSslState: Boolean,
        isAdmin: Boolean,
        isVisitor: Boolean
    ): CloudAccount {
        return CloudAccount(
            id = id,
            portalUrl = portal,
            login = login,
            avatarUrl = avatarUrl,
            name = name,
            isAdmin = isAdmin,
            isVisitor = isVisitor,
            portal = CloudPortal(
                scheme = Scheme.valueOf(scheme),
                url = portal,
                version = PortalVersion(serverVersion = serverVersion),
                settings = PortalSettings(isSslState, isSslCiphers)
            )
        )
    }

    private fun parseTokenAndPasswordAndExpires(json: String?): List<String> {
        return if (!json.isNullOrEmpty()) {
            listOf(
                JSONObject(json).getString("token"),
                JSONObject(json).getString("password"),
                JSONObject(json).getString("expires")
            )
        } else emptyList()
    }

    private fun parseIsOnline(json: String?): Boolean {
        return if (!json.isNullOrEmpty()) {
            JSONObject(json).getBoolean("isOnline")
        } else false
    }
}