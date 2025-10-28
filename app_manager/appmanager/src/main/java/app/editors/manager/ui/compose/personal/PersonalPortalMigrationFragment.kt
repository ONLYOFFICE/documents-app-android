package app.editors.manager.ui.compose.personal

import android.content.DialogInterface
import androidx.compose.runtime.Composable
import app.editors.manager.app.App
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme

class PersonalPortalMigrationFragment : ComposeDialogFragment() {

    companion object {

        fun newInstance(): PersonalPortalMigrationFragment = PersonalPortalMigrationFragment()
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            PersonalMigrationScreen(::dismiss)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        App.getApp().showPersonalPortalMigration = false
    }
}