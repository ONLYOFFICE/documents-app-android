package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import app.documents.core.account.AccountDao
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.di.component.DaggerApiComponent
import app.editors.manager.di.module.ApiModule
import app.editors.manager.managers.utils.Constants
import app.editors.manager.mvp.models.response.ResponseModules
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import javax.inject.Inject

sealed class MainPagerState {
    class VisitorState(val account: String, val version: Int) : MainPagerState()
    class PersonalState(val account: String, val version: Int) : MainPagerState()
    class CloudState(val account: String, val version: Int) : MainPagerState()
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

    private var disposable: Disposable? = null

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

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getState(account: String?) {
        disposable = isFavoriteEnable()?.subscribe({ response ->
            if (response.response != null) {
                preferenceTool.isProjectDisable = !response.response.get(0).isEnable
            } else {
                viewState.onError(response?.error?.message)
            }
            account?.let { jsonAccount ->
                Json.decodeFromString<CloudAccount>(jsonAccount).let { cloudAccount ->
                    when {
                        networkSetting.getPortal().contains(ApiContract.PERSONAL_HOST) -> {
                            viewState.onRender(
                                MainPagerState.PersonalState(
                                    jsonAccount,
                                    StringUtils.convertServerVersion(
                                        networkSetting.serverVersion
                                    )
                                )
                            )
                        }
                        cloudAccount.isVisitor -> {
                            viewState.onRender(
                                MainPagerState.VisitorState(
                                    jsonAccount,
                                    StringUtils.convertServerVersion(
                                        networkSetting.serverVersion
                                    )
                                )
                            )
                        }
                        else -> {
                            viewState.onRender(
                                MainPagerState.CloudState(
                                    jsonAccount,
                                    StringUtils.convertServerVersion(networkSetting.serverVersion)
                                )
                            )
                        }
                    }
                }
            } ?: run {
                throw Exception("Need account")
            }
        }) { throwable: Throwable -> fetchError(throwable) }
    }

    private fun isProjectDisable(): Observable<ResponseModules> {
        return api.getModules(listOf(Constants.Modules.PROJECT_ID))
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun isFavoriteEnable(): Observable<ResponseModules>? {
        return api.getRootFolder(mapOf("filterType" to 2), mapOf("withsubfolders" to false, "withoutTrash" to true, "withoutAdditionalFolder" to false))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap{response ->
                if(response.response != null) {
                    for (folder in response.response) {
                        if(StringUtils.Favorites.contains(folder.current.title)) {
                            preferenceTool.setFavoritesEnable(true)
                            break
                        } else {
                            preferenceTool.setFavoritesEnable(false)
                        }
                    }
                    return@flatMap isProjectDisable()
                } else {
                    viewState.onError(response.error?.message)
                    return@flatMap null
                }
            }
    }
}