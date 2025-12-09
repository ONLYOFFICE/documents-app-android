package app.documents.core.network

const val API_VERSION = "2.0"

const val HEADER_AUTHORIZATION = "Authorization"
const val HEADER_HOST = "Host"
const val HEADER_CONTENT_OPERATION_TYPE = "Content-OperationType"
const val HEADER_CONTENT_TYPE = "Content-Type"
const val HEADER_ACCEPT = "Accept"
const val HEADER_AGENT = "User-Agent"
const val HEADER_CACHE = "Cache-Control"
const val HEADER_DEPTH = "Depth"

const val HTTP_METHOD_GET = "GET"
const val HTTP_METHOD_PROPFIND = "PROPFIND"
const val HTTP_METHOD_POST = "POST"

const val VALUE_CONTENT_TYPE = "application/json"
const val VALUE_ACCEPT = "application/json"
const val VALUE_CACHE = "no-cache"
const val VALUE_GRANT_TYPE_AUTH = "authorization_code"
const val VALUE_GRANT_TYPE_REFRESH = "refresh_token"

const val ARG_AUTH_URL = "auth_url"
const val ARG_CLIENT_ID = "client_id"
const val ARG_CLIENT_SECRET = "client_secret"
const val ARG_REDIRECT_URI = "redirect_uri"
const val ARG_RESPONSE_TYPE = "response_type"
const val ARG_REFRESH_TOKEN = "refresh_token"
const val ARG_TOKEN_ACCESS = "token_access_type"
const val ARG_ACCESS_TYPE = "access_type"
const val ARG_GRANT_TYPE = "grant_type"
const val ARG_APPROVAL_PROMPT = "approval_prompt"
const val ARG_SCOPE = "scope"
const val ARG_CODE = "code"

const val GOOGLE_DRIVE_AUTH_URL = "https://oauth2.googleapis.com/"
const val GOOGLE_DRIVE_BASE_URL = "https://www.googleapis.com/"
const val GOOGLE_DRIVE_URL = "drive.google.com"

const val DROPBOX_BASE_URL = "https://api.dropboxapi.com/"
const val DROPBOX_PORTAL_URL = "dropbox.com"

const val ONEDRIVE_AUTH_URL = "https://login.microsoftonline.com/"
const val ONEDRIVE_PORTAL_URL = "graph.microsoft.com"
const val ONEDRIVE_API_VERSION = "v1.0"
const val ONEDRIVE_VALUE_SCOPE = "User.Read files.readwrite.all offline_access"

const val OWNCLOUD_CLIENT_ID = "web"
const val OWNCLOUD_CLIENT_SECRET = ""
const val OWNCLOUD_REDIRECT_SUFFIX = "/oidc-callback.html"
const val OWNCLOUD_OCIS_CONFIG_URL = "/.well-known/openid-configuration"