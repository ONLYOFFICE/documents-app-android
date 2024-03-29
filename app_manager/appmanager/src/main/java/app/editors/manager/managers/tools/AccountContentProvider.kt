package app.editors.manager.managers.tools

import android.accounts.Account
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import app.documents.core.storage.account.AccountDao
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.appComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils

class AccountContentProvider : ContentProvider() {

    companion object {

        const val AUTHORITY = "com.onlyoffice.accounts"
        const val PATH = "accounts"
        const val ID = "id"
        const val TIME = "time"

        const val ALL_ID = 0
        const val ACCOUNT_ID = 1
        const val TIME_ID = 2

        const val CLOUD_ACCOUNT_KEY = "account"
        const val TIME_KEY = "time"
    }

    private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH, ALL_ID)
        addURI(AUTHORITY, "$PATH/*", ACCOUNT_ID)
        addURI(AUTHORITY, TIME, TIME_ID)
    }

    private var dao: AccountDao? = null
    private var pref: PreferenceTool? = null

    override fun onCreate(): Boolean {
        dao = context?.appComponent?.accountsDao
        pref = context?.appComponent?.preference
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
                    dao?.getCursorAccountsByLogin(arg)
                } ?: run {
                    dao?.getCursorAccounts()
                }
            }
            ACCOUNT_ID -> {
                return dao?.getCursorAccount(uri.lastPathSegment ?: "")
            }
            TIME_ID -> {
                return dao?.getCursorAccounts()?.apply {
                    extras = Bundle(1).apply {
                        putLong(TIME_KEY, pref?.dbTimestamp ?: 0L)
                    }
                }
            }
        }
        return null
    }

    override fun getType(uri: Uri): String {
        return CLOUD_ACCOUNT_KEY
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val account: CloudAccount = if (values?.containsKey(CLOUD_ACCOUNT_KEY) == true) {
            Json.decodeFromString(values.getAsString(CLOUD_ACCOUNT_KEY))
        } else {
            getAccount(values)
        }.apply {
            if (token.isNotEmpty()) {
                setCryptToken(token)
                setCryptPassword(password)
            }
        }
        runBlocking {
            val online = dao?.getAccountOnline()
            if (account.id == online?.id) {
                return@runBlocking dao?.addAccount(account.copy(isOnline = true))
            } else {
                return@runBlocking dao?.addAccount(account)
            }

        }
        addSystemAccount(account)
        return Uri.parse("content://$AUTHORITY/$PATH/${account.id}")
    }

    override fun insert(uri: Uri, values: ContentValues?, extras: Bundle?): Uri? {
        var id = ""
        extras?.let {
            if (extras.containsKey(CLOUD_ACCOUNT_KEY)) {
                val account: CloudAccount = Json.decodeFromString(extras.getString(CLOUD_ACCOUNT_KEY) ?: "")
                id = account.id
                runBlocking {
                    dao?.addAccount(account = account)
                }
                addSystemAccount(account)
            } else {
                val account: CloudAccount = getAccount(it).apply {
                    if (token.isNotEmpty()) {
                        setCryptToken(token)
                        setCryptPassword(password)
                    }
                }
                id = account.id
                runBlocking {
                    val online = dao?.getAccountOnline()
                    if (online?.id == id) {
                        dao?.addAccount(account = account.copy(isOnline = true))
                    } else {
                        dao?.addAccount(account = account)
                    }
                }
                addSystemAccount(account)
            }
        }
        return Uri.parse("content://$AUTHORITY/$PATH/$id")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        when (uriMatcher.match(uri)) {
            ACCOUNT_ID -> {
                runBlocking {
                    dao?.getAccount(uri.lastPathSegment ?: "")?.let { account ->
                        return@let dao?.deleteCursorAccount(account)
                    }

                }
            }
        }
        return -1
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return runBlocking {
            return@runBlocking dao?.updateCursorAccount(getAccount(values)) ?: -1
        }
    }

    private fun addSystemAccount(cloudAccount: CloudAccount) {
        val account = Account(cloudAccount.getAccountName(), context?.getString(R.string.account_type))
        val accountData = AccountData(
            portal = cloudAccount.portal ?: "",
            scheme = cloudAccount.scheme ?: "",
            displayName = cloudAccount.name ?: "",
            userId = cloudAccount.id,
            provider = cloudAccount.provider ?: "",
            accessToken = "",
            email = cloudAccount.login ?: "",
            avatar = cloudAccount.avatarUrl,
            expires = ""
        )

        val token: String
        val password: String
        if (cloudAccount.token.isNotEmpty()) {
            token = cloudAccount.getDecryptToken() ?: ""
            password = cloudAccount.getDecryptPassword() ?: ""
        } else {
            token = ""
            password = ""
        }

        if (!AccountUtils.addAccount(checkNotNull(context), account, password, accountData)) {
            AccountUtils.setAccountData(checkNotNull(context), account, accountData)
            AccountUtils.setPassword(checkNotNull(context), account, password)
        }
        AccountUtils.setToken(checkNotNull(context), account, token)
    }

    private fun getAccount(values: ContentValues?): CloudAccount {
        return CloudAccount(
            id = values?.getAsString("id") ?: "",
            login = values?.getAsString("login"),
            portal = values?.getAsString("portal"),
            serverVersion = values?.getAsString("serverVersion") ?: "",
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
            isVisitor = values?.getAsBoolean("isVisitor") ?: false
        ).apply {
            token = values?.getAsString("token") ?: ""
            password = values?.getAsString("password") ?: ""
            expires = values?.getAsString("expires") ?: ""
        }
    }

    private fun getAccount(extras: Bundle): CloudAccount {
        return CloudAccount(
            id = extras.getString("id") ?: "",
            login = extras.getString("login"),
            portal = extras.getString("portal"),
            serverVersion = extras.getString("serverVersion") ?: "",
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
            isVisitor = extras.getBoolean("isVisitor", false)
        ).apply {
            token = extras.getString("token") ?: ""
            password = extras.getString("password") ?: ""
            expires = extras.getString("expires") ?: ""
        }
    }
}