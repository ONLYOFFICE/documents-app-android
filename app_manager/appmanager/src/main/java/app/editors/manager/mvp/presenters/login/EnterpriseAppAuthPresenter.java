package app.editors.manager.mvp.presenters.login;

import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.request.RequestSignIn;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.models.user.Token;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.login.EnterpriseAppView;
import moxy.InjectViewState;
import retrofit2.Response;

@InjectViewState
public class EnterpriseAppAuthPresenter extends BaseLoginPresenter<EnterpriseAppView, ResponseSignIn> {

    public EnterpriseAppAuthPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    protected void onGetUser(User user) {
        super.onGetUser(user);
        getViewState().onSuccessLogin();
    }

    public void signInPortal(final String smsCode, AccountsSqlData sqlData) {
        final RequestSignIn requestSignIn = new RequestSignIn();
        if (!mPreferenceTool.isSocialLogin()) {
            requestSignIn.setUserName(mPreferenceTool.getLogin());
            requestSignIn.setPassword(mPreferenceTool.getPassword());
        } else {
            requestSignIn.setProvider(mPreferenceTool.getSocialProvider());
            requestSignIn.setAccessToken(mPreferenceTool.getSocialToken());
        }

        if (sqlData != null) {
            Api api = new RetrofitTool(mContext)
                    .setCiphers(sqlData.isSslCiphers())
                    .setSslOn(sqlData.isSslState())
                    .getApi(sqlData.getScheme() + sqlData.getPortal());
            mRequestCall = api.smsSignIn(requestSignIn, smsCode);

        } else {
            mRequestCall = mRetrofitTool.getApiWithPreferences().smsSignIn(requestSignIn, smsCode);
        }
        AccountsSqlData finalSqlData = sqlData;
        mRequestCall.enqueue(new BaseCallback() {

            @Override
            public void onSuccessResponse(Response<ResponseSignIn> response) {
                final Token token = response.body().getResponse();
                final String tokenStr = token.getToken();
                if (!tokenStr.isEmpty()) {
                    if (finalSqlData != null) {

                    }
                    mPreferenceTool.setToken(tokenStr);
                    getUser(tokenStr, finalSqlData);
                } else {
                    getViewState().onError(response.message());
                }
            }
        });
    }
}
