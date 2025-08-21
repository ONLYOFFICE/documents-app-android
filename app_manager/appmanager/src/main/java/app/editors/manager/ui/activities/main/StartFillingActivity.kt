package app.editors.manager.ui.activities.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.collection.ArrayMap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.documents.core.model.login.User
import lib.compose.ui.theme.BaseAppTheme
import lib.compose.ui.theme.LocalUseTabletPadding
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.FormRole
import lib.toolkit.base.managers.utils.FormRoleList

class StartFillingActivity : ComponentActivity() {

    private val formRoles: ArrayMap<FormRole, User?> by lazy {
        val formRoles = FormRoleList
            .fromJson(intent.extras?.getString(EditorsContract.EXTRA_FORM_ROLES))

        ArrayMap<FormRole, User?>(formRoles.size).apply {
            formRoles.forEach { role ->
                this[role] = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseAppTheme(
                primaryColor = getThemeColor()
            ) {
                CompositionLocalProvider(LocalUseTabletPadding provides true) {
                    StartFillingScreen(
                        roles = ArrayMap(formRoles),
                        onClose = ::finish
                    )
                }
            }
        }
    }

    private fun getThemeColor(): Color {
        return intent?.getIntExtra(EditorsContract.EXTRA_THEME_COLOR, -1)?.let { Color(it) }
            ?: Color(getColor(lib.toolkit.base.R.color.colorPrimary))
    }
}

@Composable
private fun StartFillingScreen(
    modifier: Modifier = Modifier,
    roles: ArrayMap<FormRole, User?>,
    onClose: () -> Unit
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Start filling",
                backListener = onClose,
                isClose = true
            )
        }
    ) {
        NestedColumn {
            AppDescriptionItem(
                modifier = Modifier.padding(top = 8.dp),
                text = "In this panel you can monitor the completion of the form in which you participate or in which you are the organizer of completion"
            )
            AppHeaderItem(
                title = "Roles from the form:"
            )
            roles.entries.withIndex().forEach { entry ->
                RoleItem(
                    index = entry.index + 1,
                    roleName = entry.value.key.name,
                    roleColor = Color(entry.value.key.color),
                    user = entry.value.value
                )
            }
        }
    }
}

@Composable
private fun RoleItem(
    modifier: Modifier = Modifier,
    index: Int,
    roleName: String,
    roleColor: Color,
    user: User?
) {
    Row(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = index.toString())
        }
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(roleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_add),
                    contentDescription = null,
                    tint = MaterialTheme.colors.colorTextSecondary
                )
            }
        }
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = roleName
        )
    }
}