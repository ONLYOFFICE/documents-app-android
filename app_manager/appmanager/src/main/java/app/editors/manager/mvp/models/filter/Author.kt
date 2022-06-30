package app.editors.manager.mvp.models.filter

import android.graphics.drawable.Drawable
import app.editors.manager.R
import lib.toolkit.base.ui.adapters.holder.ViewType


sealed class Author(open val id: String, open val name: String) : ViewType {

    class Group(
        override val id: String,
        override val name: String,
        override val viewType: Int = R.layout.list_author_group_item
    ) : Author(id, name)

    class User(
        override val id: String,
        override val name: String,
        val department: String,
        val avatarUrl: String,
        var avatar: Drawable? = null,
        override val viewType: Int = R.layout.list_author_user_item
    ) : Author(id, name)
}