package app.editors.manager.ui.adapters

import android.view.View
import app.editors.manager.mvp.models.filter.Author
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.ui.adapters.base.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.holders.AuthorViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.factory.inflate
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class AuthorAdapter(authorId: String?, clickListener: (FilterAuthor) -> Unit) :
    BaseViewTypeAdapter<Author>(AuthorHolderFactory(authorId, clickListener)) {

    override fun getItemViewType(position: Int): Int {
        return itemsList[position].viewType
    }

    companion object {
        const val PAYLOAD_AVATAR = "payload_avatar"
    }
}

class AuthorHolderFactory(
    private val authorId: String?,
    private val clickListener: (FilterAuthor) -> Unit
) : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<Author> {
        return AuthorViewHolder(view.inflate(type), authorId, clickListener)
    }
}