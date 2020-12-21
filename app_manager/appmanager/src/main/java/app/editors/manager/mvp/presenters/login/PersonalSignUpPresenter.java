package app.editors.manager.mvp.presenters.login;

import java.util.Locale;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.mvp.models.request.RequestRegister;
import app.editors.manager.mvp.models.response.ResponseRegisterPersonalPortal;
import app.editors.manager.mvp.views.login.PersonalRegisterView;
import moxy.InjectViewState;
import retrofit2.Response;

@InjectViewState
public class PersonalSignUpPresenter extends BaseLoginPresenter<PersonalRegisterView, ResponseRegisterPersonalPortal> {

    public static final String TAG = PersonalSignUpPresenter.class.getSimpleName();
    private static final String EMAIL_CODE = "201";

    public PersonalSignUpPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    public void registerPortal(final String email) {
        try {
            initRetrofitPref(Api.PERSONAL_HOST);
            final RequestRegister requestRegister = new RequestRegister();
            requestRegister.setEmail(email);
            requestRegister.setLanguage(Locale.getDefault().getLanguage());
            mRequestCall = mRetrofitTool.getApiWithPreferences().registerPersonalPortal(requestRegister);
            mRequestCall.enqueue(new BaseCallback() {

                @Override
                public void onSuccessResponse(Response<ResponseRegisterPersonalPortal> response) {
                    final String message = response.body().getResponse();
                    if (message != null && !message.isEmpty() && !response.body().getStatus().equals(EMAIL_CODE)) {
                        getViewState().onError(mContext.getString(R.string.errors_email_already_registered));
                        return;
                    } else if (message != null && !message.isEmpty()) {
                        getViewState().onError(message);
                        return;
                    }

                    getViewState().onRegisterPortal();
                }
            });
        } catch (UrlSyntaxMistake e) {
            // No need handle
        }
    }

}
