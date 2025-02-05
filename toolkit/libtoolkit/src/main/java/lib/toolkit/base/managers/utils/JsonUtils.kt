package lib.toolkit.base.managers.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.util.Locale

object JsonUtils {

    private const val FORMULAS_PATH = "formulas/"
    private const val JSON_EXTENSION = ".json"
    private const val DESC_SUFFIX = "_desc"

    private fun jsonFromAssets(context: Context, path: String): String {
        context.assets.open(path).use {
            return String(it.readBytes())
        }
    }

    inline fun <reified T> objectToJson(data: T): String {
        return Gson().toJson(data, object : TypeToken<T>() {}.type)
    }

    inline fun <reified T> objectToJsonExpose(data: T): String {
        return GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(data, object : TypeToken<T>() {}.type)
    }

    inline fun <reified T> jsonToObject(json: String, clazz: Class<T>): T {
        return Gson().fromJson(json, clazz)
    }

    inline fun <reified T> jsonToObjectExpose(json: String, clazz: Class<T>): T? {
        return GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .fromJson(json, clazz)
    }

    fun getFormulasJson(context: Context, lang: String = Locale.ENGLISH.language): JSONObject? {
        return try {
            JSONObject(jsonFromAssets(context, FORMULAS_PATH + lang + JSON_EXTENSION))
        }
        catch (exception: FileNotFoundException) {
            JSONObject(jsonFromAssets(context, FORMULAS_PATH + Locale.ENGLISH.language + JSON_EXTENSION))

        }
        catch (exception: JSONException) {
            return null
        }
    }

    fun getFormulasDescJson(context: Context, lang: String = Locale.ENGLISH.language): JSONObject? {
        return try {
            JSONObject(jsonFromAssets(context, FORMULAS_PATH + lang + DESC_SUFFIX + JSON_EXTENSION))
        }
        catch (exception: FileNotFoundException) {
            JSONObject(jsonFromAssets(context, FORMULAS_PATH + Locale.ENGLISH.language + DESC_SUFFIX + JSON_EXTENSION))
        }
        catch (exception: JSONException) {
            return null
        }
    }

}