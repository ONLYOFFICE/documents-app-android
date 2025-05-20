package app.editors.manager.ui.fragments.main.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.editors.manager.app.accountOnline
import lib.toolkit.base.managers.utils.AccountUtils

data class AccountContext(
    val token: String,
    val portal: CloudPortal
)

@Composable
fun rememberAccountContext(): AccountContext {
    val context = LocalContext.current
    val view = LocalView.current

    val token = remember {
        if (!view.isInEditMode) {
            context.accountOnline?.accountName?.let { AccountUtils.getToken(context, it) }
                .orEmpty()
        } else {
            ""
        }
    }
    val portal = remember {
        if (!view.isInEditMode) {
            context.accountOnline?.portal
                ?: CloudPortal(provider = PortalProvider.Cloud.DocSpace)
        } else {
            CloudPortal(provider = PortalProvider.Cloud.DocSpace)
        }
    }
    return AccountContext(token, portal)
}