package app.editors.manager.mvp.presenters.login;

import app.editors.manager.app.App;
import app.editors.manager.mvp.models.request.RequestNumber;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.login.EnterprisePhoneView;
import moxy.InjectViewState;
import retrofit2.Response;

@InjectViewState
public class EnterprisePhonePresenter extends BasePresenter<EnterprisePhoneView, ResponseSignIn> {

    public static final String TAG = EnterprisePhonePresenter.class.getSimpleName();

    public EnterprisePhonePresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    public void setPhone(final String newPhone) {
        final RequestNumber requestNumber = new RequestNumber();
        requestNumber.setMobilePhone(newPhone);

        if (!mPreferenceTool.isSocialLogin()) {
            requestNumber.setUserName(mPreferenceTool.getLogin());
            requestNumber.setPassword(mPreferenceTool.getPassword());
        } else {
            requestNumber.setProvider(mPreferenceTool.getSocialProvider());
            requestNumber.setAccessToken(mPreferenceTool.getSocialToken());
        }

        mRequestCall = mRetrofitTool.getApiWithPreferences().changeNumber(requestNumber);
        mRequestCall.enqueue(new BaseCallback() {

            @Override
            public void onSuccessResponse(Response<ResponseSignIn> response) {
                mPreferenceTool.setPhoneNoise(newPhone);
                getViewState().onSuccessChange();
            }
        });
    }

}
