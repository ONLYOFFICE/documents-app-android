package app.editors.manager.viewModels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.editors.manager.app.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL
import java.util.UUID

sealed class NextCloudLogin {

    data object Success : NextCloudLogin()
    data object Error : NextCloudLogin()
    data object None : NextCloudLogin()
}

class NextCloudLoginViewModel : ViewModel() {

    private val _state: MutableStateFlow<NextCloudLogin> = MutableStateFlow(NextCloudLogin.None)
    val state: StateFlow<NextCloudLogin> = _state.asStateFlow()

    fun saveUser(path: String) {
        val args = path.split("&").toTypedArray()
        if (args.size > 2) {
            try {
                viewModelScope.launch {
                    App.getApp().loginComponent
                        .accountRepository.addAccount(
                            cloudAccount = createCloudAccount(
                                url = URL(args[0].substring(args[0].indexOf(":") + 1)),
                                login = args[1].substring(args[1].indexOf(":") + 1)
                            ),
                            password = args[2].substring(args[2].indexOf(":") + 1)
                        )
                }
                _state.value = NextCloudLogin.Success
            } catch (e: MalformedURLException) {
                _state.value = NextCloudLogin.Error
            }
        }
    }

    private fun createCloudAccount(url: URL, login: String): CloudAccount {
        val portal = url.toString().replace(".*://".toRegex(), "")
        return CloudAccount(
            id = UUID.nameUUIDFromBytes("$login@$portal".toByteArray()).toString(),
            portalUrl = portal,
            login = login,
            name = login,
            portal = CloudPortal(
                url = portal,
                scheme = Scheme.Custom(url.protocol + "://"),
                provider = PortalProvider.Webdav(
                    provider = WebdavProvider.NextCloud,
                    path = url.path.orEmpty() + WebdavProvider.DEFAULT_NEXT_CLOUD_PATH + "$login/"
                )
            )
        )
    }
}