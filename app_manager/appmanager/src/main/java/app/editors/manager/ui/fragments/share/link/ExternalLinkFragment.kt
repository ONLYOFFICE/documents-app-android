package app.editors.manager.ui.fragments.share.link

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import app.editors.manager.R
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar

class ExternalLinkFragment : BaseAppFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            ExternalLinkScreen(onBackClick = parentFragmentManager::popBackStack)
        }
    }

    @Composable
    private fun ExternalLinkScreen(onBackClick: () -> Unit) {
        AppScaffold(
            topBar = {
                AppTopBar(title = R.string.share_title_main, backListener = onBackClick)
            }
        ) {

        }
    }

    @Preview
    @Composable
    fun ExternalLinkScreenPreview() {
        ManagerTheme {
            ExternalLinkScreen {}
        }
    }
}