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
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
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
class MainPagerPresenter(private val accountJson: String?) : BasePresenter<MainPagerView>() {

    @Inject
    lateinit var networkSetting: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    private val api: Api?
        get() {
          AccountUtils.getToken(
                context,
                Account(accountJson?.let { Json.decodeFromString<CloudAccount>(it).getAccountName() }, context.getString(R.string.account_type))
            )?.let {
                return DaggerApiComponent.builder().apiModule(ApiModule(it))
                    .appComponent(App.getApp().appComponent)
                    .build()
                    .getApi()
            } ?: return null
        }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getState() {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                isProjectDisable()
                isFavoriteEnable()
            }
            viewState.onFinishRequest()
            accountJson?.let { jsonAccount ->
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
        }
    }

    private fun isProjectDisable() {
        val response = api?.getModules(listOf(Constants.Modules.PROJECT_ID))?.blockingGet()
        if (response?.response != null) {
            preferenceTool.isProjectDisable = !response.response[0].isEnable
        } else {
            viewState.onError(response?.error?.message)
        }
    }

    private fun isFavoriteEnable() {
        val response = api?.getRootFolder(
            mapOf("filterType" to 2),
            mapOf(
                "withsubfolders" to false,
                "withoutTrash" to true,
                "withoutAdditionalFolder" to false
            )
        )?.blockingGet()
        if (response?.response != null) {
            for (folder in response.response) {
                if (StringUtils.Favorites.contains(folder.current.title)) {
                    preferenceTool.setFavoritesEnable(true)
                    break
                } else {
                    preferenceTool.setFavoritesEnable(false)
                }
            }
        } else {
            viewState.onError(response?.error?.message)
        }
    }
}