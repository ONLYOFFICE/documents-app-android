package app.editors.manager.mvp.presenters.login;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.request.RequestRegister;
import app.editors.manager.mvp.models.request.RequestSignIn;
import app.editors.manager.mvp.models.response.ResponseRegisterPortal;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.login.EnterpriseCreateSignInView;
import moxy.InjectViewState;
import retrofit2.Response;

@InjectViewState
public class EnterpriseCreateLoginPresenter extends BaseLoginPresenter<EnterpriseCreateSignInView, ResponseRegisterPortal> {

    public static final String TAG = EnterpriseCreateLoginPresenter.class.getSimpleName();

    private static final int PORTAL_PARTS = 3;
    private static final int PORTAL_PART_NAME = 0;
    private static final int PORTAL_PART_HOST = 1;
    private static final int PORTAL_PART_DOMAIN = 2;
    private static final int PORTAL_LENGTH = 6;

    private static final String APP_KEY = "android-39ed-4f49-89a4-01fe9175dc91";

    public EnterpriseCreateLoginPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mRequestSignInCall != null) {
            mRequestSignInCall.cancel();
        }
    }

    @Override
    protected void onTwoFactorAuth(boolean isPhone, AccountsSqlData sqlData) {
        super.onTwoFactorAuth(isPhone, sqlData);
        getViewState().onTwoFactorAuth(isPhone);
    }

    @Override
    protected void onGetUser(User user) {
        super.onGetUser(user);
        getViewState().onSuccessLogin();
    }

    public void createPortal( final String password) {
        mPreferenceTool.setPassword(password);

        // Check user input portal
        final String [] partsPortal = mPreferenceTool.getPortal().split("\\.");
        if (partsPortal.length != PORTAL_PARTS || partsPortal[PORTAL_PART_NAME].length() < PORTAL_LENGTH) {
            getViewState().onError(mContext.getString(R.string.login_api_portal_name));
            return;
        }

        // Create api
        final String domain = partsPortal[PORTAL_PART_HOST] + "." + partsPortal[PORTAL_PART_DOMAIN] + "/";
        final String apiHost = mPreferenceTool.getScheme() + Api.API_SUBDOMAIN + "." + domain;
        try {
            mRetrofitApi.setSslOn(mPreferenceTool.getSslState());
            mRetrofitApi.setCiphers(mPreferenceTool.getSslCiphers());
            mRetrofitApi.init(apiHost);
        } catch (UrlSyntaxMistake e) {
            getViewState().onError(mContext.getString(R.string.login_api_init_portal_error));
            return;
        }

        // Validate portal
        final RequestRegister requestRegister = new RequestRegister();
        requestRegister.setPortalName(partsPortal[PORTAL_PART_NAME]);
        requestRegister.setEmail(mPreferenceTool.getLogin());
        requestRegister.setFirstName(mPreferenceTool.getUserFirstName());
        requestRegister.setLastName(mPreferenceTool.getUserLastName());
        requestRegister.setPassword(mPreferenceTool.getPassword());
        requestRegister.setAppKey(APP_KEY);

        mRequestCall = mRetrofitApi.getApi(apiHost).registerPortal(requestRegister);
        mRequestCall.enqueue(new BaseCallback() {

            @Override
            public void onSuccessResponse(Response<ResponseRegisterPortal> response) {
                FirebaseUtils.addAnalyticsCreatePortal(mPreferenceTool.getPortal(), mPreferenceTool.getLogin());
                signInPortal();
            }

            @Override
            public void onErrorResponse(Response<ResponseRegisterPortal> response) {
                getViewState().onError(mContext.getString(R.string.login_api_init_error));
            }
        });
    }

    private void signInPortal() {
        try {
            final RequestSignIn requestSignIn = new RequestSignIn();
            requestSignIn.setUserName(mPreferenceTool.getLogin());
            requestSignIn.setPassword(mPreferenceTool.getPassword());
            initRetrofitPref(mPreferenceTool.getPortal());
            signIn(requestSignIn, mPreferenceTool.getPortal());
        } catch (UrlSyntaxMistake e) {
            return;
        }
    }

}
