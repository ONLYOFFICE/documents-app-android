package app.documents.core.utils

import android.text.Html
import app.documents.core.model.login.User

val User.displayNameFromHtml: String
    get() = Html.fromHtml(displayName, Html.FROM_HTML_MODE_LEGACY).toString()