package app.editors.manager.mvp.presenters.login;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.request.RequestSignIn;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.models.user.Token;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.login.EnterpriseSmsView;
import moxy.InjectViewState;
import retrofit2.Call;
import retrofit2.Response;

@InjectViewState
public class EnterpriseSmsPresenter extends BaseLoginPresenter<EnterpriseSmsView, ResponseSignIn> {

    public static final String TAG = EnterpriseSmsPresenter.class.getSimpleName();

    public EnterpriseSmsPresenter() {
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

        mRequestCall = mRetrofitTool.getApiWithPreferences().smsSignIn(requestSignIn, smsCode);
        mRequestCall.enqueue(new BaseCallback() {

            @Override
            public void onSuccessResponse(Response<ResponseSignIn> response) {
                final Token token = response.body().getResponse();
                final String tokenStr = token.getToken();
                if (!tokenStr.isEmpty()) {
                    mPreferenceTool.setToken(tokenStr);
                    getUser(tokenStr, sqlData);
                } else {
                    getViewState().onError(response.message());
                }
            }
        });
    }

    public void resendSms() {
        final RequestSignIn requestSignIn = new RequestSignIn();
        if (!mPreferenceTool.isSocialLogin()) {
            requestSignIn.setUserName(mPreferenceTool.getLogin());
            requestSignIn.setPassword(mPreferenceTool.getPassword());
        } else {
            requestSignIn.setProvider(mPreferenceTool.getSocialProvider());
            requestSignIn.setAccessToken(mPreferenceTool.getSocialToken());
        }

        mRequestCall = mRetrofitTool.getApiWithPreferences().sendSms(requestSignIn);
        mRequestCall.enqueue(new BaseCallback() {

            @Override
            public void onSuccessResponse(Response<ResponseSignIn> response) {
                getViewState().onResendSms();
            }

            @Override
            public void onResponse(Call<ResponseSignIn> call, Response<ResponseSignIn> response) {
                try {
                    final String errorMessage = response.errorBody().string();
                    if (errorMessage.contains(Api.Errors.SMS_TO_MANY)) {
                        getViewState().onError(mContext.getString(R.string.errors_client_portal_sms));
                        return;
                    }
                } catch (Exception e) {
                    // No need handle
                }

                super.onErrorResponse(response);
            }

        });
    }

}
