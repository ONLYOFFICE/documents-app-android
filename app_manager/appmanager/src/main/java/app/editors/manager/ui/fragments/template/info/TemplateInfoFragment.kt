package app.editors.manager.ui.fragments.template.info

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.fragments.template.settings.AccessSettingsDestination
import app.editors.manager.viewModels.main.TemplateInfoViewModel
import app.editors.manager.viewModels.main.TemplateSettingsMode
import kotlinx.serialization.Serializable
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.toolkit.base.managers.utils.putArgs

class TemplateInfoFragment : ComposeDialogFragment() {

    private val templateId: String
        get() = arguments?.getString(KEY_TEMPLATE_ID).orEmpty()

    @Composable
    override fun Content() {
        ManagerTheme {
            val navController = rememberNavController()
            val viewModel = viewModel<TemplateInfoViewModel>(
                factory = TemplateInfoViewModel.factory(
                    templateId,
                    TemplateSettingsMode.MODE_CREATE_ROOM
                )
            )

            NavHost(
                navController = navController,
                startDestination = Screens.Info
            ) {
                composable<Screens.Info> {
                    TemplateInfoScreen(
                        viewModel = viewModel,
                        showSnackbar = ::showSnackbar,
                        navigateToAccessSettings = {
                            navController.navigate(Screens.AccessSettings)
                        },
                        navigateToCreated = { roomId, roomType, _ ->
                            setResult(roomId, roomType)
                            dismiss()
                        },
                        onBack = ::dismiss
                    )
                }

                composable<Screens.AccessSettings> {
                    AccessSettingsDestination(
                        templateId = templateId,
                        modeId = TemplateSettingsMode.MODE_CREATE_ROOM,
                        initSettings = viewModel.uiState.value.accessSettings,
                        showSnackbar = ::showSnackbar,
                        saveSettings = true,
                        onSaveClick = viewModel::updateAccessSettings,
                        onSavedSuccessfully = navController::popBackStackWhenResumed,
                        onClose = navController::popBackStackWhenResumed
                    )
                }
            }
        }
    }

    private fun setResult(id: String?, roomType: Int?) {
        parentFragmentManager.setFragmentResult(
            TAG_FRAGMENT_RESULT,
            bundleOf(
                KEY_SAVED_ID to id,
                KEY_SAVED_ROOM_TYPE to roomType
            )
        )
    }

    sealed interface Screens {
        @Serializable
        data object Info : Screens

        @Serializable
        data object AccessSettings : Screens
    }

    companion object {
        private val TAG: String = TemplateInfoFragment::class.java.simpleName
        private const val TAG_FRAGMENT_RESULT = "TemplateInfoFragmentResult"
        private const val KEY_TEMPLATE_ID = "key_template_id"
        const val KEY_SAVED_ID = "key_saved_room_id"
        const val KEY_SAVED_ROOM_TYPE = "key_saved_room_type"

        fun show(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            templateId: String?,
            onResult: (Bundle) -> Unit
        ) {
            fragmentManager.setFragmentResultListener(
                TAG_FRAGMENT_RESULT,
                lifecycleOwner
            ) { _, bundle -> onResult(bundle) }
            newInstance(templateId).show(fragmentManager, TAG)
        }

        private fun newInstance(templateId: String?) =
            TemplateInfoFragment().putArgs(KEY_TEMPLATE_ID to templateId)
    }
}