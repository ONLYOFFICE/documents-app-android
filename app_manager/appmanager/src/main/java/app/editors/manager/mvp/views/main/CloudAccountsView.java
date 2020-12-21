package app.editors.manager.mvp.views.main;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface CloudAccountsView extends BaseView {

    void onAccountLogin();

    void onWebDavLogin(AccountsSqlData account);

    void onShowClouds();

    void onShowBottomDialog(AccountsSqlData account);

    void onSetAccounts(List<AccountsSqlData> accounts);

    void onShowWaitingDialog();

    void removeItem(int position);

    void onUpdateItem(AccountsSqlData account);

    void onSuccessLogin();

    void onSignIn(String portal, String login);

    void onEmptyList();

    void onSelectionMode();

    void onDefaultState();

    void onSelectedItem(int position);

    void onActionBarTitle(String title);

    void onNotifyItems();
}
