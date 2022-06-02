package app.editors.manager.mvp.models.filter

import android.graphics.drawable.Drawable
import app.editors.manager.R
import lib.toolkit.base.ui.adapters.holder.ViewType


sealed class Author : ViewType {

    class Group(
        val id: String,
        val title: String,
        override val viewType: Int = R.layout.list_author_group_item
    ) : Author()

    class User(
        val id: String,
        val name: String,
        val department: String,
        val avatarUrl: String,
        var avatar: Drawable? = null,
        override val viewType: Int = R.layout.list_author_user_item
    ) : Author()
}