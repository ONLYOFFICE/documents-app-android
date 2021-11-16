package app.editors.manager.googledrive.googledrive.login


sealed class GoogleDriveResponse {
    class Success(val response: Any) : GoogleDriveResponse()
    class Error(val error: Throwable) : GoogleDriveResponse()
}

interface IGoogleDriveLoginServiceProvider {



}