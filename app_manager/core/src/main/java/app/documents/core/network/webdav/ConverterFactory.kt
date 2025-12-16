@file:Suppress("DEPRECATION")

package app.documents.core.network.webdav

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.lang.reflect.Type

class ConverterFactory : Converter.Factory() {
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Json

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Xml

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        for (annotation in annotations) {
            if (annotation.annotationClass == Xml::class) {
                return SimpleXmlConverterFactory.create().responseBodyConverter(type, annotations, retrofit)
            } else if (annotation.annotationClass == Json::class) {
                return jsonConverter.responseBodyConverter(type, annotations, retrofit)
            }
        }
        return jsonConverter.responseBodyConverter(type, annotations, retrofit)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        for (annotation in methodAnnotations) {
            if (annotation.annotationClass == Xml::class) {
                return SimpleXmlConverterFactory.create()
                    .requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
            } else if (annotation.annotationClass == Json::class) {
                return jsonConverter.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
            }
        }
        return jsonConverter.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    @Suppress("JSON_FORMAT_REDUNDANT")
    private val jsonConverter: Converter.Factory
        get() = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory("application/json".toMediaType())
}