package lib.toolkit.base.managers.utils

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts

object EditorsContract {

    const val KEY_HELP_URL = "KEY_HELP_URL"
    const val KEY_DOC_SERVER = "KEY_DOC_SERVER"
    const val KEY_PDF = "KEY_PDF"
    const val KEY_EDIT_TYPE = "KEY_EDIT_TYPE"
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
}

enum class EditorsType {
    DOCS, CELLS, PRESENTATION, PDF
}

enum class EditType {
    EDIT, FILL, VIEW
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