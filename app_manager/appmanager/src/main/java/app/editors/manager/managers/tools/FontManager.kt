package app.editors.manager.managers.tools

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.delay
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.FileUtils
import java.io.File

class FontManager(private val resourcesProvider: ResourcesProvider){

    private val fontsDir: String
        get() = FileUtils.getFontsDir(resourcesProvider.context)

    fun getFonts(): List<File> {
        return File(fontsDir).listFiles()?.toList().orEmpty()
    }

    fun clearFonts(){
        val fonts = File(fontsDir)
        if (fonts.exists()) fonts.deleteRecursively()
    }

    fun deleteFont(font: File){
        val file = File("$fontsDir/${font.name}")
        if (file.exists()) file.delete()
    }

    suspend fun addFonts(
        fonts: List<Uri>,
        onProgress: suspend (Int) -> Unit,
        onError: suspend (Exception) -> Unit
    ){
        fonts.forEachIndexed { index, font ->
            try {
                val filename = DocumentFile.fromSingleUri(resourcesProvider.context, font)?.name.orEmpty()
                val file = File(fontsDir, filename)
                file.createNewFile()
                file.writeBytes(
                    resourcesProvider.context
                        .contentResolver
                        .openInputStream(font)?.readBytes() ?: ByteArray(0)
                )
                onProgress(((index + 1).toFloat() / fonts.size * 100).toInt())
                delay(250)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}