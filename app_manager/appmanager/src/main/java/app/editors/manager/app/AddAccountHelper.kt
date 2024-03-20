package app.editors.manager.app

import android.app.Application
import android.database.Cursor
import android.net.Uri
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AddAccountHelper(private val context: Application) {

    private data class CloudAccountWithToken(
        val cloudAccount: CloudAccount,
        val token: String,
        val password: String
    )

    fun copyData() {
        App.getApp().refreshLoginComponent(null)
        CoroutineScope(Dispatchers.Default).launch {
            val cursor = context.contentResolver.query(
                Uri.parse("content://com.onlyoffice.projects.accounts/accounts"),
                null,
                null,
                null,
                null
            )
            val accounts = parseAccounts(cursor)
            val existAccounts = context.coreComponent.cloudDataSource.getAccounts()

            with(App.getApp().loginComponent.accountRepository) {
                accounts.forEach { accountWithToken ->
                    existAccounts.find { accountWithToken.cloudAccount.id == it.id } ?: run {
                        addAccount(
                            cloudAccount = accountWithToken.cloudAccount,
                            accessToken = accountWithToken.token,
                            password = accountWithToken.password
                        )
                    }
                }
            }
            cursor?.close()
        }
    }

    private fun parseAccounts(cursor: Cursor?): List<CloudAccountWithToken> {
        cursor?.let {
            val arrayList: ArrayList<CloudAccountWithToken> = ArrayList(cursor.count)
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

    private fun parse(json: JSONObject): CloudAccountWithToken {
        return CloudAccountWithToken(
            cloudAccount = CloudAccount(
                id = json.optString(CloudAccount::id.name),
                login = json.optString(CloudAccount::login.name),
                name = json.optString(CloudAccount::name.name),
                avatarUrl = json.optString(CloudAccount::avatarUrl.name),
                portalUrl = json.optString("portal"),
                isAdmin = json.optString(CloudAccount::isAdmin.name) == "1",
                isVisitor = json.optString(CloudAccount::isVisitor.name) == "1",
                portal = CloudPortal(
                    url = json.optString("portal"),
                    scheme = Scheme.valueOf(json.optString("scheme")),
                    settings = PortalSettings(
                        isSslCiphers = json.optString("isSslCiphers") == "1",
                        isSslState = json.optString("isSslState") == "1",
                    ),
                    version = PortalVersion(serverVersion = json.optString("serverVersion"))
                ),
            ),
            token = json.optString("token"),
            password = json.optString("password")
        )
    }
}

