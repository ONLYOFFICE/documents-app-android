package app.documents.core.providers

class ProviderError(message: String?) : Exception(message) {

    companion object {
        const val FORBIDDEN = "Forbidden symbol"
        const val INTERRUPT = "Interrupt"
        const val FILE_EXIST = "Exist"
        const val UNSUPPORTED_PATH = "Unsupported path"
        const val ERROR_CREATE_LOCAL = "Error create"

        @JvmStatic
        fun throwForbiddenError(): ProviderError {
            return ProviderError(FORBIDDEN)
        }

        @JvmStatic
        fun throwInterruptException(): ProviderError {
            return ProviderError(INTERRUPT)
        }

        @JvmStatic
        fun throwExistException(): ProviderError {
            return ProviderError(FILE_EXIST)
        }

        @JvmStatic
        fun throwUnsupportedException(): ProviderError {
            return ProviderError(UNSUPPORTED_PATH)
        }

        @JvmStatic
        fun throwErrorCreate(): ProviderError {
            return ProviderError(ERROR_CREATE_LOCAL)
        }
    }
}