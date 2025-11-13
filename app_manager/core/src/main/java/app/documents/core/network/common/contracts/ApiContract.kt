package app.documents.core.network.common.contracts

import lib.toolkit.base.BuildConfig


object ApiContract {

    /*
     * Api constants
     * */
    const val API_VERSION = "2.0"
    const val SCHEME_HTTPS = "https://"
    const val SCHEME_HTTP = "http://"
    const val API_SUBDOMAIN = "api-system"
    const val PERSONAL_SUBDOMAIN = BuildConfig.SUBDOMAIN
    const val DEFAULT_HOST = BuildConfig.DEFAULT_HOST
    const val DEFAULT_INFO_HOST = BuildConfig.DEFAULT_INFO_HOST
    const val PERSONAL_HOST = "$PERSONAL_SUBDOMAIN.$DEFAULT_HOST"
    const val PERSONAL_INFO_HOST = "$PERSONAL_SUBDOMAIN.$DEFAULT_INFO_HOST"
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
    const val HEADER_CONTENT_OPERATION_TYPE = "Content-OperationType"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_AGENT = "User-Agent"
    const val HEADER_CACHE = "Cache-Control"

    const val VALUE_CONTENT_TYPE = "application/json"
    const val VALUE_ACCEPT = "application/json"
    const val VALUE_CACHE = "no-cache"

    const val DOWNLOAD_ZIP_NAME = "download.zip"

    object Social {
        const val TWITTER = "twitter"
        const val FACEBOOK = "facebook"
        const val GOOGLE = "google"
    }

    object Modules {
        const val FILTER_TYPE_HEADER = "filterType"
        const val FILTER_TYPE_VALUE = 2

        const val FLAG_SUBFOLDERS = "withsubfolders"
        const val FLAG_TRASH = "withoutTrash"
        const val FLAG_ADDFOLDERS = "withoutAdditionalFolder"

        const val PROJECT_ID = "1e044602-43b5-4d79-82f3-fd6208a11960"
        const val CRMP_ID = "6743007C-6F95-4d20-8C88-A8601CE5E76D"
        const val DOCUMENTS_ID = "E67BE73D-F9AE-4ce1-8FEC-1880CB518CB4"
        const val PEOPLE_ID = "F4D98AFD-D336-4332-8778-3C6945C81EA0"
        const val MAIL_ID = "2A923037-8B2D-487b-9A22-5AC0918ACF3F"
        const val CALENDAR_ID = "32D24CB5-7ECE-4606-9C94-19216BA42086"
        const val BIRTHDAYS_ID = "37620AE5-C40B-45ce-855A-39DD7D76A1FA"
        const val TALK_ID = "BF88953E-3C43-4850-A3FB-B1E43AD53A3E"
        const val VOIP_ID = "46CFA73A-F320-46CF-8D5B-CD82E1D67F26"
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
        const val UNSUPPORTED_MEDIA_TYPE = 415
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
        const val STORAGE_NOT_AVAILABLE = "The content of third party folder are not available. Try to reconnect the account"
        const val PINNED_ROOM_LIMIT = "You can't pin a room"
        const val FILLING_FORM_ROOM_UPLOAD = "Please try to upload the ONLYOFFICE PDF form"
        const val EXCEED_ROOM_SPACE_QUOTA = "Room space quota exceeded"
    }

    object ActivationStatus {
        const val ACTIVATE = 1
        const val PENDING = 2
    }

    object EmployeeStatus {
        const val ACTIVE = 1
        const val TERMINATED = 2
        const val PENDING = 4
    }

    object ThumbnailStatus {
        const val WAITING = 0
        const val CREATED = 1
        const val ERROR = 2
        const val NOT_REQUIRED = 3
        const val CREATING = 4
    }

