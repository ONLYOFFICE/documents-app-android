package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import app.editors.manager.R
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppMultilineArrowItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.contains

class FillFormChooserFragment : BaseDialogFragment() {

    companion object {
        const val KEY_FILL_FORM_CHOOSER_REQUEST = "key_fill_form_chooser_request"
        const val KEY_FILL_FORM_CHOOSER_RESULT = "key_fill_form_chooser_result"
        const val KEY_FILL_FORM_CHOOSER_FROM_YOURSELF = "key_fill_form_chooser_from_yourself"
        const val KEY_FILL_FORM_CHOOSER_IN_ROOM = "key_fill_form_chooser_in_room"

        private fun newInstance(): FillFormChooserFragment = FillFormChooserFragment()

        fun show(activity: FragmentActivity, onFillForm: () -> Unit, onSelectRoom: () -> Unit) {
            activity.supportFragmentManager.setFragmentResultListener(
                KEY_FILL_FORM_CHOOSER_REQUEST,
                activity
            ) { _, bundle ->
                if (bundle.contains(KEY_FILL_FORM_CHOOSER_RESULT)) {
                    when (bundle.getString(KEY_FILL_FORM_CHOOSER_RESULT)) {
                        KEY_FILL_FORM_CHOOSER_FROM_YOURSELF -> onFillForm()
                        KEY_FILL_FORM_CHOOSER_IN_ROOM -> onSelectRoom()
                    }
                }
            }
            newInstance().show(activity.supportFragmentManager, null)
        }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            ManagerTheme {
                FillFormChooserScreen(
                    onBack = ::dismiss,
                    onSelectRoom = {
                        dismiss()
                        setFragmentResult(
                            KEY_FILL_FORM_CHOOSER_REQUEST,
                            bundleOf(KEY_FILL_FORM_CHOOSER_RESULT to KEY_FILL_FORM_CHOOSER_IN_ROOM)
                        )
                    },
                    onFillOut = {
                        dismiss()
                        setFragmentResult(
                            KEY_FILL_FORM_CHOOSER_REQUEST,
                            bundleOf(KEY_FILL_FORM_CHOOSER_RESULT to KEY_FILL_FORM_CHOOSER_FROM_YOURSELF)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FillFormChooserScreen(onBack: () -> Unit, onSelectRoom: () -> Unit, onFillOut: () -> Unit) {
    AppScaffold(
        topBar = {
            AppTopBar(title = R.string.rooms_fill_in_as_title, backListener = onBack)
        },
        useTablePaddings = false
    ) {
        NestedColumn {
            AppMultilineArrowItem(
                title = stringResource(id = R.string.rooms_fill_out_form_yourself),
                description = stringResource(id = R.string.rooms_fill_out_form_yourself_desc),
                onClick = onFillOut
            )
            AppMultilineArrowItem(
                title = stringResource(id = R.string.rooms_share_and_collect),
                description = stringResource(id = R.string.rooms_share_and_collect_desc),
                onClick = onSelectRoom
            )
        }
    }
}

@Preview
@Composable
private fun FillFormChooserPreview() {
    ManagerTheme {
        FillFormChooserScreen({}, {}, {})
    }
}