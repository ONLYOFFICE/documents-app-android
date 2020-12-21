package app.editors.manager.mvp.views.main;

import android.graphics.drawable.Drawable;

import androidx.annotation.StringRes;

import com.google.android.play.core.review.ReviewInfo;

import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.views.base.BaseViewExt;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface MainActivityView extends BaseViewExt {
    void onDialogClose();
    void onRemotePlayMarket(@StringRes int title, @StringRes int info, @StringRes int accept, @StringRes int cancel);
    void onRemoteApp(@StringRes int title, @StringRes int info, @StringRes int accept, @StringRes int cancel);
    void onRatingApp();
    void onAccountAvatar(Drawable resource);
    void onQuestionDialog(String title, String tag, String accept, String cancel, String question);
    void onShowEditMultilineDialog(String title, String hint, String accept, String cancel, String tag);
    void onShowPlayMarket(String releaseId);
    void onShowInAppReview(ReviewInfo mReviewInfo);
    void onShowApp(String releaseId);
    void onShowEmailClientTemplate(String value);
    void onShowOnBoarding();
    void onShowToolbarAccount(String portal, String login, boolean isVisible);
    void onSetWebDavImage(WebDavApi.Providers provider);
    void onShowRecentFragment(boolean isRestore);
    void onShowWebDavFragment(boolean isRestore, WebDavApi.Providers provider);
    void onShowCloudFragment(boolean isRestore, boolean isNoPortal);
    void onShowOnDeviceFragment(boolean isRestore);
    void onShowAccountsFragment(boolean isRestore);
    void onShowProfileFragment();
    void onClearStack();
    void onCloseActionDialog();
}