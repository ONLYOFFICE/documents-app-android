package app.editors.manager.ui.views.custom

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import app.editors.manager.BuildConfig
import app.editors.manager.databinding.IncludeSocialNetworksLayoutBinding
import com.facebook.*
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class SocialViews(private val activity: Activity, view: View?,
                  private val facebookId: String?) {

    interface OnSocialNetworkCallbacks {
        fun onTwitterSuccess(token: String)
        fun onTwitterFailed()
        fun onFacebookSuccess(token: String)
        fun onFacebookLogin(message: String)
        fun onFacebookCancel()
        fun onFacebookFailed()
        fun onGoogleSuccess(account: Account)
        fun onGoogleFailed()
        fun onGoogleCancelled()
    }

    private var onSocialNetworkCallbacks: OnSocialNetworkCallbacks? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var facebookCallbackManager: CallbackManager? = null
    private var loginPersonalSocialFacebookNativeButton: LoginButton? = null
    private var viewBinding: IncludeSocialNetworksLayoutBinding? = null

    init {
        viewBinding = view?.let { IncludeSocialNetworksLayoutBinding.bind(it) }
        initListeners()
        initFacebook()
    }

    fun showGoogleLogin(isShow: Boolean) {
        viewBinding?.loginSocialGoogleButton?.isVisible = isShow
    }

    fun showFacebookLogin(isShow: Boolean) {
        viewBinding?.loginSocialFacebookButton?.isVisible = isShow
    }

    /*
    * Facebook initWithPreferences
    * */
    private fun initFacebook() {
        Log.d(TAG, "initFacebook() - app ID: " + BuildConfig.FACEBOOK_APP_ID)
        if (FacebookSdk.isInitialized()) {
            facebookId?.let { FacebookSdk.setApplicationId(it) }
            loginPersonalSocialFacebookNativeButton = LoginButton(activity)
            loginPersonalSocialFacebookNativeButton?.loginBehavior = LoginBehavior.WEB_ONLY
            facebookCallbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(facebookCallbackManager, FacebookAuthCallback())
        } else {
            viewBinding?.loginSocialFacebookButton?.isVisible = false
        }

    }

    private fun initListeners() {
        viewBinding?.let {
            it.loginSocialFacebookButton.setOnClickListener { onFacebookClick() }
            it.loginSocialGoogleButton.setOnClickListener { onGoogleClick() }
        }
    }

    /*
    * Actions
    * */
    private fun onGoogleClick() {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_ID)
                .requestProfile()
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso).apply {
            signOut()
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun getGoogleToken(completedTask: Task<GoogleSignInAccount>) {
        onSocialNetworkCallbacks?.let { callbacks ->
            try {
                val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
                account.account?.let { callbacks.onGoogleSuccess(it) }
            } catch (e: ApiException) {
                Log.e(TAG, "Status code: " + e.statusCode, e)
                googleSignInClient?.signOut()
                if (e.statusCode == SIGN_IN_CANCELLED) {
                    callbacks.onGoogleCancelled()
                } else {
                    callbacks.onGoogleFailed()
                }
            } catch (e: Exception) {
                googleSignInClient?.signOut()
                callbacks.onGoogleFailed()
            }
        }
    }

    /*
    * Facebook click. Get previous token or get new with button click.
    * */
    private fun onFacebookClick() {
        val accessToken: AccessToken? = AccessToken.getCurrentAccessToken()
        onSocialNetworkCallbacks?.let { callbacks ->
            accessToken?.let { token ->
                callbacks.onFacebookLogin(token.userId)
            } ?: run {
                loginPersonalSocialFacebookNativeButton?.performClick()
            }
        }
    }

    fun onFacebookContinue() {
        onSocialNetworkCallbacks?.onFacebookSuccess(AccessToken.getCurrentAccessToken()?.token ?: "")
    }

    fun onFacebookLogout() {
        LoginManager.getInstance().logOut()
        loginPersonalSocialFacebookNativeButton?.performClick()
    }

    /*
    * Lifecycle methods
    * */
    fun onDestroyView() {
        if (FacebookSdk.isInitialized()) {
            LoginManager.getInstance().unregisterCallback(facebookCallbackManager)
        }
        setOnSocialNetworkCallbacks(null)
        viewBinding = null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            getGoogleToken(GoogleSignIn.getSignedInAccountFromIntent(data))
        } else {
            facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    /*
    * Getters/Setters
    * */
    fun setOnSocialNetworkCallbacks(onSocialNetworkCallbacks: OnSocialNetworkCallbacks?) {
        this.onSocialNetworkCallbacks = onSocialNetworkCallbacks
    }

    /*
    * Facebook callback
    * */
    private inner class FacebookAuthCallback : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            onSocialNetworkCallbacks?.onFacebookSuccess(result.accessToken.token)
        }

        override fun onCancel() {
            onSocialNetworkCallbacks?.onFacebookCancel()
        }

        override fun onError(error: FacebookException) {
            LoginManager.getInstance().logOut()
            onSocialNetworkCallbacks?.onFacebookFailed()
        }
    }

    companion object {
        val TAG = SocialViews::class.java.simpleName
        const val GOOGLE_PERMISSION = 1212
        private const val RC_SIGN_IN = 9001
        private const val SIGN_IN_CANCELLED = 12501
    }

}