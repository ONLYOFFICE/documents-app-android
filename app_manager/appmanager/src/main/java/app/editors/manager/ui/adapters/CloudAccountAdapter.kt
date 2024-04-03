package app.editors.manager.ui.adapters

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.editors.manager.R
import app.editors.manager.databinding.AccountListItemLayoutBinding
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.ManagerUiUtils.setDropboxImage
import app.editors.manager.managers.utils.ManagerUiUtils.setOneDriveImage
import com.bumptech.glide.Glide
import lib.toolkit.base.managers.extensions.inflate
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.adapters.BaseListAdapter

class CloudAccountAdapter(
    private val accountClickListener: ((position: Int) -> Unit),
    private val accountContextClickListener: ((position: Int, view: View) -> Unit),
    private val addClickListener: (() -> Unit)
) : BaseListAdapter<CloudAccount>() {

    private var accountOnlineId: String = ""

    var selected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                repeat(itemList.size) {
                    notifyItemChanged(it)
                }
            }
        }

    var selectedTracker: SelectionTracker<String>? = null
        set(value) {
            if (field == null) {
                field = value
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> CloudAccountViewHolder(parent.inflate(R.layout.account_list_item_layout))
            else -> AddViewHolder(
                addClickListener,
                parent.inflate(R.layout.add_item_layout)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CloudAccountViewHolder) {
            selectedTracker?.let { tracker ->
                holder.bind(
                    mList[position],
                    tracker.hasSelection(),
                    mList[position].id == accountOnlineId,
                    accountClickListener,
                    accountContextClickListener
                )
                if (tracker.hasSelection()) {
                    holder.setSelection(tracker.isSelected(mList[position].id))
                    holder.setMode(true)
                } else {
                    holder.setMode(false)
                }
            }
        } else {
            (holder as AddViewHolder).bind(selectedTracker?.hasSelection())
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            2
        } else {
            1
        }
    }

    override fun setItems(list: MutableList<CloudAccount>) {
        list.add(0, CloudAccount(""))
        super.setItems(list)
    }

    fun getIds(): List<String> {
        return mList.filter { it.id.isNotEmpty() }.map { it.id }
    }

    fun setItems(list: MutableList<CloudAccount>, accountOnlineId: String) {
        this.accountOnlineId = accountOnlineId
        setItems(list)
    }

}

class CloudAccountViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val binding: AccountListItemLayoutBinding = AccountListItemLayoutBinding.bind(view)

    var itemDetailsLookup: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(
        account: CloudAccount,
        isSelection: Boolean,
        isOnline: Boolean,
        accountClick: ((position: Int) -> Unit)? = null,
        accountContextClick: ((position: Int, view: View) -> Unit)? = null
    ) {
        val token = AccountUtils.getToken(view.context, account.accountName)
        itemDetailsLookup = AccountDetailsItemLookup(absoluteAdapterPosition, account)
        with(binding) {
            accountItemName.text = account.name
            accountItemPortal.text = account.portal.url
            accountItemContext.isVisible = true
            imageCheck.isVisible = !isSelection && isOnline

            when {
                account.isOneDrive -> accountAvatar.setOneDriveImage()
                account.isDropbox -> accountAvatar.setDropboxImage(account, token.orEmpty())
                account.isWebDav -> {
                    accountItemName.text = account.login
                    ManagerUiUtils.setWebDavImage(account.portal.provider as? PortalProvider.Webdav, accountAvatar)
                }
                else -> {
                    val url = with(account) {
                        StringBuilder()
                            .append(portal.urlWithScheme.takeUnless { StringUtils.hasScheme(avatarUrl) }.orEmpty())
                            .append(avatarUrl)
                            .toString()
                    }

                    Glide.with(accountAvatar)
                        .load(GlideUtils.getCorrectLoad(url, token.orEmpty()))
                        .apply(GlideUtils.avatarOptions)
                        .into(accountAvatar)
                }
            }
            accountAvatar.foreground = null
            setListener(accountClick, accountContextClick)
        }
    }

    fun setSelection(isSelection: Boolean) {
        if (isSelection) {
            binding.accountAvatar.foreground = getDrawable(R.drawable.drawable_list_image_select_foreground)
        } else {
            binding.accountAvatar.foreground = getDrawable(R.drawable.drawable_list_image_select_background)
        }
    }

    fun setMode(hasSelection: Boolean) {
        binding.accountItemContext.isVisible = !hasSelection
    }

    private fun setListener(
        accountClick: ((position: Int) -> Unit)?,
        accountContextClick: ((position: Int, view: View) -> Unit)?
    ) {
        binding.accountItemLayout.setOnClickListener {
            accountClick?.invoke(absoluteAdapterPosition)
        }

        binding.accountItemContext.setOnClickListener { view: View ->
            accountContextClick?.invoke(absoluteAdapterPosition, view)
        }
    }

    private fun getDrawable(drawable: Int) = AppCompatResources.getDrawable(view.context, drawable)

}

internal class AddViewHolder(
    private val listener: (() -> Unit)? = null,
    view: View
) : RecyclerView.ViewHolder(view) {

    private val addLayout: LinearLayout =
        view.findViewById(R.id.accountsAddLayout)

    private val height = addLayout.layoutParams.height

    fun bind(isSelection: Boolean?) {
        addLayout.layoutParams.height = if (isSelection == true) 0 else height
        addLayout.setOnClickListener { listener?.invoke() }
    }
}

internal class AccountKeyProvider(private val recycler: RecyclerView?) :
    ItemKeyProvider<String>(SCOPE_CACHED) {

    override fun getKey(position: Int): String? {
        return if (recycler?.adapter is CloudAccountAdapter) {
            (recycler.adapter as CloudAccountAdapter).itemList?.get(position)?.id
        } else {
            null
        }
    }

    override fun getPosition(key: String): Int {
        return if (recycler?.adapter is CloudAccountAdapter) {
            (recycler.adapter as CloudAccountAdapter).itemList.indexOfFirst { it.id == key }
        } else {
            RecyclerView.NO_POSITION
        }
    }

}

internal class AccountDetailsLookup(private val recyclerView: RecyclerView?) :
    ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        recyclerView?.findChildViewUnder(e.x, e.y)?.let {
            val holder = recyclerView.getChildViewHolder(it)
            return if (holder is AddViewHolder) {
                null
            } else {
                (holder as CloudAccountViewHolder).itemDetailsLookup
            }
        } ?: run {
            return null
        }
    }
}

internal class AccountDetailsItemLookup(private val pos: Int, private val item: CloudAccount) :
    ItemDetailsLookup.ItemDetails<String>() {

    override fun getPosition(): Int = pos

    override fun getSelectionKey(): String = item.id

}
