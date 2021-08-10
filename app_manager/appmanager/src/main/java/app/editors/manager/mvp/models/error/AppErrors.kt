package app.editors.manager.mvp.models.error

sealed class AppErrors(val message: String) {
    class HttpError(val errorMessage: String): AppErrors(errorMessage)
    class Unauthorized(val errorMessage: String): AppErrors(errorMessage)
}
