package app.editors.manager.app

import android.app.Application
import android.database.Cursor
import android.net.Uri
import app.documents.core.account.CloudAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AddAccountHelper(private val context: Application) {

    fun copyData() {
        CoroutineScope(Dispatchers.Default).launch {
            val cursor = context.contentResolver.query(Uri.parse("content://com.onlyoffice.accounts/accounts"), null, null, null, null)
            val accounts = parseAccounts(cursor)
            val existAccounts = context.appComponent.accountsDao.getAccounts()
            accounts.forEach { account ->
                existAccounts.find { account.id == it.id } ?: run {
                    context.appComponent.accountsDao.addAccount(account)
                }
            }
            cursor?.close()
        }
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
            id = json.getString(CloudAccount::id.name),
            login = json.getString(CloudAccount::login.name),
            portal = json.getString(CloudAccount::portal.name),
            serverVersion = json.getString(CloudAccount::serverVersion.name),
            scheme = json.getString(CloudAccount::scheme.name),
            name = json.getString(CloudAccount::name.name),
            avatarUrl = json.getString(CloudAccount::avatarUrl.name),
            isSslCiphers = json.getString(CloudAccount::isSslCiphers.name) == "1",
            isSslState = json.getString(CloudAccount::isSslState.name) == "1",
            isOnline = json.getString(CloudAccount::isOnline.name) == "1",
            isWebDav = json.getString(CloudAccount::isWebDav.name) == "1",
            isOneDrive = json.getString(CloudAccount::isOneDrive.name) == "1",
            isDropbox = json.getString(CloudAccount::isDropbox.name) == "1",
            isAdmin = json.getString(CloudAccount::isAdmin.name) == "1",
            isVisitor = json.getString(CloudAccount::isVisitor.name) == "1",
        ).apply {
            token = json.getString(CloudAccount::token.name)
            password = json.getString(CloudAccount::password.name)
            expires = json.getString(CloudAccount::expires.name)
        }
    }

}
