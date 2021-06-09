package app.editors.manager.ui.dialogs

import android.accounts.Account
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.UiUtils
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.UiUtils.setImageTint
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class AccountContextDialog : BaseBottomDialog() {

    interface OnAccountContextClickListener {
        fun onProfileClick(account: CloudAccount?)
        fun onLogOutClick()
        fun onRemoveClick(account: CloudAccount?)
        fun onSignInClick()
    }

    companion object {
        val TAG: String = AccountContextDialog::class.java.simpleName

        private const val KEY_ACCOUNT = "KEY_ACCOUNT"
        private const val KEY_TOKEN = "KEY_TOKEN"

        fun newInstance(account: String, token: String?): AccountContextDialog {
            return AccountContextDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_ACCOUNT, account)
                    putString(KEY_TOKEN, token)
                }
            }
        }
    }


    @JvmField
    @BindView(R.id.headerItem)
    var mHeaderItem: FrameLayout? = null

    @JvmField
    @BindView(R.id.view_icon_selectable_image)
    var mAvatarImage: AppCompatImageView? = null

    @JvmField
    @BindView(R.id.imageCheck)
    var mCheckImage: AppCompatImageView? = null

    @JvmField
    @BindView(R.id.accountItemName)
    var mAccountName: AppCompatTextView? = null

    @JvmField
    @BindView(R.id.accountItemPortal)
    var mAccountPortal: AppCompatTextView? = null

    @JvmField
    @BindView(R.id.accountItemEmail)
    var mAccountEmail: AppCompatTextView? = null

    @JvmField
    @BindView(R.id.accountItemContext)
    var mAccountContext: AppCompatImageButton? = null

    @JvmField
    @BindView(R.id.signInItem)
    var mSignInItem: ConstraintLayout? = null

    @JvmField
    @BindView(R.id.profileItem)
    var mProfileItem: ConstraintLayout? = null

    @JvmField
    @BindView(R.id.logoutItem)
    var mLogoutItem: ConstraintLayout? = null

    @JvmField
    @BindView(R.id.removeItem)
    var mRemoveItem: ConstraintLayout? = null
    private var mAccount: CloudAccount? = null
    private var mClickListener: OnAccountContextClickListener? = null
    private var mUnbinder: Unbinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.ContextMenuDialog)
        if (arguments != null && arguments!!.containsKey(KEY_ACCOUNT)) {
             arguments?.getString(KEY_ACCOUNT)?.let { mAccount = Json.decodeFromString(it) }
        } else {
            Log.d(TAG, "onCreate: account error")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mClickListener = null
        if (mUnbinder != null) {
            mUnbinder!!.unbind()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.account_context_layout, container, false)
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initListener()
        initSignInItem()
        initProfileItem()
        initLogoutItem()
        initRemoveItem()
        setState()
    }

    private fun initHeader() {
        mCheckImage!!.visibility = View.GONE
        mAccountContext!!.visibility = View.GONE
        if (mAccount != null) {
            mAccountName!!.text = mAccount!!.name
            mAccountEmail!!.text = mAccount!!.login
            mAccountPortal!!.text = mAccount!!.portal
            if (mAccount!!.isWebDav) {
                mAccountName!!.visibility = View.GONE
                UiUtils.setWebDavImage(mAccount?.webDavProvider, mAvatarImage!!)
            } else {
                loadAvatar()
            }
        }
    }

    private fun initListener() {
        requireFragmentManager().fragments.find { it is OnAccountContextClickListener }?.let {
            mClickListener = (it as OnAccountContextClickListener)
        }
    }

    private fun initSignInItem() {
        val image: AppCompatImageView = mSignInItem!!.findViewById(R.id.itemImage)
        image.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_list_item_share_user_icon
            )
        )
        val text: AppCompatTextView = mSignInItem!!.findViewById(R.id.itemText)
        text.text = getString(R.string.dialogs_sign_in_portal_header_text)
        mSignInItem!!.setOnClickListener { v: View? ->
            if (mClickListener != null) {
                mClickListener!!.onSignInClick()
            }
            dismiss()
        }
    }

    private fun initProfileItem() {
        val image: AppCompatImageView = mProfileItem!!.findViewById(R.id.itemImage)
        image.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_list_item_share_user_icon
            )
        )
        val text: AppCompatTextView = mProfileItem!!.findViewById(R.id.itemText)
        text.text = getString(R.string.fragment_profile_title)
        mProfileItem!!.setOnClickListener { v: View? ->
            if (mClickListener != null) {
                mClickListener!!.onProfileClick(mAccount)
            }
            dismiss()
        }
    }

    private fun initLogoutItem() {
        val image: AppCompatImageView = mLogoutItem!!.findViewById(R.id.itemImage)
        image.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_account_logout
            )
        )
        val text: AppCompatTextView = mLogoutItem!!.findViewById(R.id.itemText)
        text.text = getString(R.string.navigation_drawer_menu_logout)
        mLogoutItem!!.setOnClickListener { v: View? ->
            if (mClickListener != null) {
                mClickListener!!.onLogOutClick()
            }
            dismiss()
        }
    }

    private fun initRemoveItem() {
        val image: AppCompatImageView = mRemoveItem!!.findViewById(R.id.itemImage)
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash))
        setImageTint(image, R.color.colorLightRed)
        val text: AppCompatTextView = mRemoveItem!!.findViewById(R.id.itemText)
        text.setText(R.string.dialog_remove_account_title)
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorLightRed))
        mRemoveItem!!.setOnClickListener { v: View? ->
            if (mClickListener != null) {
                mClickListener!!.onRemoveClick(account = mAccount)
            }
            dismiss()
        }
    }

    private fun setState() {
        if (mAccount != null) {
            val password = AccountUtils.getPassword(context!!, Account(mAccount!!.getAccountName(), context!!.getString(R.string.account_type)))
            val token = AccountUtils.getToken(context!!, Account(mAccount!!.getAccountName(), context!!.getString(R.string.account_type)))
            if (mAccount!!.isWebDav) {
                if (mAccount!!.isOnline || password != null && password.isNotEmpty()
                ) {
                    mSignInItem!!.visibility = View.GONE
                }
                if (password == null || password.isEmpty()) {
                    mLogoutItem!!.visibility = View.GONE
                }
                mProfileItem!!.visibility = View.GONE
            } else {
                if (mAccount!!.isOnline || token != null && token.isNotEmpty()) {
                    mSignInItem!!.visibility = View.GONE
                }
                if (token == null || token.isEmpty()) {
                    mLogoutItem!!.visibility = View.GONE
                    mProfileItem!!.visibility = View.GONE
                }
            }
        }
    }

    private fun loadAvatar() {
        if (mAccount != null) {
            val url = if (mAccount?.avatarUrl?.contains("static") == true) {
                 mAccount?.avatarUrl
            } else {
                 mAccount?.scheme + mAccount?.portal + mAccount?.avatarUrl
            }
            Glide.with(mAvatarImage!!)
                .load(GlideUtils.getCorrectLoad(url ?: "", arguments?.getString(KEY_TOKEN) ?: ""))
                .apply(GlideUtils.getAvatarOptions())
                .into(mAvatarImage!!)
        }
    }
}