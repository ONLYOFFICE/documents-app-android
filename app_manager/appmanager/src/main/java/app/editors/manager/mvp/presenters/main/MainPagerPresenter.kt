package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.content.Context
import android.util.Log
import app.documents.core.account.AccountDao
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.di.component.DaggerApiComponent
import app.editors.manager.di.module.ApiModule
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.Constants
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import kotlinx.coroutines.*
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

sealed class MainPagerState {
    class VisitorState(val version: Int) : MainPagerState()
    class PersonalState(val version: Int) : MainPagerState()
    class CloudState(val version: Int) : MainPagerState()
}

@InjectViewState
class MainPagerPresenter : BasePresenter<MainPagerView>() {

    @Inject
    lateinit var networkSetting: NetworkSettings

    @Inject
    lateinit var accountsDao: AccountDao

    init {
        App.getApp().appComponent.inject(this)
    }

    private val api: Api = runBlocking(Dispatchers.Default) {
        accountsDao.getAccountOnline()?.let { account ->
            AccountUtils.getToken(
                context,
                Account(account.getAccountName(), context.getString(R.string.account_type))
            )
                ?.let {
                    return@runBlocking DaggerApiComponent.builder().apiModule(ApiModule(it))
                        .appComponent(App.getApp().appComponent)
                        .build()
                        .getApi()
                }
        } ?: run {
            throw Error("No account")
        }
    }

    fun getState() {
        CoroutineScope(Dispatchers.Default).launch {
            launch {
                isProjectDisable()
            }
            isFavoriteEnable()
            accountsDao.getAccountOnline()?.let {
                when {
                    networkSetting.getPortal().contains(ApiContract.PERSONAL_HOST) -> {
                        withContext(Dispatchers.Main) {
                            viewState.onRender(
                                MainPagerState.PersonalState(
                                    StringUtils.convertServerVersion(
                                        networkSetting.serverVersion
                                    )
                                )
                            )
                        }
                    }
                    it.isVisitor -> {
                        withContext(Dispatchers.Main) {
                            viewState.onRender(
                                MainPagerState.VisitorState(
                                    StringUtils.convertServerVersion(
                                        networkSetting.serverVersion
                                    )
                                )
                            )
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            viewState.onRender(MainPagerState.CloudState(StringUtils.convertServerVersion(networkSetting.serverVersion)))
                        }
                    }
                }
            }
        }
    }

    private fun isProjectDisable() {
        api.getModules(listOf(Constants.Modules.PROJECT_ID)).toObservable().subscribe({ response ->
            if(response.response != null) {
                preferenceTool.isProjectDisable = !response.response.get(0).isEnable
            } else {
                viewState.onError(response?.error?.message)
            }
        }) {throwable: Throwable -> fetchError(throwable)}
    }

    private fun isFavoriteEnable() {
        api.getRootFolder(mapOf("filterType" to 2), mapOf("withsubfolders" to false, "withoutTrash" to true, "withoutAdditionalFolder" to false))
            .subscribe({response ->
                if(response.response != null) {
                    for (folder in response.response) {
                        if(StringUtils.Favorites.contains(folder.current.title)) {
                            preferenceTool.setFavoritesEnable(true)
                            break
                        } else {
                            preferenceTool.setFavoritesEnable(false)
                        }
                    }
                } else {
                    viewState.onError(response?.error?.message)
                }
            }) {throwable: Throwable -> fetchError(throwable)}
    }

}