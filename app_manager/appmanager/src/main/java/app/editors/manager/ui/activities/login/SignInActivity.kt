package app.editors.manager.ui.activities.login

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.*
import lib.toolkit.base.managers.utils.AccountUtils

class SignInActivity : BaseAppActivity() {

    companion object {
        val TAG: String = SignInActivity::class.java.simpleName
        private const val REQUEST_SIGN_IN = 100

        const val TAG_ACTION = "TAG_ACTION"
        const val TAG_PORTAL_SIGN_IN = "TAG_PORTAL_SIGN_IN"
        const val TAG_PORTAL_SIGN_IN_EMAIL = "TAG_PORTAL_SIGN_IN_EMAIL"
        const val TAG_PORTAL_CREATE = "TAG_PORTAL_CREATE"
        const val TAG_PERSONAL_SIGN_UP = "TAG_PERSONAL_SIGN_UP"
        const val TAG_PASSWORD_RECOVERY = "TAG_PASSWORD_RECOVERY"
        const val KEY_EMAIL_RECOVERY = "KEY_EMAIL_RECOVERY"
        const val TAG_SMS = "TAG_SMS"
        const val TAG_REQUEST = "TAG_REQUEST"
        const val TAG_PHONE = "TAG_PHONE"
        const val KEY_PORTAL = "KEY_PORTAL"
        const val KEY_LOGIN = "KEY_LOGIN"
        const val KEY_PROVIDERS = "KEY_PROVIDERS"

        @JvmStatic
        fun showPortalSignIn(context: Context, portal: String?, login: String?, providers: Array<String>?) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(KEY_PORTAL, portal)
            intent.putExtra(KEY_LOGIN, login)
            intent.putExtra(KEY_PROVIDERS, providers)
            intent.putExtra(TAG_ACTION, TAG_PORTAL_SIGN_IN)
            context.startActivity(intent)
        }

        @JvmStatic
        fun showPortalSignIn(fragment: Fragment, portal: String?, login: String?) {
            val intent = Intent(fragment.context, SignInActivity::class.java)
            intent.putExtra(KEY_PORTAL, portal)
            intent.putExtra(KEY_LOGIN, login)
            intent.putExtra(TAG_ACTION, TAG_PORTAL_SIGN_IN)
            fragment.startActivityForResult(intent, REQUEST_SIGN_IN)
        }

        @JvmStatic
        fun showPortalSignInEmail(context: Context) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_PORTAL_SIGN_IN)
            intent.putExtra(TAG_PORTAL_SIGN_IN_EMAIL, TAG_PORTAL_SIGN_IN_EMAIL)
            context.startActivity(intent)
        }

        @JvmStatic
        fun showPortalCreate(context: Context) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_PORTAL_CREATE)
            context.startActivity(intent)
        }

        @JvmStatic
        fun showPersonalSignUp(context: Context) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_PERSONAL_SIGN_UP)
            context.startActivity(intent)
        }

        @JvmStatic
        fun showSms(context: Context, request: String) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_SMS)
            intent.putExtra(TAG_REQUEST, request)
            context.startActivity(intent)
        }

        @JvmStatic
        fun showPhone(context: Context, request: String) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_PHONE)
            intent.putExtra(TAG_REQUEST, request)
            context.startActivity(intent)
        }

        @JvmStatic
        fun getAddAccountIntent(
            context: Context,
            accountType: String?,
            authType: String?,
            response: AccountAuthenticatorResponse?
        ): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_PORTAL_SIGN_IN)
            intent.putExtra(AccountUtils.KEY_ACCOUNT_TYPE, accountType)
            intent.putExtra(AccountUtils.KEY_AUTH_TYPE, authType)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            return intent
        }
        @JvmStatic
        fun showPasswordRecovery(context: Context, email: String) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(TAG_ACTION, TAG_PASSWORD_RECOVERY)
            intent.putExtra(KEY_EMAIL_RECOVERY, email)
            context.startActivity(intent)
        }
    }

    private var mAppBarToolbar: Toolbar? = null
    private var mSocialFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        mAppBarToolbar = findViewById(R.id.app_bar_toolbar)
        init(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mSocialFragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(mAppBarToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        getAction(savedInstanceState)
    }

    private fun getAction(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val intent = intent
            val action = intent.getStringExtra(TAG_ACTION)
            if (action != null) {
                when (action) {
                    TAG_PORTAL_SIGN_IN -> {
                        val portal = intent.getStringExtra(KEY_PORTAL)
                        val login = intent.getStringExtra(KEY_LOGIN)
                        val providers = intent.getStringArrayExtra(KEY_PROVIDERS)
                        showFragment(EnterpriseSignInFragment.newInstance(portal, login, providers), null)
                    }
                    TAG_PORTAL_CREATE -> showFragment(EnterpriseCreatePortalFragment.newInstance(), null)
                    TAG_PERSONAL_SIGN_UP -> showFragment(PersonalSignUpFragment.newInstance(), null)
                    TAG_SMS -> showFragment(EnterpriseSmsFragment.newInstance(false, intent.getStringExtra(TAG_REQUEST)), null)
                    TAG_PHONE -> showFragment(EnterprisePhoneFragment.newInstance(intent.getStringExtra(TAG_REQUEST)), null)
                    TAG_PASSWORD_RECOVERY -> showFragment(PasswordRecoveryFragment.newInstance(intent.getStringExtra(
                        KEY_EMAIL_RECOVERY), true), null)
                }
            }
        }
    }

    fun setOnActivityResult(fragment: Fragment?) {
        mSocialFragment = fragment
    }

}