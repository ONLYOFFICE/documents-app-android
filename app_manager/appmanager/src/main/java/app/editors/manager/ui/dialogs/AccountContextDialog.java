package app.editors.manager.ui.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import app.editors.manager.R;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.fragments.main.CloudAccountsFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog;

public class AccountContextDialog extends BaseBottomDialog {

    public static final String TAG = AccountContextDialog.class.getSimpleName();

    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";

    public static AccountContextDialog newInstance(AccountsSqlData account) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_ACCOUNT, account);

        AccountContextDialog fragment = new AccountContextDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnAccountContextClickListener {
        void onProfileClick(AccountsSqlData account);

        void onLogOutClick();

        void onRemoveClick();

        void onSignInClick();

    }

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
    private AccountsSqlData mAccount;
    @Nullable
    private OnAccountContextClickListener mClickListener;
    @Nullable
    private Unbinder mUnbinder;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.ContextMenuDialog);
        if (getArguments() != null && getArguments().containsKey(KEY_ACCOUNT)) {
            mAccount = getArguments().getParcelable(KEY_ACCOUNT);
        } else {
            Log.d(TAG, "onCreate: account error");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mClickListener = null;
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_context_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeader();
        initListener();
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

    private void initListener() {
        Fragment fragment = requireFragmentManager().findFragmentByTag(CloudAccountsFragment.TAG);
        if (fragment instanceof OnAccountContextClickListener) {
            mClickListener = (OnAccountContextClickListener) fragment;
        }
    }

    private void initSignInItem() {
        AppCompatImageView image = mSignInItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_item_share_user_icon));
        AppCompatTextView text = mSignInItem.findViewById(R.id.itemText);
        text.setText(getString(R.string.dialogs_sign_in_portal_header_text));
        mSignInItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onSignInClick();
            }
            dismiss();
        });
    }

    private void initProfileItem() {
        AppCompatImageView image = mProfileItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_item_share_user_icon));
        AppCompatTextView text = mProfileItem.findViewById(R.id.itemText);
        text.setText(getString(R.string.fragment_profile_title));
        mProfileItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onProfileClick(mAccount);
            }
            dismiss();
        });
    }

    private void initLogoutItem() {
        AppCompatImageView image = mLogoutItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_account_logout));
        AppCompatTextView text = mLogoutItem.findViewById(R.id.itemText);
        text.setText(getString(R.string.navigation_drawer_menu_logout));
        mLogoutItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onLogOutClick();
            }
            dismiss();
        });
    }

    private void initRemoveItem() {
        AppCompatImageView image = mRemoveItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash));
        UiUtils.setImageTint(image, R.color.colorLightRed);
        AppCompatTextView text = mRemoveItem.findViewById(R.id.itemText);
        text.setText(R.string.dialog_remove_account_title);
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorLightRed));
        mRemoveItem.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onRemoveClick();
            }
            dismiss();
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
