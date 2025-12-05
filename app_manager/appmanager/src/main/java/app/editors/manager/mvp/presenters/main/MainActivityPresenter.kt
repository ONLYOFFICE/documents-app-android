package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import app.documents.core.account.AccountPreferences
import app.documents.core.account.AccountRepository
import app.documents.core.database.datasource.RecentDataSource
import app.documents.core.login.CheckLoginResult
import app.documents.core.login.PortalResult
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.Recent
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.NetworkResult
import app.documents.core.providers.FileOpenResult
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.appComponent
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainActivityView
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var recentDataSource: RecentDataSource

    private val disposable = CompositeDisposable()

    private var reviewInfo: ReviewInfo? = null
    var isAppColdStart = true
        private set

    var isDialogOpen: Boolean = false
    val isVPNChecked: Boolean
        get() = preferenceTool.isVpnChecked

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        preferenceTool.setUserSession()
        preferenceTool.filter = Filter()
        checkSdk()
    }

    private fun checkSdk() {
        presenterScope.launch(Dispatchers.IO) {
            val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }

            if (preferenceTool.appVersion != BuildConfig.VERSION_NAME + "." + version) {
                context.externalCacheDir?.let(FileUtils::deletePath)
                context.cacheDir?.let(FileUtils::deletePath)
            }
            preferenceTool.appVersion = BuildConfig.VERSION_NAME + "." + version
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
                    if (account.isDocSpace) {
                        val isRegular = App.getApp().loginComponent.cloudLoginRepository.checkUserRegular()
                        accountPreferences.isRegularUser = isRegular
                    } else {
                        accountPreferences.isRegularUser = false
                    }
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

    fun onRateOff() {
        preferenceTool.isRateOn = false
    }

    fun checkOnBoardingShowed() {
        if (!preferenceTool.onBoarding) {
            viewState.onShowOnBoarding()
        }
    }

    fun openDeeplink(uri: Uri) {
        val data = Json.decodeFromString<OpenDataModel>(CryptUtils.decodeUri(uri.query))
        val hasToken = data.share.isNotEmpty()
        val account = context.accountOnline
        val isAccountOnline = account?.portal?.urlWithScheme == data.portal &&
                account?.login == data.email

        presenterScope.launch(Dispatchers.Default) {
            if (isAccountOnline || hasToken) {
                openFile(data)
                return@launch
            }

            when (val result = accountRepository.checkLoginWithEmailAndPortal(email = data.email.orEmpty(), portalUrl = data.portal)) {
                is CheckLoginResult.Success -> {
                    viewState.restartActivity(deeplink = uri)
                }
                is CheckLoginResult.NeedLogin -> {
                    signInAndOpenDeeplink(data, uri)
                }
                is CheckLoginResult.Error -> fetchError(result.exception)
                else -> Unit
            }
        }
    }

    private suspend fun signInAndOpenDeeplink(data: OpenDataModel, uri: Uri) {
        val portalUri = data.portal?.toUri() ?: return
        val portalScheme = portalUri.scheme?.let { Scheme.valueOf("$it://") }
            ?: data.originalUrl?.toUri()?.scheme?.let { Scheme.valueOf("$it://") }
            ?: Scheme.Https

        App.getApp().refreshLoginComponent(
            CloudPortal(url = portalUri.host.takeIf { it.isNullOrEmpty() } ?: portalUri.toString(), scheme = portalScheme)
        )

        App.getApp().loginComponent.cloudLoginRepository
            .checkPortal(portalUri.host.orEmpty(), portalScheme)
            .collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is PortalResult.Error -> {
                            viewState.onError(context.getString(R.string.errors_unknown_error))
                        }

                        is PortalResult.Success -> {
                            App.getApp().refreshLoginComponent(result.cloudPortal)
                            viewState.signInAndOpenDeeplink(
                                portal = data.portal,
                                email = data.email.orEmpty(),
                                uri = uri
                            )
                        }

                        else -> Unit
                    }
                }
            }
    }

    private suspend fun openFile(data: OpenDataModel) {
        presenterScope.launch {
//            if (data.folder != null) return@launch

            recentDataSource.insertOrUpdate(Recent(
                fileId = data.file?.id.orEmpty(),
                name = data.file?.title.orEmpty(),
                source = data.portal,
                token = data.share
            ))
        }

        context.cloudFileProvider
            .openDeeplink(
                portal = data.portal.orEmpty(),
                token = data.share,
                login = data.email.orEmpty(),
                id = data.file?.id.orEmpty(),
                extension = data.file?.extension.orEmpty(),
                title = data.file?.title.orEmpty()
            )
            .collect { result ->
                when (result) {
                    is NetworkResult.Error -> fetchError(result.exception)
                    is NetworkResult.Success<FileOpenResult> -> {
                        handleFileOpenResult(result.data)
                    }
                    else -> Unit
                }
            }
    }

    private fun handleFileOpenResult(result: FileOpenResult) {
        when (result) {
            is FileOpenResult.OpenDocumentServer -> with(result) {
                viewState.onDialogClose()
                viewState.showEditors(
                    data = info,
                    extension = cloudFile.fileExst,
                    access = cloudFile.access,
                    editType = editType,
                    onResultListener = null
                )
            }

            is FileOpenResult.OpenLocally -> with(result) {
                viewState.onDialogClose()
                viewState.showEditors(
                    uri = file.toUri(),
                    extension = file.extension,
                    editType = editType,
                    access = access,
                    onResultListener = null
                )
            }

            else -> Unit
        }
    }

    fun setCheckVPN(checked: Boolean) {
        preferenceTool.isVpnChecked = checked
    }

}