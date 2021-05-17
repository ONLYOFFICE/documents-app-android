package app.editors.manager.mvp.presenters.main

import app.documents.core.account.AccountDao
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.app.App
import app.editors.manager.mvp.views.main.MainPagerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
class MainPagerPresenter : MvpPresenter<MainPagerView>() {

    @Inject
    lateinit var networkSetting: NetworkSettings

    @Inject
    lateinit var accountsDao: AccountDao

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getState() {
        CoroutineScope(Dispatchers.Default).launch {
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

}