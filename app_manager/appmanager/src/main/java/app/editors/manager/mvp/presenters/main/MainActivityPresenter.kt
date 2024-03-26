package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.os.Build
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.account.copyWithToken
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainActivityView
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.CryptUtils
import moxy.InjectViewState
import moxy.presenterScope

sealed class MainActivityState {
    object RecentState : MainActivityState()
    object OnDeviceState : MainActivityState()
    object SettingsState : MainActivityState()
    class CloudState(val account: CloudAccount? = null) : MainActivityState()
}

@InjectViewState
class MainActivityPresenter : BasePresenter<MainActivityView>() {

    companion object {
        val TAG: String = MainActivityPresenter::class.java.simpleName
        const val TAG_DIALOG_REMOTE_PLAY_MARKET = "TAG_DIALOG_REMOTE_PLAY_MARKET"
        const val TAG_DIALOG_REMOTE_APP = "TAG_DIALOG_REMOTE_APP"
        const val TAG_DIALOG_RATE_FIRST = "TAG_DIALOG_RATE_FIRST"
        private const val TAG_DIALOG_RATE_SECOND = "TAG_DIALOG_RATE_SECOND"
        private const val TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK"
        private const val DEFAULT_RATE_SESSIONS: Long = 5
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private val disposable = CompositeDisposable()

    private var cloudAccount: CloudAccount? = null
    private var reviewInfo: ReviewInfo? = null
    private var isAppColdStart = true

    var isDialogOpen: Boolean = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        preferenceTool.setUserSession()
        preferenceTool.filter = Filter()
        if (isAppColdStart) {
            isAppColdStart = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun init(isPortal: Boolean = false, isShortcut: Boolean = false) {
        presenterScope.launch {
            accountDao.getAccountOnline()?.let {
                cloudAccount = it
                setNetworkSetting(it)
                if (isShortcut) {
                    viewState.onRender(MainActivityState.OnDeviceState)
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    checkToken(it)
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    if (isPortal && !isShortcut) {
                        viewState.onRender(MainActivityState.CloudState())
                    } else {
                        viewState.onRender(MainActivityState.OnDeviceState)
                    }
                }
            }
        }
        checkAppLocale()
    }

    private fun checkAppLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.appComponent.appLocaleHelper.checkAppLocale(true)) {
                viewState.onLocaleConfirmation()
            }
        }
    }

    private fun checkToken(cloudAccount: CloudAccount) {
        if (cloudAccount.isWebDav) {
            viewState.onRender(MainActivityState.CloudState(cloudAccount))
        } else {
            AccountUtils.getToken(
                context = context,
                accountName = cloudAccount.getAccountName()
            )?.let {
                viewState.onRender(MainActivityState.CloudState(cloudAccount))
            } ?: run {
                viewState.onRender(MainActivityState.CloudState())
            }
        }
    }

    private fun setNetworkSetting(cloudAccount: CloudAccount) {
        networkSettings.setBaseUrl(cloudAccount.portal ?: ApiContract.DEFAULT_HOST)
        networkSettings.setScheme(cloudAccount.scheme ?: ApiContract.SCHEME_HTTPS)
        networkSettings.setSslState(cloudAccount.isSslState)
        networkSettings.setCipher(cloudAccount.isSslCiphers)
        networkSettings.serverVersion = cloudAccount.serverVersion
    }

    fun getRemoteConfigRate() {
        if (!BuildConfig.DEBUG) {
            if (preferenceTool.isRateOn && preferenceTool.userSession % DEFAULT_RATE_SESSIONS == 0L) {
                viewState.onRatingApp()
            }
        }
    }

    private fun getReviewInfo() {
        ReviewManagerFactory.create(context)
            .requestReviewFlow()
            .addOnCompleteListener { task: Task<ReviewInfo?> ->
                if (task.isSuccessful) {
                    reviewInfo = task.result
                }
            }
    }

    fun setAccount() {
        presenterScope.launch {
            accountDao.getAccountOnline()?.let {
                cloudAccount = it
            }
        }
    }

    fun onAcceptClick(value: String?, tag: String?) {
        tag?.let {
            when (tag) {
                TAG_DIALOG_REMOTE_PLAY_MARKET -> {
                    viewState.onShowPlayMarket(BuildConfig.RELEASE_ID)
                    viewState.onDialogClose()
                }
                TAG_DIALOG_REMOTE_APP -> {
                    viewState.onShowApp(BuildConfig.RELEASE_ID)
                    viewState.onDialogClose()
                }
                TAG_DIALOG_RATE_FIRST -> {
                    getReviewInfo()
                    viewState.onQuestionDialog(
                        context.getString(R.string.dialogs_question_rate_second_info), TAG_DIALOG_RATE_SECOND,
                        context.getString(R.string.dialogs_question_accept_sure),
                        context.getString(R.string.dialogs_question_accept_no_thanks), null
                    )
                }
                TAG_DIALOG_RATE_SECOND -> {
                    viewState.onDialogClose()
                    reviewInfo?.let {
                        viewState.onShowInAppReview(it)
                    } ?: run {
                        viewState.onShowPlayMarket(BuildConfig.RELEASE_ID)
                    }
                }
                TAG_DIALOG_RATE_FEEDBACK -> {
                    if (value != null) {
                        viewState.onShowEmailClientTemplate(value)
                    }
                    viewState.onDialogClose()
                }
            }
        }
    }

    fun onCancelClick(tag: String?) {
        tag?.let {
            when (tag) {
                TAG_DIALOG_RATE_FIRST -> {
                    preferenceTool.isRateOn = false
                    viewState.onDialogClose()
                    viewState.onShowEditMultilineDialog(
                        context.getString(R.string.dialogs_edit_feedback_rate_title),
                        context.getString(R.string.dialogs_edit_feedback_rate_hint),
                        context.getString(R.string.dialogs_edit_feedback_rate_accept),
                        context.getString(R.string.dialogs_question_accept_no_thanks), TAG_DIALOG_RATE_FEEDBACK
                    )
                }
                TAG_DIALOG_RATE_SECOND -> {
                    preferenceTool.isRateOn = false
                    viewState.onDialogClose()
                }
            }
        }
    }

    fun navigationItemClick(itemId: Int) {
        when (itemId) {
            R.id.menu_item_recent -> viewState.onRender(MainActivityState.RecentState)
            R.id.menu_item_on_device -> viewState.onRender(MainActivityState.OnDeviceState)
            R.id.menu_item_settings -> viewState.onRender(MainActivityState.SettingsState)
            R.id.menu_item_cloud -> {
                presenterScope.launch {
                    cloudAccount = accountDao.getAccountOnline()
                    withContext(Dispatchers.Main) {
                        viewState.onRender(MainActivityState.CloudState(cloudAccount))
                    }
                }
            }
        }
    }

    fun clear() {
        presenterScope.launch {
            accountDao.getAccountOnline()?.let {
                accountDao.updateAccount(
                    it.copyWithToken(
                        isOnline = false
                    )
                )
            }
            networkSettings.setDefault()
            withContext(Dispatchers.Main) {
                viewState.onRender(MainActivityState.CloudState())
            }
        }
    }

    fun checkFileData(fileData: Uri) {
        presenterScope.launch {
            val data: OpenDataModel = if (preferenceTool.fileData.isNotEmpty()) {
                Json.decodeFromString(preferenceTool.fileData)
            } else {
                Json.decodeFromString(CryptUtils.decodeUri(fileData.query))
            }

            accountDao.getAccountOnline()?.let { account ->
                if (fileData.queryParameterNames.contains("push")) {
                    viewState.openFile(account, fileData.getQueryParameter("data") ?: "")
                    return@launch
                }

                preferenceTool.fileData = ""
                if (data.getPortalWithoutScheme()?.equals(
                        account.portal,
                        ignoreCase = true
                    ) == true &&
                    data.email?.equals(account.login, ignoreCase = true) == true
                ) {
                    preferenceTool.fileData = Json.encodeToString(data)
                    withContext(Dispatchers.Main) {
                        viewState.openFile(account, Json.encodeToString(data))
                    }
                } else {
                    val isToken = checkAccountLogin(data)
                    preferenceTool.fileData = Json.encodeToString(data)
                    withContext(Dispatchers.Main) {
                        viewState.onSwitchAccount(data, isToken)
                    }
                }

            } ?: run {
                withContext(Dispatchers.Main) {
                    viewState.onSwitchAccount(data, false)
                }
            }
        }
    }

    private suspend fun checkAccountLogin(data: OpenDataModel): Boolean {
        val account = accountDao.getAccountByLogin(data.email?.lowercase() ?: "")
        return account?.token != null && account.token.isNotEmpty()
    }

    fun onRemoveFileData() {
        preferenceTool.fileData = ""
    }

    fun onRateOff() {
        preferenceTool.isRateOn = false
    }

    fun checkOnBoardingShowed() {
        if (!preferenceTool.onBoarding) {
            viewState.onShowOnBoarding()
        }
    }
}