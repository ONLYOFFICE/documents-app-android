package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.setFragmentResult
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import lib.compose.ui.addIf
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppDivider
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class AddRoomBottomDialog : BaseBottomDialog() {

    companion object {
        val TAG: String = AddRoomBottomDialog::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.Theme_Common_BottomSheetDialog)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        init(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult("result", Bundle(1).apply { putInt("type", -1) })
    }

    private fun init(dialog: Dialog) {
        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent {
                ManagerTheme {
                    Surface(shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)) {
                        AddRoomBottomDialogContent { type ->
                            setFragmentResult("result", Bundle(1).apply { putInt("type", type) })
                            dismiss()
                        }
                    }
                }
            }
        })
        dialog.setCanceledOnTouchOutside(true)
    }

}

//TODO Set room type constants
@Composable
private fun AddRoomBottomDialogContent(itemClick: (type: Int) -> Unit) {
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colors.surface)
            .wrapContentHeight()
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
        AddRoomItem(
            icon = R.drawable.ic_collaboration_room,
            title = R.string.rooms_add_collaboration,
            description = R.string.rooms_add_collaboration_des
        ) {
            itemClick(2)
        }
        AddRoomItem(
            icon = R.drawable.ic_public_room,
            title = R.string.rooms_add_public_room,
            description = R.string.rooms_add_public_room_des
        ) {
            itemClick(6)
        }
        AddRoomItem(
            icon = R.drawable.ic_custom_room,
            title = R.string.rooms_add_custom,
            description = R.string.rooms_add_custom_des
        ) {
            itemClick(ApiContract.RoomType.CUSTOM_ROOM)
        }
    }
}

@Composable
fun AddRoomItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    isSelect: Boolean? = null,
    isClickable: Boolean = true,
    itemClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .addIf(isClickable) {
                clickable { itemClick() }
            }
    ) {
        Row(
            Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 8.dp)
                .widthIn()
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = icon), contentDescription = null, modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(id = title),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .align(Alignment.Start)
                )
                Text(
                    text = stringResource(id = description),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            if (isSelect != null) {
                if (isSelect) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = lib.toolkit.base.R.drawable.ic_done),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(0.1f)
                    )
                } else {
                    Spacer(modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(0.1f))
                }
            } else {
                if (isClickable) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = lib.toolkit.base.R.drawable.ic_arrow_right),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        tint = MaterialTheme.colors.colorTextTertiary
                    )
                }
            }
        }
        AppDivider(startIndent = 64.dp, modifier = Modifier.align(Alignment.BottomStart))
    }

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