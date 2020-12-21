package app.editors.manager.mvp.views.login;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface AccountsView extends BaseView {
    void onAccountLogin();
    void onUsersAccounts(List<AccountsSqlData> accounts);
    void onAccountDelete(int position);
    void onSignIn(String portal, String login);
    void showWaitingDialog();
    void onWebDavLogin(AccountsSqlData mAccountClickedItem);
}