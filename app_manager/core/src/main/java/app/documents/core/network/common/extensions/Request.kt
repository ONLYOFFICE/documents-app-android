package app.documents.core.network.common

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.Base
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

suspend fun <R : Base> request(
    func: suspend () -> R,
    onSuccess: (R) -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val response: R = func.invoke()
        withContext(Dispatchers.Main) {
            if (response.statusCode.toInt() != ApiContract.HttpCodes.SUCCESS) throw response.httpExtension
            onSuccess.invoke(response)
        }
    } catch (e: Throwable) {
        onError.invoke(e)
    }
}

suspend fun <R : Base, T> request(
    func: suspend () -> R,
    map: ((R) -> T),
    onSuccess: (T) -> Unit,
    onError: (Throwable) -> Unit,
) {
    try {
        val response: R = func.invoke()
        withContext(Dispatchers.Main) {
            if (response.statusCode.toInt() != ApiContract.HttpCodes.SUCCESS) throw response.httpExtension
            map.invoke(response)?.let(onSuccess)
        }
    } catch (e: Throwable) {
        onError.invoke(e)
    }
}

suspend fun <R1: Base, R2: Base> requestZip(
    func1: suspend () -> R1,
    func2: suspend () -> R2,
    onSuccess: (R1, R2) -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val response1: R1 = func1.invoke()
        val response2: R2 = func2.invoke()
        withContext(Dispatchers.Main) {
            if (response1.statusCode.toInt() != ApiContract.HttpCodes.SUCCESS) throw response1.httpExtension
            if (response2.statusCode.toInt() != ApiContract.HttpCodes.SUCCESS) throw response2.httpExtension
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

private val Base.httpExtension: HttpException
    get() = HttpException(
            Response.error<ResponseBody>(
                this.statusCode.toInt(),
                ResponseBody.create(MediaType.parse(ApiContract.VALUE_CONTENT_TYPE), status)
            )
        )