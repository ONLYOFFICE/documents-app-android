package app.editors.manager.app

import android.accounts.Account
import android.app.Application
import android.database.Cursor
import android.net.Uri
import app.documents.core.storage.account.CloudAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.CryptUtils
import org.json.JSONObject

class AddAccountHelper(private val context: Application) {

    fun copyData() {
        CoroutineScope(Dispatchers.Default).launch {
            val cursor = context.contentResolver.query(Uri.parse("content://com.onlyoffice.projects.accounts/accounts"), null, null, null, null)
            val accounts = parseAccounts(cursor)
            val existAccounts = context.appComponent.accountsDao.getAccounts()
            accounts.forEach { account ->
                existAccounts.find { account.id == it.id } ?: run {
                    context.appComponent.accountsDao.addAccount(account)
                    addSystemAccount(account)
                }
            }
            cursor?.close()
        }
    }

    private fun addSystemAccount(cloudAccount: CloudAccount) {
        val account = Account(cloudAccount.getAccountName(), context.getString(R.string.account_type))
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

        val token = cloudAccount.getDecryptToken()
        val password = cloudAccount.getDecryptPassword() ?: ""

        if (!AccountUtils.addAccount(context, account, password, accountData)) {
            AccountUtils.setAccountData(context, account, accountData)
            AccountUtils.setPassword(context, account, password)
        }
        AccountUtils.setToken(context, account, token)
    }

    private fun parseAccounts(cursor: Cursor?): List<CloudAccount> {
        cursor?.let {
            val arrayList: ArrayList<CloudAccount> = ArrayList(cursor.count)
            while (cursor.moveToNext()) {
                val jsonObject = JSONObject()
                val names = cursor.columnNames
                repeat(cursor.columnCount) { index ->
                    jsonObject.put(names[index], cursor.getString(index))
                }
                arrayList.add(parse(jsonObject))
            }
            return arrayList
        } ?: run {
            return emptyList()
        }
    }

    private fun parse(json: JSONObject): CloudAccount {
        return CloudAccount(
            id = json.optString(CloudAccount::id.name),
            login = json.optString(CloudAccount::login.name),
            portal = json.optString(CloudAccount::portal.name),
            serverVersion = json.optString(CloudAccount::serverVersion.name),
            scheme = json.optString(CloudAccount::scheme.name),
            name = json.optString(CloudAccount::name.name),
            avatarUrl = json.optString(CloudAccount::avatarUrl.name),
            isSslCiphers = json.optString(CloudAccount::isSslCiphers.name) == "1",
            isSslState = json.optString(CloudAccount::isSslState.name) == "1",
            isOnline = json.optString(CloudAccount::isOnline.name) == "1",
            isWebDav = json.optString(CloudAccount::isWebDav.name) == "1",
            isOneDrive = json.optString(CloudAccount::isOneDrive.name) == "1",
            isDropbox = json.optString(CloudAccount::isDropbox.name) == "1",
            isAdmin = json.optString(CloudAccount::isAdmin.name) == "1",
            isVisitor = json.optString(CloudAccount::isVisitor.name) == "1",
        ).apply {
            token = json.optString(CloudAccount::token.name)
            password = json.optString(CloudAccount::password.name)
            expires = json.optString(CloudAccount::expires.name)
        }
    }

}
