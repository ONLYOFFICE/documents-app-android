package app.editors.manager.viewModels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.account.AccountRepository
import app.documents.core.login.OwnCloudLoginRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.model.login.OidcConfiguration
import app.documents.core.network.common.NetworkResult
import app.editors.manager.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URL
import java.util.UUID

sealed class WebViewCloudLogin {

    data object Success : WebViewCloudLogin()
    data object Error : WebViewCloudLogin()
    data object None : WebViewCloudLogin()
}

class WebViewCloudLoginViewModel(
    private val accountRepository: AccountRepository,
    private val ownCloudLoginRepository: OwnCloudLoginRepository
) : ViewModel() {

    private val _state: MutableStateFlow<WebViewCloudLogin> =
        MutableStateFlow(WebViewCloudLogin.None)
    val state: StateFlow<WebViewCloudLogin> = _state.asStateFlow()

    fun saveNextCloudUser(path: String) {
        val args = path.split("&").toTypedArray()
        if (args.size > 2) {
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    accountRepository.addAccount(
                        cloudAccount = createNextCloudAccount(
                            url = URL(args[0].substring(args[0].indexOf(":") + 1)),
                            login = args[1].substring(args[1].indexOf(":") + 1)
                        ),
                        password = args[2].substring(args[2].indexOf(":") + 1)
                    )
                }
                _state.value = WebViewCloudLogin.Success
            } catch (_: MalformedURLException) {
                _state.value = WebViewCloudLogin.Error
            }
        }
    }

    fun saveOwnCloudUser(code: String, configuration: OidcConfiguration) {
        viewModelScope.launch(Dispatchers.IO) {
            ownCloudLoginRepository.signIn(code, configuration).collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> _state.value = WebViewCloudLogin.Success
                        is NetworkResult.Error -> _state.value = WebViewCloudLogin.Error
                        else -> Unit
                    }
                }

            }
        }
    }

    private fun createNextCloudAccount(url: URL, login: String): CloudAccount {
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                WebViewCloudLoginViewModel(
                    accountRepository = app.loginComponent.accountRepository,
                    ownCloudLoginRepository = app.loginComponent.owncloudLoginRepository
                )
            }
        }
    }
}