    object Parameters {
        const val ARG_ACTION = "action"
        const val ARG_COUNT = "count"
        const val ARG_START_INDEX = "startIndex"
        const val ARG_SORT_BY = "sortBy"
        const val ARG_AREA = "area"
        const val ARG_INVITER_ID = "inviterId"
        const val ARG_SORT_ORDER = "sortOrder"
        const val ARG_FILTER_BY = "filterBy"
        const val ARG_FILTER_OP = "filterOp"
        const val ARG_FILTER_VALUE = "filterValue"
        const val ARG_FILTER_BY_TYPE = "filterType"
        const val ARG_FILTER_BY_TYPE_ROOM = "type"
        const val ARG_FILTER_BY_PROVIDER_ROOM = "provider"
        const val ARG_FILTER_BY_TAG_ROOM = "tags"
        const val ARG_FILTER_BY_SUBJECT_ID = "subjectId"
        const val ARG_FILTER_BY_AUTHOR = "userIdOrGroupId"
        const val ARG_FILTER_SUBFOLDERS = "withSubfolders"
        const val ARG_FILTER_LOCATION = "location"
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
        const val VAL_FILTER_BY_FOLDERS = "FoldersOnly"
        const val VAL_FILTER_BY_DOCUMENTS = "DocumentsOnly"
        const val VAL_FILTER_BY_PDF_DOCUMENTS = "Pdf"
        const val VAL_FILTER_BY_PDF_FORMS = "PdfForm"
        const val VAL_FILTER_BY_PRESENTATIONS = "PresentationsOnly"
        const val VAL_FILTER_BY_SPREADSHEETS = "SpreadsheetsOnly"
        const val VAL_FILTER_BY_IMAGES = "ImagesOnly"
        const val VAL_FILTER_BY_MEDIA = "MediaOnly"
        const val VAL_FILTER_BY_ARCHIVE = "ArchiveOnly"
        const val VAL_FILTER_BY_FILES = "FilesOnly"
        const val VAL_FILTER_BY_NONE = "None"
        const val VAL_SORT_BY_UPDATED = "DateAndTime"
        const val VAL_SORT_BY_CREATED = "created"
        const val VAL_SORT_BY_TITLE = "AZ"
        const val VAL_SORT_BY_TYPE = "type"
        const val VAL_SORT_BY_SIZE = "size"
        const val VAL_SORT_BY_OWNER = "Author"
        const val VAL_SORT_BY_NAME = "name"
        const val VAL_SORT_BY_FIRST_NAME = "firstname"
        const val VAL_SORT_BY_DISPLAY_NAME = "displayName"
        const val VAL_SORT_BY_ROOM_TYPE = "roomType"
        const val VAL_SORT_BY_TAGS = "Tags"
        const val VAL_AREA_GUESTS = "guests"
        const val VAL_AREA_PEOPLE = "people"
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
        const val CLOUD_PRIVATE_ROOM = 13
        const val CLOUD_VIRTUAL_ROOM = 14
        const val CLOUD_ARCHIVE_ROOM = 20
        const val EDITING_ROOM = 16
        const val CUSTOM_ROOM = 19
        const val THIRDPARTY_BACKUP = 21
        const val PUBLIC_ROOM = 22
        const val READY_FORM_FOLDER = 25
        const val IN_PROCESS_FORM_FOLDER = 26
        const val FORM_FILLING_FOLDER_DONE = 27
        const val FORM_FILLING_FOLDER_IN_PROGRESS = 28
        const val VIRTUAL_DATA_ROOM = 29
        const val ROOM_TEMPLATES_FOLDER = 30

        const val WEB_DAV = 100
        const val GOOGLE_DRIVE = 110
        const val DROPBOX = 111
        const val ONEDRIVE = 112

        const val LOCAL_RECENT = 200

        fun isRoom(type: Int): Boolean = type == 14
        fun isArchive(type: Int): Boolean = type == CLOUD_ARCHIVE_ROOM
        fun isTemplates(type: Int?): Boolean = type == ROOM_TEMPLATES_FOLDER

        fun shouldShowShareBadge(type: Int): Boolean {
            return type == EDITING_ROOM || type == CUSTOM_ROOM || type == VIRTUAL_DATA_ROOM
        }
    }

    sealed class Section(val type: Int) {
        object Common : Section(SectionType.CLOUD_COMMON)
        object Trash : Section(SectionType.CLOUD_TRASH)
        object User : Section(SectionType.CLOUD_USER)
        object Share : Section(SectionType.CLOUD_SHARE)
        object Projects : Section(SectionType.CLOUD_PROJECTS)
        object Device : Section(SectionType.DEVICE_DOCUMENTS)
        object Favorites : Section(SectionType.CLOUD_FAVORITES)
        object Recent : Section(SectionType.CLOUD_RECENT)
        object LocalRecent : Section(SectionType.LOCAL_RECENT)
        object Webdav : Section(SectionType.WEB_DAV)

        sealed class Room(type: Int) : Section(type) {
            object Private : Room(SectionType.CLOUD_PRIVATE_ROOM)
            object Virtual : Room(SectionType.CLOUD_VIRTUAL_ROOM)
            object Archive : Room(SectionType.CLOUD_ARCHIVE_ROOM)
            object Templates : Room(SectionType.ROOM_TEMPLATES_FOLDER)
        }

        sealed class Storage(type: Int) : Section(type) {
            object GoogleDrive : Storage(SectionType.GOOGLE_DRIVE)
            object Dropbox : Storage(SectionType.DROPBOX)
            object OneDrive : Storage(SectionType.ONEDRIVE)
        }

