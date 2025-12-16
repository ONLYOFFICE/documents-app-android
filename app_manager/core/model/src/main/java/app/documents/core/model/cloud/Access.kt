package app.documents.core.model.cloud

import kotlinx.serialization.Serializable

@Serializable
sealed class Access(val type: String, val code: Int) {

    data object None : Access(TYPE_NONE, CODE_NONE)
    data object ReadWrite : Access(TYPE_READ_WRITE, CODE_READ_WRITE)
    data object Read : Access(TYPE_READ, CODE_READ)
    data object Restrict : Access(TYPE_RESTRICT, CODE_RESTRICT)
    data object Review : Access(TYPE_REVIEW, CODE_REVIEW)
    data object Comment : Access(TYPE_COMMENT, CODE_COMMENT)
    data object CustomFilter : Access(TYPE_CUSTOM_FILTER, CODE_CUSTOM_FILTER)
    data object FormFiller : Access(TYPE_FILL_FORMS, CODE_FILL_FORMS)
    data object Editor : Access(TYPE_EDITOR, CODE_EDITOR)
    data object RoomManager : Access(TYPE_ROOM_MANAGER, CODE_ROOM_MANAGER)
    data object ContentCreator : Access(TYPE_CONTENT_CREATOR, CODE_CONTENT_CREATOR)

    val isEditable: Boolean
        get() = this in arrayOf(None, ReadWrite)

    companion object {

        private const val CODE_NONE = 0
        private const val CODE_READ_WRITE = 1
        private const val CODE_READ = 2
        private const val CODE_RESTRICT = 3
        private const val CODE_REVIEW = 5
        private const val CODE_COMMENT = 6
        private const val CODE_FILL_FORMS = 7
        private const val CODE_CUSTOM_FILTER = 8
        private const val CODE_ROOM_MANAGER = 9
        private const val CODE_EDITOR = 10
        private const val CODE_CONTENT_CREATOR = 11

        private const val TYPE_NONE = "None"
        private const val TYPE_READ_WRITE = "ReadWrite"
        private const val TYPE_READ = "Read"
        private const val TYPE_RESTRICT = "Restrict"
        private const val TYPE_REVIEW = "Review"
        private const val TYPE_COMMENT = "Comment"
        private const val TYPE_FILL_FORMS = "FillForms"
        private const val TYPE_CUSTOM_FILTER = "CustomFilter"
        private const val TYPE_ROOM_MANAGER = "RoomManager"
        private const val TYPE_EDITOR = "Editor"
        private const val TYPE_CONTENT_CREATOR = "ContentCreator"

        fun get(code: Int?): Access =
            when (code) {
                CODE_READ_WRITE -> ReadWrite
                CODE_READ -> Read
                CODE_RESTRICT -> Restrict
                CODE_REVIEW -> Review
                CODE_COMMENT -> Comment
                CODE_FILL_FORMS -> FormFiller
                CODE_CUSTOM_FILTER -> CustomFilter
                CODE_ROOM_MANAGER -> RoomManager
                CODE_EDITOR -> Editor
                CODE_CONTENT_CREATOR -> ContentCreator
                else -> None
            }

        fun get(type: String): Access =
            when (type) {
                TYPE_READ_WRITE -> ReadWrite
                TYPE_READ -> Read
                TYPE_RESTRICT -> Restrict
                TYPE_REVIEW -> Review
                TYPE_COMMENT -> Comment
                TYPE_FILL_FORMS -> FormFiller
                TYPE_CUSTOM_FILTER -> CustomFilter
                TYPE_ROOM_MANAGER -> RoomManager
                TYPE_EDITOR -> Editor
                TYPE_CONTENT_CREATOR -> ContentCreator
                else -> None
            }
    }
}