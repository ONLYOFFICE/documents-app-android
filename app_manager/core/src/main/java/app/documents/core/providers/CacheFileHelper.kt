package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import app.documents.core.network.manager.models.explorer.CloudFile
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.FileUtils
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException

interface CacheFileHelper {

    fun getDownloadResponse(cloudFile: CloudFile, token: String?): Single<Response<ResponseBody>>

    @SuppressLint("MissingPermission")
    fun getCachedFile(context: Context, cloudFile: CloudFile, accountName: String): Single<File> {
        return getDownloadResponse(
            cloudFile,
            AccountUtils.getToken(context, accountName)
        )
            .subscribeOn(Schedulers.io())
            .map { response ->
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    FileUtils.createCacheFile(context, cloudFile.title)?.also { file ->
                        FileUtils.writeFromResponseBody(
                            response = responseBody,
                            to = file.toUri(),
                            context = context
                        )
                    } ?: throw FileNotFoundException("Caching file error")
                } else throw HttpException(response)
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

}