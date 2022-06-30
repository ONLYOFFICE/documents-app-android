package app.editors.manager.mvp.models.filter

import app.documents.core.network.ApiContract
import app.editors.manager.R

enum class FilterType(val checkedId: Int, val filterVal: String) {
    Folders(R.id.folders, ApiContract.Parameters.VAL_FILTER_BY_FOLDERS),
    Documents(R.id.documents, ApiContract.Parameters.VAL_FILTER_BY_DOCUMENTS),
    Presentations(R.id.presentations, ApiContract.Parameters.VAL_FILTER_BY_PRESENTATIONS),
    Spreadsheets(R.id.spreadsheets, ApiContract.Parameters.VAL_FILTER_BY_SPREADSHEETS),
    Images(R.id.images, ApiContract.Parameters.VAL_FILTER_BY_IMAGES),
    Media(R.id.media, ApiContract.Parameters.VAL_FILTER_BY_MEDIA),
    Archives(R.id.archives, ApiContract.Parameters.VAL_FILTER_BY_ARCHIVE),
    All(R.id.all, ApiContract.Parameters.VAL_FILTER_BY_FILES),
    None(-1, ApiContract.Parameters.VAL_FILTER_BY_NONE)
}