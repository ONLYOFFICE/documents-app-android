package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.compose.ui.theme.AppManagerTheme
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class AddRoomBottomDialog : BaseBottomDialog() {

    interface OnClickListener {
        fun onActionButtonClick(roomType: Int)
        fun onActionDialogClose()
    }

    companion object {
        val TAG: String = AddRoomBottomDialog::class.java.simpleName
    }

    var onClickListener: OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.ContextMenuDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onClickListener = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClickListener?.onActionDialogClose()
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        init(dialog)
    }

    private fun init(dialog: Dialog) {
        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent {
                AppManagerTheme {
                    Surface(shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)) {
                        AddRoomBottomDialogContent { type ->
                            onClickListener?.onActionButtonClick(type)
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
private fun AddRoomBottomDialogContent(itemClick: (type: Int) -> Unit) {
    Column(modifier = Modifier
        .background(color = MaterialTheme.colors.surface)
        .padding(bottom = 8.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ic_bottom_divider),
            contentDescription = null,
            alignment = Alignment.TopCenter,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onSurface),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        )
        AddRoomItem(
            icon = R.drawable.ic_room_fill_forms,
            title = R.string.rooms_add_filling_forms,
            description = R.string.rooms_add_filling_forms_des
        ) {
            itemClick(ApiContract.RoomType.FILLING_FORM_ROOM)
        }
        AddRoomItem(
            icon = R.drawable.ic_room_collaboration,
            title = R.string.rooms_add_collaboration,
            description = R.string.rooms_add_collaboration_des
        ) {
            itemClick(ApiContract.RoomType.EDITING_ROOM)
        }
        AddRoomItem(
            icon = R.drawable.ic_room_review,
            title = R.string.rooms_add_review,
            description = R.string.rooms_add_review_des
        ) {
            itemClick(ApiContract.RoomType.REVIEW_ROOM)
        }
        AddRoomItem(
            icon = R.drawable.ic_room_view_only,
            title = R.string.rooms_add_view_only,
            description = R.string.rooms_add_view_only_des
        ) {
            itemClick(ApiContract.RoomType.READ_ONLY_ROOM)
        }
        AddRoomItem(
            icon = R.drawable.ic_room_custom,
            title = R.string.rooms_add_custom,
            description = R.string.rooms_add_custom_des
        ) {
            itemClick(ApiContract.RoomType.CUSTOM_ROOM)
        }
    }
}

@Composable
private fun AddRoomItem(@DrawableRes icon: Int, @StringRes title: Int, @StringRes description: Int, itemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_one_line_height))
            .clickable { itemClick() }
    ) {
        Row(Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)) {
            Image(
                painter = painterResource(id = icon), contentDescription = null, modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxHeight()
            )
//            Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp)
            )
//                Text(text = stringResource(id = description), style = MaterialTheme.typography.body2, modifier = Modifier.align(Alignment.Start))
//            }
        }

    }

}