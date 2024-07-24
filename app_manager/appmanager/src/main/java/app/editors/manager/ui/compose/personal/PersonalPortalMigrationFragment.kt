package app.editors.manager.ui.compose.personal

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.UiUtils

class PersonalPortalMigrationFragment : BaseDialogFragment() {

    companion object {

        fun newInstance(): PersonalPortalMigrationFragment = PersonalPortalMigrationFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UiUtils.isTablet(requireContext())) {
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialog
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            ManagerTheme {
                PersonalMigrationScreen(::dismiss)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        App.getApp().showPersonalPortalMigration = false
    }
}
