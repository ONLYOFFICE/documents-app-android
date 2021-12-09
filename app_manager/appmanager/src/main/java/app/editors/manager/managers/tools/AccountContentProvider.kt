package app.editors.manager.managers.tools

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import app.documents.core.account.AccountDao
import app.documents.core.account.CloudAccount
import app.editors.manager.app.appComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AccountContentProvider : ContentProvider() {

    companion object {

        const val AUTHORITY = "com.onlyoffice.accounts"
        const val PATH = "accounts"
        const val ID = "id"

        const val ALL = 0
        const val ACCOUNT_ID = 1

        const val CLOUD_ACCOUNT_KEY = "account"
    }

    private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH, ALL)
        addURI(AUTHORITY, "$PATH/*", ACCOUNT_ID)
    }

    private var dao: AccountDao? = null

    override fun onCreate(): Boolean {
        dao = context?.appComponent?.accountsDao
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        when (uriMatcher.match(uri)) {
            ALL -> {
                return dao?.getCursorAccounts()
            }
            ACCOUNT_ID -> {
                return dao?.getCursorAccount(uri.lastPathSegment ?: "")
            }
        }
        return null
    }

    override fun getType(uri: Uri): String {
        return "account"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val account: CloudAccount = getAccount(values)
        runBlocking {
            dao?.addAccount(account)
        }
        return Uri.parse("content://content://com.onlyoffice.accounts/accounts/${account.id}")
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
            }
        }
        return Uri.parse("content://content://com.onlyoffice.accounts/accounts/$id")
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

    private fun getAccount(values: ContentValues?): CloudAccount {
        return CloudAccount(
            id = values?.getAsString("id") ?: "",
            login = values?.getAsString("login"),
            portal = values?.getAsString("portal"),
            serverVersion = values?.getAsString("server_version") ?: "",
            scheme = values?.getAsString("scheme"),
            name = values?.getAsString("name"),
            provider = values?.getAsString("provider"),
            avatarUrl = values?.getAsString("avatar_url"),
            isSslCiphers = values?.getAsBoolean("ciphers") ?: false,
            isSslState = values?.getAsBoolean("ssl") ?: true,
            isOnline = false,
            isWebDav = false,
            isOneDrive = false,
            isDropbox = false,
            isAdmin = values?.getAsBoolean("admin") ?: false,
            isVisitor = values?.getAsBoolean("visitor") ?: false
        ).apply {
            token = values?.getAsString("token") ?: ""
            password = values?.getAsString("password") ?: ""
        }
    }
}