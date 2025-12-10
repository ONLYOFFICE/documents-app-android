package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import app.editors.manager.managers.utils.RoomUtils
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppMultilineArrowItem
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class AddRoomBottomDialog : BaseBottomDialog() {

    companion object {
        const val KEY_RESULT_TYPE = "key_result_type"
        const val KEY_REQUEST_TYPE = "key_request_type"

        val TAG: String = AddRoomBottomDialog::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.Theme_Common_BottomSheetDialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (requireActivity() as OnBottomDialogCloseListener).onBottomDialogClose()
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        init(dialog)
    }

    private fun init(dialog: Dialog) {
        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent {
                ManagerTheme {
                    Surface(shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)) {
                        AddRoomBottomDialogContent { type ->
                            setFragmentResult(KEY_REQUEST_TYPE, bundleOf(KEY_RESULT_TYPE to type))
                            dismiss()
                        }
                    }
                }
            }
        })
        dialog.setCanceledOnTouchOutside(true)
    }

}

@Composable
private fun AddRoomBottomDialogContent(onClick: (type: Int) -> Unit) {
    NestedColumn(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface)
            .padding(bottom = 8.dp)
    ) {
        Image(
            painter = painterResource(id = lib.toolkit.base.R.drawable.ic_bottom_divider),
            contentDescription = null,
            alignment = Alignment.TopCenter,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onSurface),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        )
        for (type in RoomUtils.roomTypes) {
            AddRoomItem(roomType = type, onClick = onClick)
        }
        AddRoomItem(roomType = 0, onClick = onClick, isTemplate = true)
    }
}

@Composable
fun AddRoomItem(
    roomType: Int,
    selected: Boolean? = null,
    clickable: Boolean = true,
    isTemplate: Boolean = false,
    onClick: (Int) -> Unit
) {
    val info = RoomUtils.getRoomInfo(roomType, isTemplate)
    AppMultilineArrowItem(
        icon = info.icon,
        selected = selected,
        clickable = clickable,
        title = stringResource(id = info.title),
        description = stringResource(id = info.description),
        onClick = { onClick.invoke(roomType) }
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = false)
@Composable
private fun PreviewNight() {
    ManagerTheme {
        Surface {
            AddRoomBottomDialogContent {}
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        Surface {
            AddRoomBottomDialogContent {}
        }
    }
}