package app.documents.core.model.cloud

sealed class UserType {
    data object DocSpaceOwner : UserType()
    data object DocSpaceAdmin : UserType()
    data object RoomAdmin : UserType()
    data object User : UserType()
    data object Guest : UserType()
}