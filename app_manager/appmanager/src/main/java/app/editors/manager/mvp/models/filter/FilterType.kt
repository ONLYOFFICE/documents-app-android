package app.editors.manager.mvp.models.filter

import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.ui.views.custom.ChipItem

enum class FilterType(override val chipTitle: Int, val filterVal: String) : ChipItem {
    Folders(R.string.list_headers_folder, ApiContract.Parameters.VAL_FILTER_BY_FOLDERS),
    Documents(R.string.filter_type_documents, ApiContract.Parameters.VAL_FILTER_BY_DOCUMENTS),
    Presentations(R.string.filter_type_presentations, ApiContract.Parameters.VAL_FILTER_BY_PRESENTATIONS),
    Spreadsheets(R.string.filter_type_spreadsheets, ApiContract.Parameters.VAL_FILTER_BY_SPREADSHEETS),
    Images(R.string.filter_type_images, ApiContract.Parameters.VAL_FILTER_BY_IMAGES),
    Media(R.string.filter_type_media, ApiContract.Parameters.VAL_FILTER_BY_MEDIA),
    Archives(R.string.filter_type_archives, ApiContract.Parameters.VAL_FILTER_BY_ARCHIVE),
    All(R.string.filter_type_all, ApiContract.Parameters.VAL_FILTER_BY_FILES),
    None(-1, ApiContract.Parameters.VAL_FILTER_BY_NONE);

    override val withOption: Boolean = false
    override var option: Any? = null

    companion object {

        val allTypes: List<FilterType>
            get() = listOf(Folders, Documents, Presentations, Spreadsheets, Images, Media, Archives, All)
    }
}