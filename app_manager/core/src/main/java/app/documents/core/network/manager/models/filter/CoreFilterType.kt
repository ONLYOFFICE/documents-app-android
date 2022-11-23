package app.documents.core.network.manager.models.filter

import app.documents.core.network.common.contracts.ApiContract

enum class CoreFilterType(val value: String) {
    Folders(ApiContract.Parameters.VAL_FILTER_BY_FOLDERS),
    Documents(ApiContract.Parameters.VAL_FILTER_BY_DOCUMENTS),
    Presentations(ApiContract.Parameters.VAL_FILTER_BY_PRESENTATIONS),
    Spreadsheets(ApiContract.Parameters.VAL_FILTER_BY_SPREADSHEETS),
    Images(ApiContract.Parameters.VAL_FILTER_BY_IMAGES),
    Media(ApiContract.Parameters.VAL_FILTER_BY_MEDIA),
    Archives(ApiContract.Parameters.VAL_FILTER_BY_ARCHIVE),
    All(ApiContract.Parameters.VAL_FILTER_BY_FILES),
    None(ApiContract.Parameters.VAL_FILTER_BY_NONE)
}