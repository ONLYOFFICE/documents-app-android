package app.documents.core.network.common.exceptions

import java.io.IOException

class NoConnectivityException : IOException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    override val message: String
        get() = "No connectivity exception"
}