package app.documents.core.account

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class AccountPreferences @Inject constructor(context: Context) {

    companion object {

        private const val ONLINE_ACCOUNT_ID_KEY = "online_account"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(
        AccountPreferences::class.simpleName, Context.MODE_PRIVATE
    )

    var onlineAccountId: String?
        get() = preferences.getString(ONLINE_ACCOUNT_ID_KEY, null)
        set(value) {
            preferences.edit().putString(ONLINE_ACCOUNT_ID_KEY, value).apply()
        }

}