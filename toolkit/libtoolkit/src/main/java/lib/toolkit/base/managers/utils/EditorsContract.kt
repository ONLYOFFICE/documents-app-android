package lib.toolkit.base.managers.utils

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import java.io.Serializable

object EditorsContract {

    const val KEY_HELP_URL = "KEY_HELP_URL"
    const val KEY_DOC_SERVER = "KEY_DOC_SERVER"
    const val KEY_EDIT_TYPE = "KEY_EDIT_TYPE"
    const val KEY_EDIT_ACCESS = "KEY_EDIT_ACCESS"
    const val KEY_KEEP_SCREEN_ON = "KEY_KEEP_SCREEN_ON"

    const val EDITOR_DOCUMENTS = "lib.editors.gdocs.ui.activities.DocsActivity"
    const val EDITOR_CELLS = "lib.editors.gcells.ui.activities.CellsActivity"
    const val EDITOR_SLIDES = "lib.editors.gslides.ui.activities.SlidesActivity"
    const val PDF = "lib.editors.gbase.ui.activities.PdfActivity"

    const val EXTRA_IS_MODIFIED = "EXTRA_IS_MODIFIED"
    const val EXTRA_IS_REFRESH = "EXTRA_IS_REFRESH"
    const val EXTRA_IS_SEND_FORM = "EXTRA_IS_SEND_FORM"
    const val EXTRA_FILL_SESSION = "EXTRA_FILL_SESSION"

    const val RESULT_FAILED_OPEN = 4000

    const val INTERNAL_EDIT_ACCESS_EDIT = 0
    const val INTERNAL_EDIT_ACCESS_READ = 1
    const val INTERNAL_EDIT_ACCESS_RESTRICT = 2
    const val INTERNAL_EDIT_ACCESS_COMMENT = 3
    const val INTERNAL_EDIT_ACCESS_FILLING_FORMS = 4
    const val INTERNAL_EDIT_ACCESS_TRACKED_CHANGES = 5
    const val INTERNAL_EDIT_ACCESS_CUSTOM_FILTER = 6
}

enum class EditorsType {
    DOCS, CELLS, PRESENTATION, PDF
}

sealed class EditType : Serializable {
    class Edit(val initialView: Boolean = true) : EditType()
    class Fill(val startFilling: Boolean = false) : EditType()
    class View : EditType()

    companion object {

        fun from(mode: String): EditType {
            return when (mode) {
                "view" -> View()
                "fill" -> Fill()
                else -> Edit()
            }
        }
    }
}

class EditorsForResult(
    activityResultRegistry: ActivityResultRegistry,
    private val callback: (result: ActivityResult) -> Unit,
    private val intent: Intent
) {

    private val launchActivity: ActivityResultLauncher<Intent> =
        activityResultRegistry.register("ActivityForResult", ActivityResultContracts.StartActivityForResult()) { result ->
            callback.invoke(result)
        }

    fun show() {
        launchActivity.launch(intent)
    }

}