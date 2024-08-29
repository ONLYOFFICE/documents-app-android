package app.editors.manager.ui.dialogs.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.model.login.User
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.base.FillResult
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.app.api
import app.editors.manager.managers.utils.GlideAvatarImage
import app.editors.manager.managers.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs

private sealed class FormCompleteState {
    data object Loading : FormCompleteState()
    class Success(val result: FillResult) : FormCompleteState()
    class Error(val message: String) : FormCompleteState()
}

private class FormCompleteViewModel(
    private val managerService: ManagerService,
    private val sessionId: String
) : ViewModel() {

    private val _roomState: MutableStateFlow<FormCompleteState> = MutableStateFlow(FormCompleteState.Loading)
    val roomState: StateFlow<FormCompleteState> = _roomState

    init {
        getResult()
    }

    private fun getResult() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = managerService.getFillResult(sessionId).response
                _roomState.emit(FormCompleteState.Success(result))
            } catch (error: Throwable) {
                _roomState.emit(FormCompleteState.Error(error.message ?: ""))
            }
        }
    }

}

class FormCompletedDialogFragment : BaseDialogFragment() {

    companion object {

        private const val KEY_SESSION_ID = "key_id"
        const val KEY_RESULT = "fill_form"


        private fun newInstance(
            sessionId: String
        ): FormCompletedDialogFragment {
            return FormCompletedDialogFragment().putArgs(
                KEY_SESSION_ID to sessionId
            )
        }

        fun show(fragmentManager: FragmentManager, sessionId: String) {
            newInstance(sessionId).show(fragmentManager, null)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ComponentDialog(
            requireContext(),
            if (!UiUtils.isTablet(requireContext())) R.style.FullScreenDialog else 0
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {

            val viewModel = viewModel {
                FormCompleteViewModel(requireContext().api, arguments?.getSerializableExt<String>(KEY_SESSION_ID) ?: "")
            }

            val response = viewModel.roomState.collectAsState().value

            ManagerTheme {
                AppScaffold(topBar = {
                    AppTopBar(title = R.string.rooms_fill_form_complete_toolbar_title)
                }) {
                    when (response) {
                        is FormCompleteState.Error -> {
                            Log.d("TAG", "onViewCreated: ${response.message}")
                        }

                        FormCompleteState.Loading -> {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        is FormCompleteState.Success -> {
                            FormCompletedScreen(
                                response.result,
                                onSendEmailClick = {
                                    ActivitiesUtils.showEmail(
                                        context = requireContext(),
                                        chooseTitle = "",
                                        to = response.result.manager.email ?: "",
                                        subject = "",
                                        body = ""
                                    )
                                },
                                onCheckReadyFormsClick = {
                                    setFragmentResult(
                                        requestKey = KEY_RESULT,
                                        result = bundleOf("id" to response.result.completedForm.folderId)
                                    )
                                    dismiss()
                                },
                                onBackToRoomClick = ::dismiss,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormCompletedScreen(
    fillResult: FillResult,
    onSendEmailClick: () -> Unit,
    onBackToRoomClick: () -> Unit,
    onCheckReadyFormsClick: () -> Unit
) {

    NestedColumn(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current
        AppDescriptionItem(
            modifier = Modifier.padding(vertical = 16.dp),
            text = R.string.rooms_fill_form_complete_desc
        )
        RowItem(
            title = fillResult.completedForm.title,
            subtitle = StringUtils.getCloudItemInfo(
                context = context,
                item = fillResult.completedForm,
                userId = null,
                sortBy = null
            ).orEmpty(),
            divider = true,
            image = {
                Image(
                    modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.vectorResource(
                        R.drawable.ic_type_pdf
                    ),
                    contentDescription = null
                )
            },
            endButton = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_list_context_external_link),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        )
        AppHeaderItem(title = R.string.rooms_fill_form_complete_number_title)
        AppListItem(title = fillResult.formNumber.toString())
        AppHeaderItem(title = R.string.rooms_fill_form_complete_owner_title)
        RowItem(
            title = fillResult.manager.displayName,
            subtitle = fillResult.manager.email ?: "",
            divider = false,
            image = { GlideAvatarImage(url = fillResult.manager.avatarMedium) }
        ) {
            IconButton(onClick = onSendEmailClick) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTextButton(
                title = R.string.rooms_fill_form_complete_back_to_room,
                onClick = onBackToRoomClick
            )
            AppTextButton(
                title = R.string.rooms_fill_form_complete_check_forms,
                onClick = onCheckReadyFormsClick
            )
        }
    }
}

@Composable
private fun RowItem(
    title: String,
    subtitle: String,
    divider: Boolean,
    image: @Composable () -> Unit,
    endButton: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            image()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(text = title)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.colorTextSecondary
                    )
                }
                endButton()
            }
            if (divider) AppDivider()
        }
    }
}

@Preview
@Composable
private fun FormCompletedScreenPreview() {
    ManagerTheme {
        AppScaffold(topBar = {
            AppTopBar(title = R.string.rooms_fill_form_complete_toolbar_title)
        }) {
            FormCompletedScreen(
                FillResult(
                    CloudFile(),
                    manager = User(),
                    originalForm = CloudFile(),
                    formNumber = 1,
                    roomId = 123,
                    isRoomMember = true
                ),
                {},
                {},
                {}
            )
        }
    }
}