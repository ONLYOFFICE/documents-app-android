package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.R
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import app.editors.manager.ui.fragments.base.BaseAppFragment
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.putArgs

class ProfileFragment : BaseAppFragment() {

    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName

        private const val KEY_ACCOUNT = "KEY_ACCOUNT"
        private const val KEY_IS_ONLINE = "KEY_IS_ONLINE"

        fun newInstance(account: String, isOnline: Boolean): ProfileFragment {
            return ProfileFragment().putArgs(
                KEY_ACCOUNT to account,
                KEY_IS_ONLINE to isOnline
            )
        }
    }

    private val account: CloudAccount by lazy {
        Json.decodeFromString(checkNotNull(arguments?.getString(KEY_ACCOUNT)))
    }

    private val accountDialogFragment: IBaseDialogFragment? get() = getDialogFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar()
        (view as? ComposeView)?.setContent {
            ManagerTheme {
                ProfileScreen(
                    account = account,
                    isOnline = arguments?.getBoolean(KEY_IS_ONLINE) == true,
                    onLogOut = {
                        setFragmentResult(
                            CloudAccountFragment.REQUEST_PROFILE,
                            bundleOf(CloudAccountFragment.RESULT_LOG_OUT to account.id)
                        )
                        parentFragmentManager.popBackStack()
                    },
                    onSignIn = {
                        setFragmentResult(
                            CloudAccountFragment.REQUEST_PROFILE,
                            bundleOf(CloudAccountFragment.RESULT_SIGN_IN to account.id)
                        )
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

    private fun initToolbar() {
        if (isTablet) {
            accountDialogFragment?.setToolbarTitle(getString(R.string.fragment_profile_title))
            accountDialogFragment?.setToolbarNavigationIcon(isClose = false)
        } else {
            setActionBarTitle(getString(R.string.fragment_profile_title))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }
}

@Composable
private fun ProfileScreen(
    account: CloudAccount,
    isOnline: Boolean,
    onLogOut: () -> Unit,
    onSignIn: () -> Unit
) {
    AppScaffold {
        NestedColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            RowItem(
                icon = R.drawable.ic_list_item_share_user_icon,
                title = R.string.profile_username_title,
                subtitle = account.name
            )
            RowItem(
                icon = R.drawable.ic_email,
                title = R.string.login_enterprise_email_hint,
                subtitle = account.login
            )
            RowItem(
                icon = R.drawable.ic_cloud,
                title = R.string.profile_portal_address,
                subtitle = account.portal.urlWithScheme
            )
            RowItem(
                icon = R.drawable.ic_contact_calendar,
                title = R.string.filter_title_type,
                subtitle = when {
                    account.isVisitor -> stringResource(R.string.profile_type_visitor)
                    account.isAdmin -> stringResource(R.string.profile_type_admin)
                    else -> stringResource(R.string.profile_type_user)
                }
            )
            if (isOnline) {
                AppTextButton(
                    title = R.string.navigation_drawer_menu_logout,
                    textColor = MaterialTheme.colors.error,
                    onClick = onLogOut
                )
            } else {
                AppTextButton(
                    title = R.string.navigation_drawer_menu_sign_in,
                    onClick = onSignIn
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ManagerTheme {
        ProfileScreen(CloudAccount(""), false, {}, {})
    }
}

@Composable
fun RowItem(icon: Int, title: Int, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_onehalf_line_height)),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colors.colorTextSecondary
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = title), style = MaterialTheme.typography.body1)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
    }
}