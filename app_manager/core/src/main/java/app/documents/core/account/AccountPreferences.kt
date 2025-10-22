package app.documents.core.account

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit

class AccountPreferences @Inject constructor(context: Context) {

    companion object {

        private const val ONLINE_ACCOUNT_ID_KEY = "online_account"
        private const val ONLINE_ACCOUNT_IS_REGULAR_KEY = "is_regular"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(
        AccountPreferences::class.simpleName, Context.MODE_PRIVATE
    )

    var onlineAccountId: String?
        get() = preferences.getString(ONLINE_ACCOUNT_ID_KEY, null)
        set(value) {
            preferences.edit { putString(ONLINE_ACCOUNT_ID_KEY, value) }
        }

    var isRegularUser: Boolean
        get() = preferences.getBoolean(ONLINE_ACCOUNT_IS_REGULAR_KEY, false)
        set(value) {
            preferences.edit { putBoolean(ONLINE_ACCOUNT_IS_REGULAR_KEY, value) }
        }

}