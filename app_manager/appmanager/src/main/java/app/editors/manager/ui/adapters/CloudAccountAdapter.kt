package app.editors.manager.ui.adapters

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.ManagerUiUtils.setOneDriveImage
import com.bumptech.glide.Glide
import lib.toolkit.base.managers.extensions.inflate
import lib.toolkit.base.ui.adapters.BaseListAdapter

class CloudAccountAdapter(
    private val accountClickListener: ((position: Int) -> Unit),
    private val accountContextClickListener: ((position: Int, view: View) -> Unit),
    private val addClickListener: (() -> Unit)
) : BaseListAdapter<CloudAccount>() {

    var selected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
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
                parent.inflate(R.layout.add_account_item_layout)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CloudAccountViewHolder) {
            selectedTracker?.let { tracker ->
                holder.bind(
                    mList[position],
                    tracker.hasSelection(),
                    accountClickListener,
                    accountContextClickListener
                )
                holder.setSelection(tracker.isSelected(mList[position].id))
                holder.setMode(tracker.hasSelection())
            }
        } else {
            (holder as AddViewHolder).bind(selectedTracker?.hasSelection())
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mList.size - 1) {
            2
        } else {
            1
        }
    }

    override fun setItems(list: MutableList<CloudAccount>) {
        list.add(CloudAccount(""))
        super.setItems(list)
    }

    fun getIds(): List<String> {
        return mList.filter { it.id.isNotEmpty() }.map { it.id }
    }

}

class CloudAccountViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val accountLayout: ConstraintLayout = view.findViewById(R.id.accountItemLayout)
    private val iconSelectableImage: AppCompatImageView =
        view.findViewById(R.id.view_icon_selectable_image)
    private val checkImage: AppCompatImageView = view.findViewById(R.id.imageCheck)
    private val iconSelectableMask: FrameLayout = view.findViewById(R.id.view_icon_selectable_mask)
    private val iconSelectableLayout: FrameLayout = view.findViewById(R.id.selectableLayout)
    private val accountName: AppCompatTextView = view.findViewById(R.id.accountItemName)
    private val accountPortal: AppCompatTextView = view.findViewById(R.id.accountItemPortal)
    private val accountEmail: AppCompatTextView = view.findViewById(R.id.accountItemEmail)
    private val accountContext: AppCompatImageButton = view.findViewById(R.id.accountItemContext)

    var itemDetailsLookup: ItemDetailsLookup.ItemDetails<String>? = null

    fun bind(
        account: CloudAccount,
        isSelection: Boolean,
        accountClick: ((position: Int) -> Unit)? = null,
        accountContextClick: ((position: Int, view: View) -> Unit)? = null
    ) {
        itemDetailsLookup = AccountDetailsItemLookup(absoluteAdapterPosition, account)

        accountName.text = account.name
        accountPortal.text = account.portal
        accountEmail.text = account.login
        accountContext.visibility = View.VISIBLE
        if (!isSelection) {
            if (account.isOnline) {
                checkImage.visibility = View.VISIBLE
            } else {
                checkImage.visibility = View.GONE
            }
        } else {
            checkImage.visibility = View.GONE
        }
        if (account.isWebDav) {
            accountName.visibility = View.GONE
            ManagerUiUtils.setWebDavImage(account.webDavProvider, iconSelectableImage)
        } else if(account.isOneDrive) {
            iconSelectableImage.setOneDriveImage()
        } else {
            accountName.visibility = View.VISIBLE
            val url: String = if (account.avatarUrl?.contains("static") == true) {
                account.avatarUrl ?: ""
            } else {
                account.scheme + account.portal + account.avatarUrl
            }
            Glide.with(iconSelectableImage)
                .load(GlideUtils.getCorrectLoad(url, account.token ?: ""))
                .apply(GlideUtils.avatarOptions)
                .into(iconSelectableImage)

        }
        iconSelectableLayout.background = null
        iconSelectableMask.background = null
        setListener(accountClick, accountContextClick)
    }

    fun setSelection(isSelection: Boolean) {
        if (isSelection) {
            iconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask)
        } else {
            iconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_background)
        }
    }

    fun setMode(hasSelection: Boolean) {
        accountContext.visibility = if (hasSelection) View.GONE else View.VISIBLE
    }

    private fun setListener(
        accountClick: ((position: Int) -> Unit)?,
        accountContextClick: ((position: Int, view: View) -> Unit)?
    ) {
        accountLayout.setOnClickListener {
            accountClick?.invoke(absoluteAdapterPosition)
        }

        accountContext.setOnClickListener { view: View ->
            accountContextClick?.invoke(absoluteAdapterPosition, view)
        }
    }

}

internal class AddViewHolder(
    private val listener: (() -> Unit)? = null,
    view: View
) : RecyclerView.ViewHolder(view) {

    private val addLayout: LinearLayoutCompat =
        view.findViewById(R.id.fragment_accounts_add_account)

    fun bind(isSelection: Boolean?) {
        if (isSelection == true) {
            itemView.visibility = View.GONE
        } else {
            itemView.visibility = View.VISIBLE
        }
        addLayout.setOnClickListener {
            listener?.invoke()
        }
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
