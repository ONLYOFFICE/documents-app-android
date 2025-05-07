package app.editors.manager.ui.views.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import app.documents.core.network.manager.models.explorer.FormRole
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.FormRoleHistory
import app.editors.manager.mvp.models.ui.FormRoleStatus
import app.editors.manager.mvp.models.ui.FormRoleUi
import app.editors.manager.mvp.models.ui.toUi
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorGreen
import lib.compose.ui.theme.colorTextPrimary
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppScaffold

private enum class CompleteStatus {
    Waiting, Complete, Stopped
}

@Composable
fun FillingStatusRoleList(modifier: Modifier = Modifier, data: List<FormRoleUi>) {
    Column(modifier = modifier) {
        var stopped = remember { false }
        data.forEach { role ->
            if (role.stoppedBy != null) {
                stopped = true
            }
            RoleItem(
                role = role,
                stopped = stopped
            )
        }
        CompleteContent(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
            completeStatus = when {
                data.any { it.stoppedBy != null } -> CompleteStatus.Stopped
                data.none { it.submitted == false } -> CompleteStatus.Complete
                else -> CompleteStatus.Waiting
            }
        )
    }
}

@Composable
private fun RoleItem(
    modifier: Modifier = Modifier,
    role: FormRoleUi,
    stopped: Boolean
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val (sequence,
            avatar,
            nameColumn,
            historyColumn,
            avatarHorizontalCenter
        ) = createRefs()
        Box(
            modifier = Modifier
                .height(64.dp)
                .width(48.dp)
                .constrainAs(sequence) {
                    start.linkTo(parent.start)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier,
                text = "${role.sequence}"
            )
        }
        Box(
            modifier = Modifier
                .height(64.dp)
                .constrainAs(avatar) {
                    start.linkTo(sequence.end)
                },
            contentAlignment = Alignment.Center
        ) {
            AvatarContent(
                imageUrl = "",
                roleStatus = role.roleStatus,
                stopped = stopped
            )
        }
        Box(
            modifier = Modifier.constrainAs(avatarHorizontalCenter) {
                start.linkTo(avatar.start)
                end.linkTo(avatar.end)
            }
        )
        Column(
            modifier = Modifier
                .height(64.dp)
                .constrainAs(nameColumn) {
                    horizontalBias = 0f
                    start.linkTo(avatar.end, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = role.roleName,
                maxLines = 1,
                color = if (stopped) {
                    MaterialTheme.colors.error
                } else if (role.roleStatus == FormRoleStatus.YourTurn) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.colorTextPrimary
                }
            )
            Text(
                text = role.user.displayNameFromHtml,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary,
                maxLines = 1
            )
        }
        HistoryContent(
            modifier = Modifier
                .constrainAs(historyColumn) {
                    width = Dimension.fillToConstraints
                    start.linkTo(avatarHorizontalCenter.start)
                    top.linkTo(avatar.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                },
            history = role.history,
            dashEffect = role.roleStatus in arrayOf(
                FormRoleStatus.YourTurn, FormRoleStatus.Waiting
            ) && !stopped,
            strokeColor = if (stopped) {
                MaterialTheme.colors.error
            } else {
                MaterialTheme.colors.colorTextTertiary
            }
        )
    }
}

@Composable
private fun AvatarContent(
    modifier: Modifier = Modifier,
    imageUrl: String,
    stopped: Boolean,
    roleStatus: FormRoleStatus
) {
    CircleContent(
        dashEffect = roleStatus in arrayOf(
            FormRoleStatus.YourTurn, FormRoleStatus.Waiting
        ) && !stopped,
        color = if (stopped) {
            MaterialTheme.colors.error
        } else if (roleStatus == FormRoleStatus.YourTurn) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.colorTextTertiary
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(lib.toolkit.base.R.color.colorOutline)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_list_item_share_user_icon),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun HistoryContent(
    modifier: Modifier = Modifier,
    dashEffect: Boolean,
    strokeColor: Color,
    history: List<FormRoleHistory>?
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .drawBehind {
                    drawRect(
                        color = strokeColor,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect
                                .dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()), 0f)
                                .takeIf { dashEffect }
                        )
                    )
                }
        )
        Column {
            history?.forEach { data ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.body2.copy(lineHeight = 16.sp)
                    )
                    Text(
                        text = data.date,
                        style = MaterialTheme.typography.body2.copy(lineHeight = 16.sp),
                        color = MaterialTheme.colors.colorTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CompleteContent(modifier: Modifier = Modifier, completeStatus: CompleteStatus) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircleContent(
            modifier = Modifier.padding(start = 16.dp + 16.dp + 16.dp),
            dashEffect = completeStatus == CompleteStatus.Waiting,
            color = when (completeStatus) {
                CompleteStatus.Waiting -> MaterialTheme.colors.colorTextTertiary
                CompleteStatus.Complete -> MaterialTheme.colors.colorGreen
                CompleteStatus.Stopped -> MaterialTheme.colors.error
            },
            background = when (completeStatus) {
                CompleteStatus.Complete -> MaterialTheme.colors.colorGreen
                CompleteStatus.Stopped -> MaterialTheme.colors.error
                CompleteStatus.Waiting -> null
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.drawable_ic_done),
                tint = if (completeStatus == CompleteStatus.Waiting) {
                    MaterialTheme.colors.colorTextTertiary
                } else {
                    MaterialTheme.colors.onPrimary
                },
                contentDescription = null
            )
        }
        Text(
            text = if (completeStatus == CompleteStatus.Stopped) {
                stringResource(R.string.filling_form_stopped)
            } else {
                stringResource(R.string.filling_form_complete)
            },
            color = when (completeStatus) {
                CompleteStatus.Waiting -> MaterialTheme.colors.colorTextTertiary
                CompleteStatus.Complete -> MaterialTheme.colors.colorGreen
                CompleteStatus.Stopped -> MaterialTheme.colors.error
            }
        )
    }
}

@Composable
private fun CircleContent(
    modifier: Modifier = Modifier,
    dashEffect: Boolean,
    color: Color,
    background: Color? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .drawBehind {
                drawCircle(
                    color = color,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect
                            .dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()), 0f)
                            .takeIf { dashEffect }
                    )
                )
            }
            .border(1.5.dp, MaterialTheme.colors.surface, CircleShape)
            .clip(CircleShape)
            .run { background?.let { then(Modifier.background(it)) } ?: this },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
private fun FillingStatusDetailsPreview() {
    ManagerTheme {
        AppScaffold {
            FillingStatusRoleList(
                data = FormRole
                    .mockList
                    .map { it.toUi(LocalContext.current) }
            )
        }
    }
}