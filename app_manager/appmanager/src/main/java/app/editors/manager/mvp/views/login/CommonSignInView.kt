package app.editors.manager.mvp.views.login

import android.content.Intent
import app.editors.manager.managers.utils.SocialSignIn
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CommonSignInView : BaseView {
    fun onSuccessLogin()
    fun onTwoFactorAuth(phoneNoise: String?, request: String)
    fun onGooglePermission(intent: Intent)
    fun onEmailNameError(message: String)
    fun onTwoFactorAuthTfa(secretKey: String?, request: String)
    fun onWaitingDialog()
    fun onSocialAuth(socialSignIn: SocialSignIn?)
}