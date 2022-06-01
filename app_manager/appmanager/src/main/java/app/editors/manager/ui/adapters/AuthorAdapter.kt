package app.editors.manager.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.databinding.ListAuthorGroupItemBinding
import app.editors.manager.databinding.ListAuthorUserItemBinding
import app.editors.manager.managers.utils.GlideUtils.loadAvatar
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.fragments.filter.Author
import lib.toolkit.base.managers.extensions.inflate

class AuthorAdapter(private val authorId: String?, private val clickListener: (FilterAuthor) -> Unit) :
    BaseAdapter<Author>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AuthorViewHolder(parent.inflate(viewType))
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is Author.Group -> R.layout.list_author_group_item
            is Author.User -> R.layout.list_author_user_item
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AuthorViewHolder) {
            holder.bind(itemList[position], authorId)
        }
        holder.itemView.setOnClickListener {
            when (val author = itemList[position]) {
                is Author.User -> {
                    clickListener(FilterAuthor(id = author.id, name = author.name, isGroup = false))
                }
                is Author.Group -> {
                    clickListener(FilterAuthor(id = author.id, name = author.title, isGroup = true))
                }
            }
        }
    }

    private class AuthorViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(author: Author, authorId: String?) {
            when (author) {
                is Author.User -> {
                    with(ListAuthorUserItemBinding.bind(view)) {
                        avatarImage.loadAvatar(author.avatarUrl)
                        department.text = author.department
                        department.isVisible = author.department.isNotEmpty()
                        userName.text = author.name
                        arrowImage.isVisible = authorId == author.id
                    }
                }
                is Author.Group -> {
                    with(ListAuthorGroupItemBinding.bind(view)) {
                        groupName.text = author.title
                        arrowImage.isVisible = authorId == author.id
                    }
                }
            }
        }
    }
}