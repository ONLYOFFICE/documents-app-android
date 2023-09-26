package app.documents.core.network.common.contracts

object StorageContract {

    const val ARG_AUTH_URL = "auth_url"
    const val ARG_CLIENT_ID = "client_id"
    const val ARG_CLIENT_SECRET = "client_secret"
    const val ARG_REDIRECT_URI = "redirect_uri"
    const val ARG_RESPONSE_TYPE = "response_type"
    const val ARG_REFRESH_TOKEN = "refresh_token"
    const val ARG_ACCESS_TYPE = "access_type"
    const val ARG_GRANT_TYPE = "grant_type"
    const val ARG_APPROVAL_PROMPT = "approval_prompt"
    const val ARG_SCOPE = "scope"
    const val ARG_CODE = "code"

    object Box {
        const val AUTH_URL = "https://account.box.com/api/oauth2/authorize?"
        const val VALUE_RESPONSE_TYPE = "code"
    }

    object DropBox {
        const val AUTH_URL = "https://www.dropbox.com/oauth2/authorize?"
        const val VALUE_RESPONSE_TYPE = "code"
        const val VALUE_GRANT_TYPE = "authorization_code"
    }

    object Google {
        const val AUTH_URL = "https://accounts.google.com/o/oauth2/auth?"
        const val VALUE_RESPONSE_TYPE = "code"
        const val VALUE_ACCESS_TYPE = "offline"
        const val VALUE_GRANT_TYPE_AUTH = "authorization_code"
        const val VALUE_APPROVAL_PROMPT = "force"
        const val VALUE_SCOPE = "https://www.googleapis.com/auth/drive"
    }

    object OneDrive {
        const val AUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?"
        const val VALUE_SCOPE = "User.Read files.readwrite.all offline_access"
        const val VALUE_GRANT_TYPE_AUTH = "authorization_code"
        const val VALUE_GRANT_TYPE_REFRESH = "refresh_token"
        const val VALUE_RESPONSE_TYPE = "code"
    }

    object WevDav {
        const val URL_YANDEX = "https://webdav.yandex.ru"
        const val URL_KDRIVE = "https://connect.drive.infomaniak.com"
    }

}