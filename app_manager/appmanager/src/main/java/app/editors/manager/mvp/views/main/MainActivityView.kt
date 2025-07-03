package app.editors.manager.mvp.views.main

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import app.documents.core.model.cloud.Access
import app.editors.manager.mvp.views.base.BaseViewExt
import com.google.android.play.core.review.ReviewInfo
import lib.toolkit.base.managers.utils.EditType
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainActivityView : BaseViewExt {
    fun onDialogClose()
    fun onRemotePlayMarket(@StringRes title: Int, @StringRes info: Int, @StringRes accept: Int, @StringRes cancel: Int)
    fun onRemoteApp(@StringRes title: Int, @StringRes info: Int, @StringRes accept: Int, @StringRes cancel: Int)
    fun onRatingApp()
    fun onQuestionDialog(title: String, tag: String, accept: String, cancel: String, question: String?)
    fun onShowEditMultilineDialog(title: String, hint: String, accept: String, cancel: String, tag: String)
    fun onShowPlayMarket(releaseId: String)
    fun onShowInAppReview(reviewInfo: ReviewInfo)
    fun onShowApp(releaseId: String)
    fun onShowEmailClientTemplate(value: String)
    fun onShowOnBoarding()
    fun onCloseActionDialog()
    fun onLocaleConfirmation()
    fun signInAndOpenDeeplink(portal: String, email: String, uri: Uri)
    fun restartActivity(deeplink: Uri? = null)
    fun showEditors(
        uri: Uri,
        editType: EditType,
        access: Access,
        onResultListener: ((Int, Intent?) -> Unit)?
    )
    fun showEditors(
        data: String,
        extension: String,
        editType: EditType,
        access: Access,
        onResultListener: ((Int, Intent?) -> Unit)?
    )
}