        val isRoom: Boolean get() = this is Room
        val isTemplates: Boolean get() = this is Room.Templates
        val isUser: Boolean get() = this is User
        val isArchive: Boolean get() = this == Room.Archive
        val isLocal: Boolean get() = this in listOf(Device, Recent, LocalRecent)
        val isLocalRecent: Boolean get() = this == LocalRecent
        val isDevice: Boolean get() = this == Device
        val isTrash: Boolean get() = this == Trash
        val isShare: Boolean get() = this == Share
        val isStorage: Boolean get() = this is Storage
        val isWebdav: Boolean get() = this == Webdav

        companion object {

            fun getSection(type: Int): Section =
                when (type) {
                    SectionType.CLOUD_TRASH -> Trash
                    SectionType.CLOUD_USER -> User
                    SectionType.CLOUD_SHARE -> Share
                    SectionType.CLOUD_PROJECTS -> Projects
                    SectionType.DEVICE_DOCUMENTS -> Device
                    SectionType.CLOUD_FAVORITES -> Favorites
                    SectionType.CLOUD_RECENT -> Recent
                    SectionType.WEB_DAV -> Webdav
                    SectionType.ROOM_TEMPLATES_FOLDER -> Room.Templates
                    SectionType.CLOUD_PRIVATE_ROOM -> Room.Private
                    SectionType.CLOUD_VIRTUAL_ROOM -> Room.Virtual
                    SectionType.CLOUD_ARCHIVE_ROOM -> Room.Archive
                    SectionType.LOCAL_RECENT -> LocalRecent
                    SectionType.GOOGLE_DRIVE -> Storage.GoogleDrive
                    SectionType.DROPBOX -> Storage.Dropbox
                    SectionType.ONEDRIVE -> Storage.OneDrive
                    else -> Common
                }
        }
    }



    sealed class FormFillingStatus(val type: Int){

        data object None : FormFillingStatus(type = NONE)
        data object Draft : FormFillingStatus(type = DRAFT_TYPE)
        data object YourTurn : FormFillingStatus(type = YOUR_TURN_TYPE)
        data object InProgress : FormFillingStatus(type = IN_PROGRESS_TYPE)
        data object Complete : FormFillingStatus(type = COMPLETE_TYPE)
        data object Stopped : FormFillingStatus(type = STOPPED_TYPE)

        companion object {
            fun fromType(statusType: Int): FormFillingStatus =
                when(statusType){
                    DRAFT_TYPE -> Draft
                    YOUR_TURN_TYPE -> YourTurn
                    IN_PROGRESS_TYPE -> InProgress
                    COMPLETE_TYPE -> Complete
                    STOPPED_TYPE -> Stopped
                    else -> None
                }

            private const val NONE = 0
            private const val DRAFT_TYPE = 1
            private const val YOUR_TURN_TYPE = 2
            private const val IN_PROGRESS_TYPE = 3
            private const val COMPLETE_TYPE = 4
            private const val STOPPED_TYPE = 5
        }
    }

    object RoomType {
        const val FILL_FORMS_ROOM = 1
        const val COLLABORATION_ROOM = 2
        const val CUSTOM_ROOM = 5
        const val PUBLIC_ROOM = 6
        const val VIRTUAL_ROOM = 8

        fun hasExternalLink(roomType: Int?): Boolean =
            roomType in arrayOf(PUBLIC_ROOM, FILL_FORMS_ROOM, CUSTOM_ROOM)

        fun toObj(type: Int): RoomTypeObj {
            return when (type) {
                FILL_FORMS_ROOM -> RoomTypeObj.FillingForms
                COLLABORATION_ROOM -> RoomTypeObj.Collaboration
                CUSTOM_ROOM -> RoomTypeObj.Custom
                PUBLIC_ROOM -> RoomTypeObj.Public
                VIRTUAL_ROOM -> RoomTypeObj.VDR
                else -> error("invalid room type")
            }
        }
    }

    // rename to RoomType after refactoring
    enum class RoomTypeObj {
        Public, VDR, FillingForms, Collaboration, Custom
    }

    object SectionPath {
        const val MY = "@my"
        const val COMMON = "@common"
        const val SHARED = "@share"
        const val PROJECTS = "@projects"
        const val TRASH = "@trash"
        const val FAVORITES = "@favorites"
        const val ROOMS = "rooms"
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
        const val SHARED_BY_LINK = 0x256
    }

    object Extension {
        const val DOCX = "DOCX"
        const val XLSX = "XLSX"
        const val PPTX = "PPTX"
    }
}