package lib.toolkit.base.managers.utils

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts

object EditorsContract {

    const val KEY_HELP_URL = "KEY_HELP_URL"
    const val KEY_NEW_FILE = "KEY_NEW_FILE"

    const val EDITOR_DOCUMENTS = "lib.editors.gdocs.ui.activities.DocsActivity"
    const val EDITOR_CELLS = "lib.editors.gcells.ui.activities.CellsActivity"
    const val EDITOR_SLIDES = "lib.editors.gslides.ui.activities.SlidesActivity"
    const val PDF = "lib.editors.gbase.ui.activities.PdfActivity"

}

enum class EditorsType {
    DOCS, CELLS, PRESENTATION, PDF
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