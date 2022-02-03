package app.documents.core.network

object ApiContract {

    /*
     * Api constants
     * */
    const val API_VERSION = "2.0"
    const val SCHEME_HTTPS = "https://"
    const val SCHEME_HTTP = "http://"
    const val API_SUBDOMAIN = "api-system"
    const val PERSONAL_SUBDOMAIN = "personal"
    const val DEFAULT_HOST = "onlyoffice.com"
    const val PERSONAL_HOST = "$PERSONAL_SUBDOMAIN.$DEFAULT_HOST"
    const val RESPONSE_FORMAT = ".json"
    const val COOKIE_HEADER = "asc_auth_key="

    /*
     * Portals versions
     * */
    const val PORTAL_VERSION_10 = "10.0.0.297"

    /*
     * Headers
     * */
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_HOST = "Host"
    const val HEADER_CONTENT_TYPE = "Content-OperationType"
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_AGENT = "User-Agent"
    const val HEADER_CACHE = "Cache-Control"

    const val VALUE_CONTENT_TYPE = "application/json"
    const val VALUE_ACCEPT = "application/json"
    const val VALUE_CACHE = "no-cache"

    const val DOWNLOAD_ZIP_NAME = "download.tgz"

    object Social {
        const val TWITTER = "twitter"
        const val FACEBOOK = "facebook"
        const val GOOGLE = "google"
    }

    object HttpCodes {
        const val NONE = -1
        const val SUCCESS = 200
        const val REDIRECTION = 300
        const val CLIENT_ERROR = 400
        const val CLIENT_UNAUTHORIZED = 401
        const val CLIENT_PAYMENT_REQUIRED = 402
        const val CLIENT_FORBIDDEN = 403
        const val CLIENT_NOT_FOUND = 404
        const val SERVER_ERROR = 500
    }

    object Errors {
        const val AUTH = "User authentication failed"
        const val AUTH_TOO_MANY_ATTEMPTS = "Login Fail. Too many attempts"
        const val PORTAL_EXIST = "Portal already exist"
        const val SMS_TO_MANY = "You have sent too many text messages"
        const val DISK_SPACE_QUOTA = "Disk space quota exceeded"
        const val EXCEED_FILE_SIZE_100 = "Exceeds the maximum file size (100MB)"
        const val EXCEED_FILE_SIZE_25 = "Exceeds the maximum file size (25MB)"
    }

    object ShareType {
        const val NONE = "None"
        const val READ_WRITE = "ReadWrite"
        const val READ = "Read"
        const val RESTRICT = "Restrict"
        const val VARIES = "Varies"
        const val REVIEW = "Review"
        const val COMMENT = "Comment"
        const val FILL_FORMS = "FillForms"

        fun getCode(type: String?): Int {
            return when (type) {
                NONE -> ShareCode.NONE
                READ_WRITE -> ShareCode.READ_WRITE
                READ -> ShareCode.READ
                RESTRICT -> ShareCode.RESTRICT
                VARIES -> ShareCode.VARIES
                REVIEW -> ShareCode.REVIEW
                COMMENT -> ShareCode.COMMENT
                FILL_FORMS -> ShareCode.FILL_FORMS
                else -> ShareCode.NONE
            }
        }
    }

    object ShareCode {

        const val NONE = 0
        const val READ_WRITE = 1
        const val READ = 2
        const val RESTRICT = 3
        const val VARIES = 4
        const val REVIEW = 5
        const val COMMENT = 6
        const val FILL_FORMS = 7

        fun getType(code: Int): String {
            return when (code) {
                NONE -> ShareType.NONE
                READ_WRITE -> ShareType.READ_WRITE
                READ -> ShareType.READ
                RESTRICT -> ShareType.RESTRICT
                VARIES -> ShareType.VARIES
                REVIEW -> ShareType.REVIEW
                else -> ShareType.NONE
            }
        }
    }

    object Parameters {
        const val ARG_ACTION = "action"
        const val ARG_COUNT = "count"
        const val ARG_START_INDEX = "startIndex"
        const val ARG_SORT_BY = "sortBy"
        const val ARG_SORT_ORDER = "sortOrder"
        const val ARG_FILTER_BY = "filterBy"
        const val ARG_FILTER_OP = "filterOp"
        const val ARG_FILTER_VALUE = "filterValue"
        const val ARG_FILTER_SUBFOLDERS = "withSubfolders"
        const val ARG_UPDATED_SINCE = "updatedSince"
        const val VAL_ACTION_VIEW = "view"
        const val VAL_SORT_ORDER_ASC = "ascending"
        const val VAL_SORT_ORDER_DESC = "descending"
        const val VAL_FILTER_OP_CONTAINS = "contains"
        const val VAL_FILTER_OP_EQUALS = "equals"
        const val VAL_FILTER_OP_STARTS_WITH = "startsWith"
        const val VAL_FILTER_OP_PRESENT = "present"
        const val VAL_FILTER_BY = "title"
        const val VAL_FILTER_SUBFOLDERS = "true"
        const val VAL_SORT_BY_UPDATED = "DateAndTime"
        const val VAL_SORT_BY_CREATED = "created"
        const val VAL_SORT_BY_TITLE = "title"
        const val VAL_SORT_BY_TYPE = "type"
        const val VAL_SORT_BY_SIZE = "size"
        const val VAL_SORT_BY_OWNER = "Author"
        const val VAL_SORT_BY_NAME = "name"
        const val VAL_SORT_BY_DISPLAY_NAME = "displayName"
    }

    object SectionType {
        const val UNKNOWN = 0
        const val CLOUD_COMMON = 1
        const val CLOUD_BUNCH = 2
        const val CLOUD_TRASH = 3
        const val CLOUD_USER = 5
        const val CLOUD_SHARE = 6
        const val CLOUD_PROJECTS = 8
        const val DEVICE_DOCUMENTS = 9
        const val CLOUD_FAVORITES = 10
        const val CLOUD_RECENT = 11
    }

    object Modules {
        const val FILTER_TYPE_HEADER = "filterType"
        const val FILTER_TYPE_VALUE = 2

        const val FLAG_SUBFOLDERS = "withsubfolders"
        const val FLAG_TRASH = "withoutTrash"
        const val FLAG_ADDFOLDERS = "withoutAdditionalFolder"
    }

    object Operation {
        const val SKIP = 0
        const val OVERWRITE = 1
        const val DUPLICATE = 2
    }

    object Storage {
        const val BOXNET = "Box"
        const val DROPBOX = "DropboxV2"
        const val GOOGLEDRIVE = "GoogleDrive"
        const val ONEDRIVE = "OneDrive"
        const val SKYDRIVE = "SkyDrive"
        const val GOOGLE = "Google"
        const val SHAREPOINT = "SharePoint"
        const val YANDEX = "Yandex"
        const val OWNCLOUD = "OwnCloud"
        const val NEXTCLOUD = "Nextcloud"
        const val KDRIVE = "KDrive"
        const val WEBDAV = "WebDav"
    }

    object FileStatus {
        const val NONE = 0x0
        const val IS_EDITING = 0x1
        const val IS_NEW = 0x2
        const val IS_CONVERTING = 0x4
        const val IS_ORIGINAL = 0x8
        const val BACKUP = 0x10
        const val FAVORITE = 0x20
    }

    object Extension {
        const val DOCX = "DOCX"
        const val XLSX = "XLSX"
        const val PPTX = "PPTX"
    }
}