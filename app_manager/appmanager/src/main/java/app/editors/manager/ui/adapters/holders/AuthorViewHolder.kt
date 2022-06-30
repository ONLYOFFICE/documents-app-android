package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.editors.manager.databinding.ListAuthorGroupItemBinding
import app.editors.manager.databinding.ListAuthorUserItemBinding
import app.editors.manager.managers.utils.GlideUtils.setAvatar
import app.editors.manager.mvp.models.filter.Author
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.ui.adapters.AuthorAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class AuthorViewHolder(
    view: View,
    private val authorId: String?,
    private val clickListener: (FilterAuthor) -> Unit
) : BaseViewHolder<Author>(view) {

    override fun bind(item: Author, payloads: List<Any>) {
        if (payloads.contains(AuthorAdapter.PAYLOAD_AVATAR) && item is Author.User) {
            with(ListAuthorUserItemBinding.bind(view)) {
                avatarImage.setAvatar(item.avatar)
            }
        }
    }

    override fun bind(item: Author) {
        when (item) {
            is Author.User -> {
                with(ListAuthorUserItemBinding.bind(view)) {
                    department.text = item.department
                    department.isVisible = item.department.isNotEmpty()
                    userName.text = item.name
                    arrowImage.isVisible = authorId == item.id
                    item.avatar?.let { drawable -> avatarImage.setAvatar(drawable) }
                }
            }
            is Author.Group -> {
                with(ListAuthorGroupItemBinding.bind(view)) {
                    groupName.text = item.name
                    arrowImage.isVisible = authorId == item.id
                }
            }
        }
        view.setOnClickListener {
            clickListener(FilterAuthor(item.id, item.name, item is Author.Group))
        }
    }
}
