package app.editors.manager.mvp.presenters.main;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.response.ResponseThirdparty;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.models.user.Thirdparty;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.main.ProfileView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.InjectViewState;
import moxy.MvpPresenter;
import retrofit2.HttpException;
import retrofit2.Response;

@InjectViewState
public class ProfilePresenter extends MvpPresenter<ProfileView> {

    @Inject
    Context mContext;
    @Inject
    AccountSqlTool mAccountSqlTool;
    @Inject
    PreferenceTool mPreferenceTool;

    @Nullable
    private String mToken;
    private AccountsSqlData mAccount;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    public ProfilePresenter() {
        App.getApp().getAppComponent().inject(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    public void setAccount(AccountsSqlData account) {
        mAccount = account;
        mToken = account.getToken();
        setState();
    }

    private void setState() {
        if (mAccount.isWebDav()) {
            getViewState().onWebDavState();
        } else {
            getViewState().onWebDavState();
            getViewState().onCloudState();
            checkThirdparty();
        }
        if (mAccount.isOnline()) {
            getViewState().onOnlineState();
        }
    }

    public void getType(@Nullable String string) {
        if (string != null && !string.isEmpty()) {
            getViewState().onAccountType(string);
        } else if (mAccount.isOnline()) {
            mDisposable.add(getUserRequest()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(responseUserResponse -> {
                        if (responseUserResponse.isSuccessful() && responseUserResponse.body() != null){
                            return responseUserResponse.body();
                        } else {
                            throw new HttpException(responseUserResponse);
                        }
                    })
                    .subscribe(responseUser -> {
                        User user = responseUser.getResponse();
                        if (user.getIsVisitor()) {
                            getViewState().onAccountType(mContext.getString(R.string.profile_type_visitor));
                        } else if (user.getIsAdmin()) {
                            getViewState().onAccountType(mContext.getString(R.string.profile_type_admin));
                        } else {
                            getViewState().onAccountType(mContext.getString(R.string.profile_type_user));
                        }
                    }, throwable -> getViewState().onError(throwable.getMessage())));
        }
    }

    public void removeAccount() {
        if (mAccountSqlTool.delete(mAccount)) {
            if (mAccount.isOnline()) {
                mPreferenceTool.setDefault();
            }
            getViewState().onClose();
        }
    }

    private void checkThirdparty() {
        if (mToken != null && !mToken.isEmpty() && mAccount.isOnline()) {
            mDisposable.add(getThirdpartyRequest()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseThirdparty -> {
                        List<Thirdparty> list = responseThirdparty.getResponse();
                        if (list.isEmpty()) {
                            getViewState().onEmptyThirdparty();
                        } else {
                            getViewState().onSetServices(list);
                        }
                    }, throwable -> getViewState().onError(throwable.getMessage())));
        } else {
            getViewState().onEmptyThirdparty();
        }

    }

    private Api getApi() {
        RetrofitTool retrofitTool = new RetrofitTool(mContext);
        retrofitTool.setSslOn(mAccount.isSslState());
        retrofitTool.setCiphers(mAccount.isSslCiphers());
        return retrofitTool.getApi(mAccount.getScheme() + StringUtils.getEncodedString(mAccount.getPortal()));
    }

    private Observable<ResponseThirdparty> getThirdpartyRequest() {
        return getApi().getThirdPartyList(mToken);
    }

    private Observable<Response<ResponseUser>> getUserRequest() {
        return Observable.fromCallable(() -> getApi().getUserInfo(mToken).execute());
    }


    public void logout() {
        if (mAccount != null) {
            if (mAccount.isOnline()) {
                mPreferenceTool.setDefault();
            }
            mAccount.setOnline(false);
            mAccount.setPassword("");
            mAccount.setToken("");
            mAccountSqlTool.setAccount(mAccount);
            getViewState().onClose();
        }
    }
}
