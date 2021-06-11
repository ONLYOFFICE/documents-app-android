package app.editors.manager.app

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.documents.core.account.AccountDao
import app.documents.core.account.CloudAccount
import app.documents.core.account.RecentDao
import app.documents.core.login.LoginResponse
import app.documents.core.network.models.login.request.RequestSignIn
import app.documents.core.network.models.login.response.ResponseSignIn
import app.documents.core.network.models.login.response.ResponseUser
import app.documents.core.settings.NetworkSettings
import app.editors.manager.managers.tools.AccountSqlTool
import app.editors.manager.mvp.models.account.AccountsSqlData
import app.editors.manager.mvp.models.account.Recent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        MigrateDb().checkMigrateDataBase()
    }
}

class MigrateDb{

    @Inject
    lateinit var accountDao: AccountDao
    @Inject
    lateinit var recentDao: RecentDao
    @Inject
    lateinit var accountSqlTool: AccountSqlTool
    @Inject
    lateinit var networkSettings: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    fun checkMigrateDataBase() {
        val sqlAccounts = accountSqlTool.accounts
        if (sqlAccounts.isEmpty()) {
            return
        } else {
            migrate(sqlAccounts)
        }
    }

    private fun migrate(sqlAccounts: List<AccountsSqlData>) {
        CoroutineScope(Dispatchers.IO).launch {
//            recentMigrate(accountSqlTool.recent)
            val cloudAccounts = arrayListOf<CloudAccount>()
            sqlAccounts.forEach {
                cloudAccounts.add(
                    CloudAccount(
                        id = getId(it.token, it),
                        login = it.login,
                        portal = it.portal,
                        isWebDav = it.isWebDav,
                        webDavProvider = it.webDavProvider,
                        webDavPath = it.webDavPath,
                        scheme = it.scheme,
                        isSslState = it.isSslState,
                        isSslCiphers = it.isSslCiphers,
                        avatarUrl = it.avatarUrl,
                        isOnline = false,
                        provider = it.provider,
                        name = it.name
                    )
                )
                accountSqlTool.delete(it)
            }
            accountDao.addAccounts(cloudAccounts)
        }
    }

    private suspend fun recentMigrate(recents: List<Recent>) {
        recents.filter { it.accountsSqlData == null }.forEach { oldRecent ->
            val newRecent = app.documents.core.account.Recent(
                id =  oldRecent.id.toInt(),
                date = oldRecent.date.time,
                isWebDav = oldRecent.isWebDav,
                isLocal = oldRecent.isLocal,
                name = oldRecent.name,
                idFile = oldRecent.idFile,
                path = oldRecent.path,
                size = oldRecent.size,
                ownerId = null
            )
            recentDao.addRecent(newRecent)
        }
        recents.forEach {
            accountSqlTool.delete(it)
        }
    }

    private fun getId(token: String?, account: AccountsSqlData): String {
        if (!account.isWebDav) {
            networkSettings.setScheme(account.scheme)
            networkSettings.setBaseUrl(account.portal)
            networkSettings.setCipher(account.isSslCiphers)
            networkSettings.setSslState(account.isSslState)

            if (token.isNullOrEmpty()) {
                return getTokenWithId(account)
            }
            val response = App.getApp().loginComponent.loginService.getUserInfo(token = token)
                .blockingGet()
            return if (response is LoginResponse.Success) {
                (response.response as ResponseUser).response.id
            } else {
                account.id
            }
        } else {
            return account.portal + " " + account.login
        }
    }

    private fun getTokenWithId(account: AccountsSqlData): String {
        val requestSignIn = RequestSignIn(
            userName = account.login,
            password = account.password,
            provider = account.provider
        )

        val response = App.getApp().loginComponent.loginService.signIn(requestSignIn)
            .blockingGet()
        if (response is LoginResponse.Success) {
            val responseSignIn = response.response as ResponseSignIn
            responseSignIn.response.token?.let {
                return it
            } ?: run {
                return ""
            }
        } else {
            return ""
        }
    }

}