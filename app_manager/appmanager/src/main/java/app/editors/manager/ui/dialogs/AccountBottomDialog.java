package app.editors.manager.ui.dialogs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import moxy.presenter.InjectPresenter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.presenters.login.AccountsPresenter;
import app.editors.manager.mvp.views.login.AccountsView;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.login.SignInActivity;
import app.editors.manager.ui.activities.login.WebDavLoginActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.adapters.BottomAccountAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog;

public class AccountBottomDialog extends BaseBottomDialog implements BottomAccountAdapter.OnItemClickListener,
        AccountsView {

    public static final String TAG = AccountBottomDialog.class.getSimpleName();

    public static AccountBottomDialog newInstance() {
        return new AccountBottomDialog();
    }

    @BindView(R.id.accountsRecyclerView)
    RecyclerView mRecyclerView;

    @InjectPresenter
    AccountsPresenter mAccountsPresenter;

    private Unbinder mUnbinder;
    private BottomAccountAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NO_FRAME, R.style.ContextMenuDialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.setOnAddAccountClick(null);
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Nullable
    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_fragment_accounts, null);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        initPortalsAccounts();
    }

    private void initPortalsAccounts() {
        mAdapter = new BottomAccountAdapter(requireContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(true);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnAddAccountClick(() -> PortalsActivity.showPortals(getActivity()));
        mAccountsPresenter.getAccounts();
    }

    @Override
    public void onItemClick(View view, int position) {
        mAccountsPresenter.setAccountClicked(mAdapter.getItem(position), position);
        mAccountsPresenter.loginAccount();
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
            getContext().startActivity(intent);
        }
    }

    @Override
    public void onUsersAccounts(List<AccountsSqlData> accounts) {
        mAdapter.setItems(accounts);
    }

    @Override
    public void onAccountDelete(int position) {

    }

    @Override
    public void onSignIn(String portal, String login) {
        hideDialog();
        SignInActivity.showPortalSignIn(getContext(), portal, login);
    }

    @Override
    public void showWaitingDialog() {
        showWaitingDialog(getString(R.string.dialogs_wait_title),
                getString(R.string.dialogs_common_cancel_button), TAG);;
    }

    @Override
    public void onWebDavLogin(AccountsSqlData account) {
        WebDavLoginActivity.show(getActivity(), WebDavApi.Providers.valueOf(account.getWebDavProvider()), account);
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
