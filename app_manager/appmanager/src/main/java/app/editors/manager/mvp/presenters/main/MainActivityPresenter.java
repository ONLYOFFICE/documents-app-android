package app.editors.manager.mvp.presenters.main;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import app.editors.manager.BuildConfig;
import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.main.MainActivityView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.tools.GlideTool;
import lib.toolkit.base.managers.utils.NetworkUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.InjectViewState;
import retrofit2.HttpException;

@InjectViewState
public class MainActivityPresenter extends BasePresenter<MainActivityView, ResponseUser>
        implements FirebaseUtils.OnRatingApp {

    public static final String TAG = MainActivityPresenter.class.getSimpleName();

    public static final String TAG_DIALOG_REMOTE_PLAY_MARKET = "TAG_DIALOG_REMOTE_PLAY_MARKET";
    public static final String TAG_DIALOG_REMOTE_APP = "TAG_DIALOG_REMOTE_APP";
    public static final String TAG_DIALOG_RATE_FIRST = "TAG_DIALOG_RATE_FIRST";
    private static final String TAG_DIALOG_RATE_SECOND = "TAG_DIALOG_RATE_SECOND";
    private static final String TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK";

    private static final long DEFAULT_RATE_SESSIONS = 5;

    private static boolean sIsAppColdStart = true;
    private AccountsSqlData mAccountsSqlData;
    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Inject
    GlideTool mGlideTool;

    @Nullable
    private AccountsSqlData mAccount;
    private boolean mRestoreFlag = false;
    private boolean mIsDialogOpen = false;

    @Nullable
    private ReviewInfo mReviewInfo;

    public MainActivityPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        mPreferenceTool.setUserSession();
        if (sIsAppColdStart) {
            sIsAppColdStart = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }

    @Override
    public void onRatingApp(boolean isRating) {
        if (isRating) {
            if (mPreferenceTool.getIsRateOn() && (mPreferenceTool.getUserSession() % DEFAULT_RATE_SESSIONS) == 0) {
                getViewState().onRatingApp();
            }
        }
    }

    public void getRemoteConfigRate() {
        if (!BuildConfig.DEBUG) {
            mDisposable.add(Observable.just(1)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> FirebaseUtils.checkRatingConfig(MainActivityPresenter.this)));
        }
    }

    public void onAcceptClick(@Nullable String value, @Nullable String tag) {
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_REMOTE_PLAY_MARKET:
                    getViewState().onShowPlayMarket(BuildConfig.RELEASE_ID);
                    getViewState().onDialogClose();
                    break;
                case TAG_DIALOG_REMOTE_APP:
                    getViewState().onShowApp(BuildConfig.RELEASE_ID);
                    getViewState().onDialogClose();
                    break;
                case TAG_DIALOG_RATE_FIRST:
                    getReviewInfo();
                    getViewState().onQuestionDialog(mContext.getString(R.string.dialogs_question_rate_second_info), TAG_DIALOG_RATE_SECOND,
                            mContext.getString(R.string.dialogs_question_accept_sure),
                            mContext.getString(R.string.dialogs_question_accept_no_thanks), null);
                    break;
                case TAG_DIALOG_RATE_SECOND:
                    mPreferenceTool.setIsRateOn(false);
                    if (mReviewInfo != null) {
                        getViewState().onShowInAppReview(mReviewInfo);
                    } else {
                        getViewState().onShowPlayMarket(BuildConfig.RELEASE_ID);
                    }
                    getViewState().onDialogClose();
                    break;
                case TAG_DIALOG_RATE_FEEDBACK:
                    if (value != null) {
                        getViewState().onShowEmailClientTemplate(value);
                    }
                    getViewState().onDialogClose();
                    break;
            }
        }
    }

    private void getReviewInfo() {
        ReviewManagerFactory.create(mContext).requestReviewFlow().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mReviewInfo = task.getResult();
            }
        });
    }

    public void onCancelClick(String tag) {
        if (tag != null) {
            switch (tag) {
                case TAG_DIALOG_RATE_FIRST:
                    mPreferenceTool.setIsRateOn(false);
                    getViewState().onShowEditMultilineDialog(mContext.getString(R.string.dialogs_edit_feedback_rate_title),
                            mContext.getString(R.string.dialogs_edit_feedback_rate_hint),
                            mContext.getString(R.string.dialogs_edit_feedback_rate_accept),
                            mContext.getString(R.string.dialogs_question_accept_no_thanks), TAG_DIALOG_RATE_FEEDBACK);
                    return;
                case TAG_DIALOG_RATE_SECOND:
                    mPreferenceTool.setIsRateOn(false);
                    getViewState().onDialogClose();
                    break;
            }
        }
    }

    public void checkOnBoarding() {
        if (!mPreferenceTool.getOnBoarding()) {
            getViewState().onShowOnBoarding();
        }
    }

    public void navigationItemClick(int itemId) {

        getViewState().onCloseActionDialog();

        if (!mRestoreFlag) {
            getViewState().onClearStack();
        }
        switch (itemId) {
            case R.id.menu_item_recent:
                getViewState().onShowRecentFragment(mRestoreFlag);
                break;
            case R.id.menu_item_cloud:
                if (mAccount != null && !mPreferenceTool.isNoPortal()) {
                    if (mAccount.isWebDav()) {
                        getViewState().onShowWebDavFragment(mRestoreFlag, WebDavApi.Providers.valueOf(mAccount.getWebDavProvider()));
                    } else {
                        getViewState().onShowCloudFragment(mRestoreFlag, false);
                    }
                } else {
                    getViewState().onShowCloudFragment(mRestoreFlag, true);
                    cancelGetInfo();
                }
                break;
            case R.id.menu_item_on_device:
                getViewState().onShowOnDeviceFragment(mRestoreFlag);
                cancelGetInfo();
                break;
            case R.id.menu_item_setting:
                if (mAccountSqlTool.getAccounts() != null && !mAccountSqlTool.getAccounts().isEmpty()) {
                    getViewState().onShowAccountsFragment(mRestoreFlag);
                } else {
                    getViewState().onShowProfileFragment();
                }
                cancelGetInfo();
                break;
        }
        mRestoreFlag = false;
    }

    public void setAccount() {
        mAccount = mAccountSqlTool.getAccountOnline();
    }

    public void setToolbarAccount(boolean isVisible) {
        final AccountsSqlData account = mAccountSqlTool.getAccountOnline();
        if (account != null) {
            final String portal = account.getPortal();
            String login;
            if (account.isWebDav()) {
                login = account.getLogin();
                getViewState().onSetWebDavImage(WebDavApi.Providers.valueOf(account.getWebDavProvider()));
            } else {
                loadAvatar(account);
                login = account.getName();
            }
            getViewState().onShowToolbarAccount(portal, login, isVisible);
        }
    }

    public void setRestore(boolean isRestore) {
        mRestoreFlag = isRestore;
    }

    public void checkPortal() {
        if (mAccount != null && mAccount.isWebDav() && mAccount.getPassword() != null && !mAccount.getPassword().isEmpty()) {
            mPreferenceTool.setNoPortal(false);
        } else if (mPreferenceTool.getLogin() != null && mPreferenceTool.getToken() != null) {
            mPreferenceTool.setNoPortal(false);
        } else {
            mPreferenceTool.setNoPortal(true);
        }
    }

    public void setUser() {
        if (mAccountsSqlData != null) {
            mPreferenceTool.setPortal(mAccountsSqlData.getPortal());
            mPreferenceTool.setSslCiphers(mAccountsSqlData.isSslCiphers());
            mPreferenceTool.setSslState(mAccountsSqlData.isSslState());
            mPreferenceTool.setScheme(mAccountsSqlData.getScheme());
            mPreferenceTool.setNoPortal(false);
            mPreferenceTool.setLogin(mAccountsSqlData.getLogin());
            mPreferenceTool.setToken(mAccountsSqlData.getToken());
            mPreferenceTool.setPassword(mAccountsSqlData.getPassword());
            try {
                mRetrofitTool.setSslOn(mAccountsSqlData.isSslState());
                mRetrofitTool.setCiphers(mAccountsSqlData.isSslCiphers());
                mRetrofitTool.init(mAccountsSqlData.getScheme() + StringUtils.getEncodedString(mAccountsSqlData.getPortal()));
            } catch (UrlSyntaxMistake urlSyntaxMistake) {
                urlSyntaxMistake.printStackTrace();
            }
        }
        mAccountsSqlData = null;
    }

    public void getAccount() {
        if (mPreferenceTool.getPortal() != null) {
            mAccountsSqlData = mAccountSqlTool
                    .getAccount(mPreferenceTool.getPortal(), mPreferenceTool.getLogin(), mPreferenceTool.getSocialProvider());
        } else {
            mAccountsSqlData = null;
        }
    }

    public void clearAccount() {
        mPreferenceTool.setDefault();
        AccountsSqlData accountsSqlData = mAccountSqlTool.getAccountOnline();
        if (accountsSqlData != null) {
            accountsSqlData.setOnline(false);
            mAccountSqlTool.setAccount(accountsSqlData);
        }
        if (mAccount != null) {
            mAccount.setOnline(false);
            mAccountSqlTool.setAccount(mAccount);
        }
        mAccount = null;
    }

    public void checkAccountInfo() {
        if (mAccount != null && !mAccount.isWebDav()) {
            mDisposable.add(Observable.fromCallable(() -> mRetrofitTool.getApiWithPreferences().getUserInfo(mPreferenceTool.getToken()).execute())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(responseUserResponse -> {
                        if (responseUserResponse.isSuccessful() && responseUserResponse.body() != null) {
                            return responseUserResponse.body().getResponse();
                        } else {
                            throw new HttpException(responseUserResponse);
                        }
                    })
                    .filter(user -> !user.getEmail().equals(""))
                    .subscribe(this::setUserInfo, throwable -> {
                        if (throwable instanceof HttpException) {
                            final int code = ((HttpException) throwable).code();
                            if (code == Api.HttpCodes.CLIENT_UNAUTHORIZED) {
                                getViewState().onUnauthorized(mContext.getString(R.string.errors_client_unauthorized));
                            }
                        }
                    }));

        }
    }

    private void setUserInfo(User user) {
        mPreferenceTool.setAdmin(user.getIsAdmin());
        mPreferenceTool.setOwner(user.getIsOwner());
        mPreferenceTool.setVisitor(user.getIsVisitor());
        if (mAccount != null) {
            if (!mAccount.getName().equals(user.getDisplayNameHtml())) {
                mAccount.setName(user.getDisplayNameHtml());
            }
            if (!mAccount.getLogin().equals(user.getEmail())) {
                mAccount.setLogin(user.getEmail());
            }
            if (!mAccount.getAvatarUrl().equals(user.getAvatar())) {
                mAccount.setAvatarUrl(user.getAvatar());
                loadAvatar(mAccount);
            }
            mAccountSqlTool.setAccount(mAccount);
            loadAvatar(mAccount);
            //getViewState().onShowToolbarAccount(mAccount.getPortal(), mAccount.getName(), true);
        }
    }

    private void loadAvatar(AccountsSqlData account) {
        if (NetworkUtils.isOnline(mContext)) {
            Glide.with(mContext)
                    .load(GlideUtils.getCorrectLoad(account.getAvatarUrl(), mPreferenceTool))
                    .apply(GlideUtils.getAvatarOptions())
                    .into(getSimpleTarget());
        }
    }

    private CustomTarget<Drawable> getSimpleTarget() {
        return new CustomTarget<Drawable>() {

            @Override
            public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                getViewState().onAccountAvatar(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (placeholder instanceof BitmapDrawable) {
                    getViewState().onAccountAvatar(placeholder);
                }

            }
        };
    }

    private void cancelGetInfo() {
        mDisposable.clear();
        getViewState().onShowToolbarAccount("", "", false);
    }

    public void setDialogOpen(boolean isOpen) {
        mIsDialogOpen = isOpen;
    }

    public boolean isDialogOpen() {
        return mIsDialogOpen;
    }

}
