package app.documents.core.account

import android.content.Context
import android.database.Cursor
import androidx.core.net.toUri
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.migration.CloudAccountWithTokenAndPassword
import app.documents.core.migration.OldCloudAccount
import app.documents.core.migration.toCloudAccountWithTokenAndPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class AddAccountHelper @Inject constructor(
    private val context: Context,
    private val cloudDataSource: CloudDataSource,
    private val accountRepository: AccountRepository,
) {

    companion object {

        private const val CONTENT_PATH = "content://com.onlyoffice.projects.accounts/accounts"
    }

    fun copyData() {
        CoroutineScope(Dispatchers.Default).launch {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(CONTENT_PATH.toUri(), null, null, null, null)

                val existAccounts = cloudDataSource.getAccounts()
                val accounts = parseCursorAccounts(cursor)
                if (accounts.isNotEmpty()) {
                    accounts.removeAll { data -> existAccounts.any { account -> account.id == data.cloudAccount.id } }
                    accountRepository.addAccounts(accounts)
                }
            } catch (_: Exception) {
                // Nothing
            } finally {
                cursor?.close()
            }
        }
    }

    private fun parseCursorAccounts(cursor: Cursor?): MutableList<CloudAccountWithTokenAndPassword> {
        return mutableListOf<CloudAccountWithTokenAndPassword>().apply {
            cursor?.let {
                while (cursor.moveToNext()) {
                    val jsonObject = JSONObject()
                    val names = cursor.columnNames
                    repeat(cursor.columnCount) { index -> jsonObject.put(names[index], cursor.getString(index)) }
                    add(parse(jsonObject))
                }
            }
        }
    }

    private fun parse(json: JSONObject): CloudAccountWithTokenAndPassword {
        return OldCloudAccount(
            id = json.optString(OldCloudAccount::id.name),
            login = json.optString(OldCloudAccount::login.name),
            portal = json.optString(OldCloudAccount::portal.name),
            serverVersion = json.optString(OldCloudAccount::serverVersion.name),
            scheme = json.optString(OldCloudAccount::scheme.name),
            name = json.optString(OldCloudAccount::name.name),
            avatarUrl = json.optString(OldCloudAccount::avatarUrl.name),
            isSslCiphers = json.optString(OldCloudAccount::isSslCiphers.name) == "1",
            isSslState = json.optString(OldCloudAccount::isSslState.name) == "1",
            isOnline = json.optString(OldCloudAccount::isOnline.name) == "1",
            isWebDav = json.optString(OldCloudAccount::isWebDav.name) == "1",
            isOneDrive = json.optString(OldCloudAccount::isOneDrive.name) == "1",
            isDropbox = json.optString(OldCloudAccount::isDropbox.name) == "1",
            isAdmin = json.optString(OldCloudAccount::isAdmin.name) == "1",
            isVisitor = json.optString(OldCloudAccount::isVisitor.name) == "1"
        ).toCloudAccountWithTokenAndPassword()
    }
}