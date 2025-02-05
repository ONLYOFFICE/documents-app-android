package app.documents.core.model.cloud

sealed class UserType {
    data object Owner : UserType()
    data object Admin : UserType()
    data object RoomAdmin : UserType()
    data object User : UserType()
    data object Guest : UserType()
}