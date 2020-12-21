package app.editors.manager.ui.fragments.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.user.Thirdparty;
import app.editors.manager.mvp.presenters.main.ProfilePresenter;
import app.editors.manager.mvp.views.main.ProfileView;
import app.editors.manager.ui.adapters.ThirdpartyAdapter;
import app.editors.manager.ui.binders.ProfileItemBinder;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

public class ProfileFragment extends BaseAppFragment implements ProfileView {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    public static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String TAG_LOGOUT = "TAG_LOGOUT";
    private static final String TAG_REMOVE = "TAG_REMOVE";

    public static ProfileFragment newInstance(AccountsSqlData account) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_ACCOUNT, account);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.usernameItem)
    protected ConstraintLayout mUsernameItem;
    @BindView(R.id.emailItem)
    protected ConstraintLayout mEmailItem;
    @BindView(R.id.portalItem)
    protected ConstraintLayout mPortalItem;
    @BindView(R.id.userTypeItem)
    protected ConstraintLayout mUserTypeItem;
    @BindView(R.id.servicesContainer)
    protected LinearLayoutCompat mListContainer;
    @BindView(R.id.recyclerView)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.logoutItem)
    protected ConstraintLayout mLogoutItem;
    @BindView(R.id.removeItem)
    protected ConstraintLayout mRemoveItem;

    private ProfileItemBinder mUsernameBinder;
    private ProfileItemBinder mEmailBinder;
    private ProfileItemBinder mPortalBinder;
    private ProfileItemBinder mTypeBinder;

    @InjectPresenter
    ProfilePresenter mProfilePresenter;

    @Nullable
    private Unbinder mUnbinder;
    private AccountsSqlData mAccount;
    private ThirdpartyAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_ACCOUNT)) {
            mAccount = args.getParcelable(KEY_ACCOUNT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mUsernameBinder = new ProfileItemBinder(mUsernameItem);
        mEmailBinder = new ProfileItemBinder(mEmailItem);
        mPortalBinder = new ProfileItemBinder(mPortalItem);
        mTypeBinder = new ProfileItemBinder(mUserTypeItem);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProfilePresenter.setAccount(mAccount);
        if (savedInstanceState != null) {
            mProfilePresenter.getType(savedInstanceState.getString(KEY_TYPE));
        } else {
            mProfilePresenter.getType(null);
        }

        initRemoveItem();

        mTypeBinder.setTitle(getString(R.string.profile_type_account))
                .setImage(R.drawable.ic_contact_calendar);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TYPE, mTypeBinder.getText());
    }

    @Override
    public void onAcceptClick(@Nullable CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null) {
            switch (tag) {
                case TAG_REMOVE: {
                    mProfilePresenter.removeAccount();
                    break;
                }
                case TAG_LOGOUT: {
                    mProfilePresenter.logout();
                    break;
                }
            }
        }
        hideDialog();
    }

    private void initRemoveItem() {
        AppCompatImageView image = mRemoveItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash));
        UiUtils.setImageTint(image, R.color.colorLightRed);
        AppCompatTextView text = mRemoveItem.findViewById(R.id.itemText);
        text.setText(getString(R.string.dialog_remove_account_title));
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorLightRed));
        mRemoveItem.setOnClickListener(v -> showQuestionDialog(getString(R.string.dialog_remove_account_title),
                getString(R.string.dialog_remove_account_description, mAccount.getLogin(), mAccount.getPortal()),
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                TAG_REMOVE));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onWebDavState() {
        mEmailItem.setVisibility(View.VISIBLE);
        mPortalItem.setVisibility(View.VISIBLE);

        mEmailBinder.setTitle(getString(R.string.login_enterprise_email_hint))
                .setImage(R.drawable.ic_email)
                .setText(mAccount.getLogin());

        mPortalBinder.setTitle(getString(R.string.profile_portal_address))
                .setImage(R.drawable.ic_cloud)
                .setText(mAccount.getScheme() + StringUtils.getEncodedString(mAccount.getPortal()));

    }

    @Override
    public void onCloudState() {
        mUsernameItem.setVisibility(View.VISIBLE);
        mUserTypeItem.setVisibility(View.VISIBLE);

        mUsernameBinder.setTitle(getString(R.string.profile_username_title))
                .setText(mAccount.getName())
                .setImage(R.drawable.ic_list_item_share_user_icon);
    }

    @Override
    public void onOnlineState() {
        mLogoutItem.setVisibility(View.VISIBLE);
        AppCompatImageView image = mLogoutItem.findViewById(R.id.itemImage);
        image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_account_logout));
        UiUtils.setImageTint(image, R.color.colorLight);
        AppCompatTextView text = mLogoutItem.findViewById(R.id.itemText);
        text.setText(getString(R.string.navigation_drawer_menu_logout));
        mLogoutItem.setOnClickListener(v -> showQuestionDialog(getString(R.string.dialog_logout_account_title),
                getString(R.string.dialog_logout_account_description),
                getString(R.string.navigation_drawer_menu_logout),
                getString(R.string.dialogs_common_cancel_button), TAG_LOGOUT));
    }

    @Override
    public void onEmptyThirdparty() {
        mListContainer.setVisibility(View.GONE);
    }

    @Override
    public void onSetServices(List<Thirdparty> list) {
        mListContainer.setVisibility(View.VISIBLE);
        mAdapter = new ThirdpartyAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setItems(list);
    }

    @Override
    public void onAccountType(String type) {
        mTypeBinder.setText(type);
    }

    @Override
    public void onClose() {
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    @Override
    public void onError(@Nullable String message) {
        if (message != null) {
            showSnackBar(message);
        }
    }
}
