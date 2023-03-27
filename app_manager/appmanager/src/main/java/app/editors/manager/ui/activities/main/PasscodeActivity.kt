package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.activities.main.PasscodeActivity
import lib.compose.ui.theme.ManagerTheme
import javax.inject.Inject


class PasscodeActivity : BaseAppActivity() {

    companion object {
        val TAG: String = PasscodeActivity::class.java.simpleName
        const val ENTER_PASSCODE_KEY = "ENTER_PASSCODE_KEY"


        fun show(context: Context, isEnterPasscode: Boolean = false, bundle: Bundle? = null) {
            context.startActivity(Intent(context, PasscodeActivity::class.java).apply {
                putExtra(ENTER_PASSCODE_KEY, isEnterPasscode)
                bundle?.let { putExtras(it) }
            })
        }
    }

    @Inject
    lateinit var preferencesTool: PreferenceTool

    var isEnterPasscode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.getApp().appComponent.inject(this)

        isEnterPasscode = intent.getBooleanExtra(ENTER_PASSCODE_KEY, false)
        onBackPressedDispatcher.addCallback { finish() }
        setContent {
            ManagerTheme {
                PasscodeActivity(
                    preferenceTool = preferencesTool,
                    data = intent.extras,
                    backPressed = ::finish
                )
            }
        }
    }

    fun onBiometricError(errorMessage: String) {
        showSnackBar(errorMessage)
    }
}