package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.os.Build
import app.documents.core.account.AccountPreferences
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
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
import lib.toolkit.base.managers.utils.FileUtils
import moxy.InjectViewState
import moxy.presenterScope
import javax.inject.Inject

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

    @Inject
    lateinit var accountPreferences: AccountPreferences

    private val disposable = CompositeDisposable()

    private var cloudAccount: CloudAccount? = null
    private var reviewInfo: ReviewInfo? = null
    private var isAppColdStart = true

    var isDialogOpen: Boolean = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        preferenceTool.setUserSession()
        preferenceTool.filter = Filter()
        checkSdk()
    }

    private fun checkSdk() {
        presenterScope.launch(Dispatchers.IO) {
            if (preferenceTool.appVersion != BuildConfig.VERSION_NAME) {
                context.externalCacheDir?.let(FileUtils::deletePath)
                context.cacheDir?.let(FileUtils::deletePath)
            }
            preferenceTool.appVersion = BuildConfig.VERSION_NAME
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun init(isPortal: Boolean = false) {
        presenterScope.launch {
            context.accountOnline?.let { account ->
                if (isAppColdStart) {
                    App.getApp().refreshLoginComponent(account.portal)
                    App.getApp().loginComponent.cloudLoginRepository.updatePortalSettings()
                    isAppColdStart = false
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
            context.accountOnline?.let {
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

    fun clear() {
        accountPreferences.onlineAccountId = null
    }

    fun checkFileData(fileData: Uri) {
        presenterScope.launch {
            val data: OpenDataModel = if (preferenceTool.fileData.isNotEmpty()) {
                Json.decodeFromString(preferenceTool.fileData)
            } else {
                Json.decodeFromString(CryptUtils.decodeUri(fileData.query))
            }

            context.accountOnline?.let { account ->
                if (fileData.queryParameterNames.contains("push")) {
                    viewState.openFile(account, fileData.getQueryParameter("data") ?: "")
                    return@launch
                }

                preferenceTool.fileData = ""
                if (data.getPortalWithoutScheme()?.equals(
                        account.portal.url,
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
        val account = cloudDataSource.getAccountByLogin(data.email?.lowercase() ?: "")
        val token = AccountUtils.getToken(context, account?.accountName.orEmpty())
        return !token.isNullOrEmpty()
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