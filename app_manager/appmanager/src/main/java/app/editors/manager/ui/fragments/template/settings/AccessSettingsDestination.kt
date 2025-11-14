package app.editors.manager.ui.fragments.template.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.viewModels.main.TemplateAccessSettings
import app.editors.manager.viewModels.main.TemplateUserListEffect
import app.editors.manager.viewModels.main.TemplateUserListViewModel
import app.editors.manager.viewModels.main.UserListEffect
import kotlinx.serialization.Serializable
import lib.compose.ui.utils.popBackStackWhenResumed

sealed interface Screens {
    @Serializable
    data object AccessSettings : Screens

    @Serializable
    data object SelectMembers : Screens

    @Serializable
    data object ConfirmMembers : Screens
}

@Composable
fun AccessSettingsDestination(
    templateId: String,
    modeId: Int,
    initSettings: TemplateAccessSettings?,
    showSnackbar: (String) -> Unit,
    onClose: () -> Unit,
    saveSettings: Boolean = false,
    onSaveClick: ((TemplateAccessSettings) -> Unit)? = null,
    onSavedSuccessfully: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val accessViewModel = viewModel<TemplateUserListViewModel>(
        factory = TemplateUserListViewModel.factory(
            templateId = templateId,
            modeId = modeId,
            initSettings = initSettings
        )
    )

    LaunchedEffect(accessViewModel) {
        accessViewModel.effect.collect { effect ->
            when (effect) {
                is UserListEffect.Error -> {
                    val message = effect.errorCode?.let { code ->
                        context.getString(R.string.errors_client_error) + code
                    } ?: context.getString(R.string.errors_unknown_error)
                    showSnackbar(message)
                }
                is TemplateUserListEffect.SavedSuccessfully -> onSavedSuccessfully?.invoke()
                else -> Unit
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screens.AccessSettings
    ) {
        composable<Screens.AccessSettings> {
            AccessSettingsScreen(
                viewModel = accessViewModel,
                goToList = {
                    accessViewModel.setSelection()
                    navController.navigate(Screens.SelectMembers)
                },
                onSave = { settings ->
                    if (saveSettings) accessViewModel.onSaveClick(settings)
                    onSaveClick?.invoke(settings)
                },
                onBack = onClose
            )
        }

        composable<Screens.SelectMembers> {
            SelectMembersScreen(
                viewModel = accessViewModel,
                onNext = {
                    accessViewModel.search("")
                    navController.navigate(Screens.ConfirmMembers)
                         },
                onBack = navController::popBackStackWhenResumed
            )
        }

        composable<Screens.ConfirmMembers> {
            ConfirmationScreen(
                viewModel = accessViewModel,
                onConfirm = {
                    accessViewModel.onConfirmMembersList()
                    navController.navigate(Screens.AccessSettings) {
                        popUpTo(Screens.AccessSettings) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onBack = navController::popBackStackWhenResumed
            )
        }
    }
}