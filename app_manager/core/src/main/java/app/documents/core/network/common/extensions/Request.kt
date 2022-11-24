package app.documents.core.network.common.extensions

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

suspend fun <R : BaseResponse> request(
    func: suspend () -> R,
    onSuccess: (R) -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val response: R = func.invoke()
        withContext(Dispatchers.Main) {
            response.checkStatusCode()
            onSuccess.invoke(response)
        }
    } catch (e: Throwable) {
        onError.invoke(e)
    }
}

suspend fun <R : BaseResponse, T> request(
    func: suspend () -> R,
    map: ((R) -> T),
    onSuccess: (T) -> Unit,
    onError: (Throwable) -> Unit,
) {
    try {
        val response: R = func.invoke()
        withContext(Dispatchers.Main) {
            response.checkStatusCode()
            map.invoke(response)?.let(onSuccess)
        }
    } catch (e: Throwable) {
        onError.invoke(e)
    }
}

suspend fun <R1: BaseResponse, R2: BaseResponse> requestZip(
    func1: suspend () -> R1,
    func2: suspend () -> R2,
    onSuccess: (R1, R2) -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val response1: R1 = func1.invoke()
        val response2: R2 = func2.invoke()
        withContext(Dispatchers.Main) {
            response1.checkStatusCode()
            response2.checkStatusCode()
            onSuccess.invoke(response1, response2)
        }
    } catch (e: Throwable) {
        onError.invoke(e)
    }
}

suspend fun <T, R> requestIterable(
    iterable: Iterable<T>,
    map: (T) -> R,
    onEach: (R) -> Unit,
    onFinish: ((Iterable<R>) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    try {
        val mappedList = mutableListOf<R>()
        iterable.forEach {
            val result = map.invoke(it)
            mappedList += result
            withContext(Dispatchers.Main) {
                onEach.invoke(result)
            }
        }
        withContext(Dispatchers.Main) {
            onFinish?.invoke(mappedList)
        }
    } catch (e: Throwable) {
        onError?.invoke(e)
    }
}

private val BaseResponse.httpExtension: HttpException
    get() = HttpException(
            Response.error<ResponseBody>(
                this.statusCode.toInt(),
                ResponseBody.create(MediaType.parse(ApiContract.VALUE_CONTENT_TYPE), status)
            )
        )

private fun BaseResponse.checkStatusCode() {
    if (statusCode.isNotEmpty()) {
        val code = statusCode.toInt()
        if (code >= ApiContract.HttpCodes.CLIENT_ERROR) throw httpExtension
    }
}