package app.editors.manager.ui.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.main.CloudAccountsPresenter;
import app.editors.manager.mvp.views.main.CloudAccountsView;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.login.SignInActivity;
import app.editors.manager.ui.activities.login.WebDavLoginActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.ProfileActivity;
import app.editors.manager.ui.activities.main.SettingsActivity;
import app.editors.manager.ui.adapters.CloudAccountsAdapter;
import app.editors.manager.ui.dialogs.AccountContextDialog;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.popup.CloudAccountPopup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

public class CloudAccountsFragment extends BaseAppFragment implements CloudAccountsView,
        AccountContextDialog.OnAccountContextClickListener {

    public static final String TAG = CloudAccountsFragment.class.getSimpleName();

    private static final String TAG_REMOVE = "TAG_REMOVE";

    @NonNull
    public static CloudAccountsFragment newInstance() {
        return new CloudAccountsFragment();
    }

    @BindView(R.id.accountsLayout)
    protected FrameLayout mAccountsLayout;
    @BindView(R.id.accountsRecyclerView)
    protected RecyclerView mAccountsRecyclerView;

    @Nullable
    private Unbinder mUnbinder;

    @InjectPresenter
    CloudAccountsPresenter mCloudAccountsPresenter;

    private MainActivity mMainActivity;

    private CloudAccountsAdapter mAdapter;

    private MenuItem mSettingItem;
    private MenuItem mSelectAll;
    private MenuItem mDeselect;
    private MenuItem mDeleteAll;

    @Nullable
    private CloudAccountPopup mPopup;
    @Nullable
    private AccountContextDialog mDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mMainActivity = (MainActivity) context;
        } else {
            throw new RuntimeException(CloudAccountsFragment.class.getSimpleName() + " - must implement - " +
                    MainActivity.class.getSimpleName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && requestCode == SignInActivity.REQUEST_SIGN_IN) {
            mCloudAccountsPresenter.restoreAccount();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.clouds_accounts_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (mPopup != null && mPopup.isVisible()) {
            mPopup.hide();
            return true;
        } else {
            return mCloudAccountsPresenter.onBackPressed();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        setPadding();
        initViews(savedInstanceState);
        initRecyclerView();
        mCloudAccountsPresenter.getAccounts();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.cloud_settings_menu, menu);
        mSettingItem = menu.findItem(R.id.settingsItem);
        mSelectAll = menu.findItem(R.id.selectAll);
        mDeselect = menu.findItem(R.id.deselect);
        mDeleteAll = menu.findItem(R.id.deleteSelected);
        setMenuState(!mAdapter.isSelectionMode());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsItem:
                SettingsActivity.show(requireContext());
                break;
            case R.id.selectAll:
                mCloudAccountsPresenter.selectAll(mAdapter.getItemList());
                break;
            case R.id.deselect:
                mCloudAccountsPresenter.deselectAll();
                break;
            case R.id.deleteSelected:
                mCloudAccountsPresenter.deleteAll();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (mPopup != null && mPopup.isVisible()) {
            mPopup.hide();
        }
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        if (mAdapter != null) {
            mAdapter.setOnAccountClick(null);
            mAdapter.setOnAccountContextClick(null);
            mAdapter.setOnAccountLongClick(null);
            mAdapter.setOnAddAccountClick(null);
        }
    }

    @Override
    public void onAcceptClick(@Nullable CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        if (tag != null && tag.equals(TAG_REMOVE)) {
            mCloudAccountsPresenter.removeAccount();
            hideDialog();
        }
    }


    @Override
    public void onAccountLogin() {
        hideDialog();
        if (getContext() != null && getActivity() != null) {
            final Intent intent = new Intent(getContext(), MainActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getActivity().isInMultiWindowMode()) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            requireContext().startActivity(intent);
        }
    }

    @Override
    public void onWebDavLogin(AccountsSqlData account) {
        WebDavLoginActivity.show(getActivity(), WebDavApi.Providers.valueOf(account.getWebDavProvider()), account);
    }

    @Override
    public void onShowClouds() {
        mMainActivity.getNavBar().setSelectedItemId(R.id.menu_item_cloud);
    }

    @Override
    public void onShowBottomDialog(AccountsSqlData account) {
        if (isTablet()) {
            mPopup = new CloudAccountPopup(requireContext());
            mPopup.setListener(this);
            mPopup.setAccount(account);
            mPopup.showDropAt(mAdapter.getClickedContextView());
        } else {
            mDialog = AccountContextDialog.newInstance(account);
            mDialog.show(requireFragmentManager(), AccountContextDialog.TAG);
        }
    }

    @Override
    public void onShowWaitingDialog() {
        showWaitingDialog(getString(R.string.dialogs_wait_title),
                getString(R.string.dialogs_common_cancel_button), TAG);
    }

    @Override
    public void removeItem(int position) {
        mAdapter.removeItem(position);
    }

    @Override
    public void onUpdateItem(AccountsSqlData account) {
        mAdapter.updateItem(account);
    }

    @Override
    public void onSuccessLogin() {
        hideDialog();
        requireActivity().finish();
        MainActivity.show(getContext());
    }

    @Override
    public void onSignIn(String portal, String login) {
        hideDialog();
        SignInActivity.showPortalSignIn(this, portal, login);
    }

    @Override
    public void onEmptyList() {
        mMainActivity.getNavBar().setSelectedItemId(R.id.menu_item_setting);
    }

    @Override
    public void onSetAccounts(List<AccountsSqlData> accounts) {
        mAdapter.setItems(accounts);
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        if (message != null) {
            showSnackBar(message);
        }
    }

    @Override
    public void onProfileClick(AccountsSqlData account) {
        ProfileActivity.show(requireActivity(), account);
    }

    @Override
    public void onLogOutClick() {
        mCloudAccountsPresenter.logout();
    }

    @Override
    public void onRemoveClick() {
        AccountsSqlData account = mCloudAccountsPresenter.getContextAccount();
        if (account != null) {
            showQuestionDialog(getString(R.string.dialog_remove_account_title),
                    getString(R.string.dialog_remove_account_description, account.getLogin(), account.getPortal()),
                    getString(R.string.dialogs_question_accept_remove),
                    getString(R.string.dialogs_common_cancel_button),
                    TAG_REMOVE);
        } else {
            showQuestionDialog(getString(R.string.dialog_remove_account_title),
                    getString(R.string.dialog_remove_account_description, "", ""),
                    getString(R.string.dialogs_question_accept_remove),
                    getString(R.string.dialogs_common_cancel_button),
                    TAG_REMOVE);
        }
    }

    @Override
    public void onSignInClick() {
        mCloudAccountsPresenter.signIn();
    }

    @Override
    public void onSelectionMode() {
        mAdapter.setSelectionMode(true);
        mAdapter.notifyDataSetChanged();
        mMainActivity.setNavigationButton(false);
        setMenuState(false);
    }

    @Override
    public void onDefaultState() {
        setActionBarTitle(getString(R.string.cloud_accounts_title));
        mAdapter.setSelectionMode(false);
        mAdapter.notifyDataSetChanged();
        mMainActivity.setNavigationButton(true);
        setMenuState(true);
    }

    @Override
    public void onSelectedItem(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onActionBarTitle(@NonNull String title) {
        if (title.equals("0")) {
            mDeleteAll.setEnabled(false);
        } else {
            mDeleteAll.setEnabled(true);
        }
        setActionBarTitle(title);
    }

    @Override
    public void onNotifyItems() {
        mAdapter.notifyDataSetChanged();
    }

    private void setMenuState(boolean isDefault) {
        if (mMenu != null) {
            if (isDefault) {
                mSettingItem.setVisible(true);
                mSelectAll.setVisible(false);
                mDeselect.setVisible(false);
                mDeleteAll.setVisible(false);
            } else {
                mSettingItem.setVisible(false);
                mSelectAll.setVisible(true);
                mDeselect.setVisible(true);
                mDeleteAll.setVisible(true);
            }
        }
    }

    private void setPadding() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mAccountsLayout.getLayoutParams();
        params.setMargins(getResources().getDimensionPixelSize(R.dimen.screen_left_right_padding),
                0,
                getResources().getDimensionPixelSize(R.dimen.screen_left_right_padding),
                0);
    }

    private void initViews(@Nullable Bundle state) {
        if (state == null) {
            setActionBarTitle(getString(R.string.cloud_accounts_title));
        }
        mMainActivity.setActionButtonVisibility(false);
        mMainActivity.setAppBarStates(false);
        mMainActivity.setOffToolbarAccount();
        mMainActivity.setNavigationButton(true);
    }

    private void initRecyclerView() {
        mAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new CloudAccountsAdapter();
        mAdapter.setOnAccountClick((account, position) -> mCloudAccountsPresenter.checkLogin(account, position));
        mAdapter.setOnAccountContextClick((account, position, view) -> {
            mAdapter.setClickedContextView(view);
            mCloudAccountsPresenter.contextClick(account, position);
        });
        mAdapter.setOnAccountLongClick((account, position) -> {
            if (!mAdapter.isSelectionMode()) {
                mCloudAccountsPresenter.longClick(account, position);
            } else {
                mCloudAccountsPresenter.checkLogin(account, position);
            }
        });
        mAdapter.setOnAddAccountClick(() -> PortalsActivity.showPortals(requireActivity()));
        mAccountsRecyclerView.setAdapter(mAdapter);
    }

}
