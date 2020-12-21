/*
 * Created by Michael Efremov on 30.07.20 11:21
 */

package app.editors.manager.ui.popup;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import app.editors.manager.R;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.dialogs.AccountContextDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.popup.BasePopup;

public class CloudAccountPopup extends BasePopup {

    @BindView(R.id.headerItem)
    FrameLayout mHeaderItem;
    @BindView(R.id.view_icon_selectable_image)
    AppCompatImageView mAvatarImage;
    @BindView(R.id.imageCheck)
    AppCompatImageView mCheckImage;
    @BindView(R.id.accountItemName)
    AppCompatTextView mAccountName;
    @BindView(R.id.accountItemPortal)
    AppCompatTextView mAccountPortal;
    @BindView(R.id.accountItemEmail)
    AppCompatTextView mAccountEmail;
    @BindView(R.id.accountItemContext)
    AppCompatImageButton mAccountContext;
    @BindView(R.id.signInItem)
    ConstraintLayout mSignInItem;
    @BindView(R.id.profileItem)
    ConstraintLayout mProfileItem;
    @BindView(R.id.logoutItem)
    ConstraintLayout mLogoutItem;
    @BindView(R.id.removeItem)
    ConstraintLayout mRemoveItem;

    @Nullable
    private Unbinder mUnbinder;
    @Nullable
    private AccountContextDialog.OnAccountContextClickListener mClickListener;
    private AccountsSqlData mAccount;


    public CloudAccountPopup(@NonNull Context context) {
        super(context, R.layout.cloud_account_popup_layout);
    }

    @Override
    protected void bind(@NonNull View view) {
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void hide() {
        super.hide();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        mClickListener = null;
    }

    public void setListener(AccountContextDialog.OnAccountContextClickListener listener) {
        mClickListener = listener;
    }

    public boolean isVisible() {
        return mPopupWindow.isShowing();
    }

    public void setAccount(AccountsSqlData mAccount) {
        this.mAccount = mAccount;
        initHeader();
        initSignInItem();
        initProfileItem();
        initLogoutItem();
        initRemoveItem();
        setState();
    }

    private void initHeader() {
        mCheckImage.setVisibility(View.GONE);
        mAccountContext.setVisibility(View.GONE);
        if (mAccount != null) {
            mAccountName.setText(mAccount.getName());
            mAccountEmail.setText(mAccount.getLogin());
            mAccountPortal.setText(mAccount.getPortal());
            if (mAccount.isWebDav()) {
                mAccountName.setVisibility(View.GONE);
                app.editors.manager.managers.utils.UiUtils.setWebDavImage(mAccount.getWebDavProvider(), mAvatarImage);
            } else {
                loadAvatar();
            }
        }
    }

    private void initSignInItem() {
        AppCompatImageView image = mSignInItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_list_item_share_user_icon));
        AppCompatTextView text = mSignInItem.findViewById(R.id.itemText);
        text.setText(getContext().getString(R.string.dialogs_sign_in_portal_header_text));
        mSignInItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onSignInClick();
            }
            hide();
        });
    }

    private void initProfileItem() {
        AppCompatImageView image = mProfileItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_list_item_share_user_icon));
        AppCompatTextView text = mProfileItem.findViewById(R.id.itemText);
        text.setText(getContext().getString(R.string.fragment_profile_title));
        mProfileItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onProfileClick(mAccount);
            }
            hide();
        });
    }

    private void initLogoutItem() {
        AppCompatImageView image = mLogoutItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_account_logout));
        AppCompatTextView text = mLogoutItem.findViewById(R.id.itemText);
        text.setText(getContext().getString(R.string.navigation_drawer_menu_logout));
        mLogoutItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onLogOutClick();
            }
            hide();
        });
    }

    private void initRemoveItem() {
        AppCompatImageView image = mRemoveItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_trash));
        UiUtils.setImageTint(image, R.color.colorLightRed);
        AppCompatTextView text = mRemoveItem.findViewById(R.id.itemText);
        text.setText(R.string.dialog_remove_account_title);
        text.setTextColor(ContextCompat.getColor(getContext(), R.color.colorLightRed));
        mRemoveItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onRemoveClick();
            }
            hide();
        });
    }

    private void setState() {
        if (mAccount != null) {

            if (mAccount.isWebDav()) {
                if (mAccount.isOnline() || (mAccount.getPassword() != null && !mAccount.getPassword().isEmpty())) {
                    mSignInItem.setVisibility(View.GONE);
                }
                if (mAccount.getPassword() == null || mAccount.getPassword().isEmpty()) {
                    mLogoutItem.setVisibility(View.GONE);
                }
                mProfileItem.setVisibility(View.GONE);
            } else {
                if (mAccount.isOnline() || (mAccount.getToken() != null && !mAccount.getToken().isEmpty())) {
                    mSignInItem.setVisibility(View.GONE);
                }
                if (mAccount.getToken() == null || mAccount.getToken().isEmpty()) {
                    mLogoutItem.setVisibility(View.GONE);
                    mProfileItem.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadAvatar() {
        if (mAccount != null) {
            final String url = mAccount.getScheme() + mAccount.getPortal() + mAccount.getAvatarUrl();
            Glide.with(mAvatarImage)
                    .load(GlideUtils.getCorrectLoad(url, mAccount.getToken()))
                    .apply(GlideUtils.getAvatarOptions())
                    .into(mAvatarImage);
        }

    }

}
