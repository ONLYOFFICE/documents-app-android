package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import app.editors.manager.app.App
import app.editors.manager.compose.ui.theme.AppManagerTheme
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.activities.main.PasscodeActivity
import javax.inject.Inject


class PasscodeActivity : BaseAppActivity() {

    companion object {
        val TAG: String = PasscodeActivity::class.java.simpleName
        const val ENTER_PASSCODE_KEY = "ENTER_PASSCODE_KEY"


        fun show(context: Context, isEnterPasscode: Boolean = false) {
            context.startActivity(Intent(context, PasscodeActivity::class.java).apply {
                putExtra(ENTER_PASSCODE_KEY, isEnterPasscode)
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

        setContent {
            AppManagerTheme() {
                PasscodeActivity(preferencesTool) {
                    onBackPressed()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun onBiometricError(errorMessage: String) {
        showSnackBar(errorMessage)
    }
}