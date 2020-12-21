package app.editors.manager.ui.activities.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.work.WorkManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.List;
import java.util.UUID;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.receivers.DownloadReceiver;
import app.editors.manager.mvp.presenters.main.MainActivityPresenter;
import app.editors.manager.mvp.views.main.MainActivityView;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.dialogs.AccountBottomDialog;
import app.editors.manager.ui.dialogs.ActionBottomDialog;
import app.editors.manager.ui.fragments.main.CloudAccountsFragment;
import app.editors.manager.ui.fragments.main.DocsMyFragment;
import app.editors.manager.ui.fragments.main.DocsOnDeviceFragment;
import app.editors.manager.ui.fragments.main.DocsRecentFragment;
import app.editors.manager.ui.fragments.main.DocsWebDavFragment;
import app.editors.manager.ui.fragments.main.MainPagerFragment;
import app.editors.manager.ui.fragments.main.OnlyOfficeCloudFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.FragmentUtils;
import lib.toolkit.base.managers.utils.PermissionUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;
import moxy.presenter.InjectPresenter;

public class MainActivity extends BaseAppActivity implements MainActivityView,
        BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_PAGE_POSITION = "KEY_PAGE_POSITION";

    private Unbinder mUnbinder;
    @BindView(R.id.app_layout)
    protected CoordinatorLayout mMainLayout;
    @BindView(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;
    @BindView(R.id.app_bar_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.app_bar_tabs)
    protected TabLayout mTabLayout;
    @BindView(R.id.frame_container)
    protected FrameLayout mFrameLayout;
    @BindView(R.id.accountContainer)
    protected ConstraintLayout mAccountLayout;
    @BindView(R.id.toolbarTitle)
    protected AppCompatTextView mToolbarTitle;
    @BindView(R.id.toolbarSubTitle)
    protected AppCompatTextView mToolbarSubTitle;
    @BindView(R.id.toolbarArrowIcon)
    protected AppCompatImageView mArrowImage;
    @BindView(R.id.toolbarIcon)
    protected AppCompatImageView mToolbarIcon;
    @BindView(R.id.bottom_navigation)
    protected BottomNavigationView mNavigationView;
    @BindView(R.id.app_floating_action_button)
    protected FloatingActionButton mFloatingActionButton;

    @InjectPresenter
    MainActivityPresenter mMainActivityPresenter;

    private AccountBottomDialog mAccountDialog;

    private View.OnClickListener mToolbarNavigationBack = v -> onBackPressed();

    private int mPagePosition = MainPagerFragment.PAGE_DOCS_MY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        init(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mNavigationView.getSelectedItemId());
        outState.putInt(KEY_PAGE_POSITION, getPagePosition());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_ACTIVITY_WEB_VIEWER) {
                mMainActivityPresenter.getRemoteConfigRate();
                if (data != null && data.hasExtra(WebViewerActivity.TAG_VIEWER_FAIL)) {
                    showSnackBar("BAD bad viewer activity... :(");
                }
            } else if (requestCode == REQUEST_ACTIVITY_PORTAL) {
                mMainActivityPresenter.setUser();
            }
        }
        if (requestCode == ProfileActivity.REQUEST_PROFILE) {
            mNavigationView.setSelectedItemId(R.id.menu_item_setting);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String action = intent.getAction();
        if (action != null && action.equals(DownloadReceiver.DOWNLOAD_ACTION_CANCELED)) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                WorkManager.getInstance().cancelWorkById(UUID.fromString(extras.getString(DownloadReceiver.EXTRAS_KEY_ID)));
            }
            return;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MainPagerFragment.TAG);
        if (fragment instanceof MainPagerFragment) {
            List<Fragment> fragments = fragment.getChildFragmentManager().getFragments();
            for (Fragment fr : fragments) {
                if (fr instanceof DocsMyFragment) {
                    ((DocsMyFragment) fr).getArgs(intent);
                }
            }
        }

        fragment = getSupportFragmentManager().findFragmentByTag(DocsWebDavFragment.TAG);
        if (fragment instanceof DocsWebDavFragment) {
            ((DocsWebDavFragment) fragment).getArgs(intent);
        }

        fragment = getSupportFragmentManager().findFragmentByTag(DocsOnDeviceFragment.TAG);
        if (fragment instanceof DocsOnDeviceFragment) {
            ((DocsOnDeviceFragment) fragment).getArgs(intent);
        }

        fragment = getSupportFragmentManager().findFragmentByTag(DocsRecentFragment.TAG);
        if (fragment instanceof DocsRecentFragment) {
            ((DocsRecentFragment) fragment).getArgs(intent);
        }

        intent.setData(null);
        intent.setClipData(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainActivityPresenter.getAccount();
    }

    @Override
    public void onBackStackChanged() {
        super.onBackStackChanged();
        setAppBarStates();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onAcceptClick(CommonDialog.Dialogs dialogs, @Nullable String value, @Nullable String tag) {
        super.onAcceptClick(dialogs, value, tag);
        mMainActivityPresenter.onAcceptClick(value, tag);

    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        mMainActivityPresenter.onCancelClick(tag);

    }

    @OnClick(R.id.accountContainer)
    void onAccountClick() {
        if (mAccountDialog != null && !mAccountDialog.isAdded()) {
            mAccountDialog.show(getSupportFragmentManager(), AccountBottomDialog.TAG);
        }
    }

    @OnClick(R.id.app_floating_action_button)
    public void onClick() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof MainPagerFragment && fragment.isVisible()) {
                ((MainPagerFragment) fragment).showActionDialog();
                break;
            } else if (fragment instanceof DocsOnDeviceFragment && fragment.isVisible()) {
                ((DocsOnDeviceFragment) fragment).showActionDialog();
                break;
            } else if (fragment instanceof DocsWebDavFragment && fragment.isVisible()) {
                ((DocsWebDavFragment) fragment).showActionDialog();
                break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mMainActivityPresenter.navigationItemClick(menuItem.getItemId());
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults.length > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    mNavigationView.setSelectedItemId(R.id.menu_item_cloud);
                } else {
                    FragmentUtils.showFragment(getSupportFragmentManager(), DocsOnDeviceFragment.newInstance(), R.id.frame_container);
                }
            }
        }
    }

    public void clearMenu() {
        mToolbar.getMenu().clear();
    }

    @Override
    public void onDialogClose() {
        hideDialog();
    }

    @Override
    public void onAccountAvatar(Drawable resource) {
        mToolbarIcon.setImageDrawable(resource);
    }

    @Override
    public void onQuestionDialog(String title, String tag, String accept, String cancel, String question) {
        showQuestionDialog(title, tag, accept, cancel, question);
    }

    @Override
    public void onShowEditMultilineDialog(String title, String hint, String accept, String cancel, String tag) {
        showEditMultilineDialog(title, hint, accept, cancel, tag);
    }

    @Override
    public void onShowPlayMarket(String releaseId) {
        showPlayMarket(releaseId);
    }

    @Override
    public void onShowInAppReview(ReviewInfo reviewInfo) {
        ReviewManagerFactory.create(this).launchReviewFlow(this, reviewInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onShowInAppReview: success");
                    } else {
                        Log.d(TAG, "onShowInAppReview: error");
                    }
                });
    }

    @Override
    public void onShowApp(String releaseId) {
        showApp(releaseId);
    }

    @Override
    public void onShowEmailClientTemplate(String value) {
        showEmailClientTemplate(value);
    }

    @Override
    public void onShowOnBoarding() {
        OnBoardingActivity.show(this);
    }

    @Override
    public void onRemotePlayMarket(@StringRes int title, @StringRes int info, @StringRes int accept, @StringRes int cancel) {
        showQuestionDialog(getString(title), MainActivityPresenter.TAG_DIALOG_REMOTE_PLAY_MARKET, getString(accept), getString(cancel), getString(info));
    }

    @Override
    public void onRemoteApp(@StringRes int title, @StringRes int info, @StringRes int accept, @StringRes int cancel) {
        showQuestionDialog(getString(title), MainActivityPresenter.TAG_DIALOG_REMOTE_APP, getString(accept), getString(cancel), getString(info));
    }

    @Override
    public void onRatingApp() {
        showQuestionDialog(getString(R.string.dialogs_question_rate_first_info), MainActivityPresenter.TAG_DIALOG_RATE_FIRST,
                getString(R.string.dialogs_question_accept_yes),
                getString(R.string.dialogs_question_accept_not_really), null);
    }

    @Override
    public void onError(@Nullable String message) {
        hideDialog();
        if (message != null) {
            if (message.equals(getString(R.string.errors_client_host_not_found))) {
                onUnauthorized(message);
            } else {
                showSnackBar(message);
            }
        }
    }

    @Override
    public void onUnauthorized(@Nullable String message) {
        if (message != null) {
            showSnackBar(message);
        }
        mMainActivityPresenter.clearAccount();
        mNavigationView.setSelectedItemId(R.id.menu_item_cloud);
    }


    @Override
    public void onShowToolbarAccount(String portal, String login, boolean isVisible) {
        if (isVisible) {
            mToolbar.post(() -> {
                mToolbarSubTitle.setText(portal);
                mToolbarTitle.setText(login);
                mAccountLayout.setVisibility(View.VISIBLE);
                mArrowImage.setVisibility(View.VISIBLE);
            });
        } else {
            setOffToolbarAccount();
        }
    }

    @Override
    public void onSetWebDavImage(WebDavApi.Providers provider) {
        switch (provider) {
            case Yandex:
                mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_storage_yandex));
                break;
            case NextCloud:
                mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_storage_nextcloud));
                break;
            case OwnCloud:
                mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_storage_owncloud));
                break;
            case WebDav:
                mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_storage_webdav));
                break;
        }
    }

    @Override
    public void onShowRecentFragment(boolean isRestore) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DocsRecentFragment.TAG);
        if (fragment == null || !isRestore) {
            fragment = DocsRecentFragment.newInstance();
        }
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
    }

    @Override
    public void onShowWebDavFragment(boolean isRestore, WebDavApi.Providers provider) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DocsWebDavFragment.TAG);
        if (fragment == null || !isRestore) {
            fragment = DocsWebDavFragment.newInstance(provider);
        }
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
    }

    @Override
    public void onShowCloudFragment(boolean isRestore, boolean isNoPortal) {
        if (isNoPortal) {
            FragmentUtils.showFragment(getSupportFragmentManager(), OnlyOfficeCloudFragment.newInstance(false), R.id.frame_container);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(MainPagerFragment.TAG);
            if (fragment == null || !isRestore) {
                fragment = MainPagerFragment.newInstance(mPagePosition);
            }
            FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
        }

    }

    @Override
    public void onShowOnDeviceFragment(boolean isRestore) {
        if (PermissionUtils.requestReadWritePermission(this, PERMISSION_READ_STORAGE)) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(DocsOnDeviceFragment.TAG);
            if (fragment == null || !isRestore) {
                fragment = DocsOnDeviceFragment.newInstance();
            }
            FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
        }
    }

    @Override
    public void onShowAccountsFragment(boolean isRestore) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CloudAccountsFragment.TAG);
        if (fragment == null || !isRestore) {
            fragment = CloudAccountsFragment.newInstance();
        }
        FragmentUtils.showFragment(getSupportFragmentManager(), fragment, R.id.frame_container);
    }

    @Override
    public void onShowProfileFragment() {
        FragmentUtils.showFragment(getSupportFragmentManager(), OnlyOfficeCloudFragment.newInstance(true), R.id.frame_container);
    }

    @Override
    public void onClearStack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onCloseActionDialog() {
        Fragment actionBottomDialog = getSupportFragmentManager().findFragmentByTag(ActionBottomDialog.TAG);
        if(actionBottomDialog instanceof ActionBottomDialog) {
            ((ActionBottomDialog) actionBottomDialog).dismiss();
        }
    }

    /*
     * Init methods
     * */
    private void init(final Bundle savedInstanceState) {
        mIsBackStackNotice = true;

        mMainActivityPresenter.setAccount();
        mMainActivityPresenter.checkPortal();
        mMainActivityPresenter.checkOnBoarding();
        mAccountDialog = new AccountBottomDialog();
        initViews();
        initToolbar(savedInstanceState);
        setAppBarStates();
        restoreViews(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    private void initViews() {
        UiUtils.removePaddingFromNavigationItem(mNavigationView);
        mNavigationView.setOnNavigationItemSelectedListener(this);
        mFloatingActionButton.setVisibility(View.GONE);
    }

    private void restoreViews(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMainActivityPresenter.setRestore(true);
            if (savedInstanceState.containsKey(KEY_PAGE_POSITION)) {
                mPagePosition = savedInstanceState.getInt(KEY_PAGE_POSITION);
            }

            if (savedInstanceState.containsKey(KEY_POSITION)) {
                mNavigationView.setSelectedItemId(savedInstanceState.getInt(KEY_POSITION));
            }
        } else {
            mMainActivityPresenter.setRestore(false);
            mNavigationView.setSelectedItemId(R.id.menu_item_cloud);
        }
    }

    private void initToolbar(final Bundle savedInstanceState) {
        setSupportActionBar(mToolbar);
        if (savedInstanceState == null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.actionbar_title_main));
        }
    }

    public void setPagePosition(int position) {
        mPagePosition = position;
    }

    private int getPagePosition() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MainPagerFragment.TAG);
        if (fragment instanceof MainPagerFragment) {
            return ((MainPagerFragment) fragment).getPosition();
        } else {
            return MainPagerFragment.PAGE_DOCS_MY;
        }
    }

    public void setCloudsState(boolean mIsBack) {
        setActionButtonShow(false);
        setAppBarStates(false);
        setAppBarMode(false);
        if (mIsBack) {
            setNavigationButton(false);
        } else {
            setNavigationButton(true);
        }
        clearMenu();
    }

    /*
     * Change states:
     *   1) Navigation button
     *   2) TabLayout animation
     *   3) AppBar scrolling
     * */
    public void setAppBarStates(final boolean isVisible) {
        setToolbarAccount(isVisible);
        setAnimation();

        setAppBarMode(isVisible);
        setNavigationButton(isVisible);
        mTabLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void setAnimation() {
        Transition transition = new AutoTransition();
        transition.setDuration(200);
        transition.excludeChildren(R.id.list_swipe_refresh, true);
        transition.excludeChildren(mNavigationView, true);
        transition.excludeChildren(mToolbar, true);
        transition.excludeChildren(mTabLayout, true);
        TransitionManager.beginDelayedTransition(mMainLayout, transition);
    }

    public void setToolbarAccount(boolean isVisible) {
        mMainActivityPresenter.setToolbarAccount(isVisible);
    }

    public void setOffToolbarAccount() {
        mAccountLayout.post(() -> {
            if (mAccountLayout != null) {
                mAccountLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setAppBarStates() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();
//        setNavigationButton(count <= 0);
        setAppBarMode(count <= 0);
    }

    public void showAppBarLayout(boolean isVisible) {
        mAppBarLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setNavigationButton(final boolean isRootScreen) {
        if (getSupportActionBar() != null) {
            if (isRootScreen) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            } else {
                mToolbar.setNavigationOnClickListener(mToolbarNavigationBack);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    /*
     * Action button
     * */
    public void setActionButtonShow(final boolean isShow) {
        mFloatingActionButton.post( () -> {
            if (isShow) {
                mFloatingActionButton.show();
            } else {
                mFloatingActionButton.hide();
            }
        });
    }

    public void setActionButtonVisibility(final boolean isVisible) {
        final int newVisibility = isVisible ? View.VISIBLE : View.GONE;
        final int buttonVisibility = mFloatingActionButton.getVisibility();
        if (newVisibility != buttonVisibility) {
            mFloatingActionButton.setVisibility(newVisibility);
        }
    }

    public void expandFloatingActionButton() {
        setActionButtonShow(true);
    }

    /*
     * AppBar/TabLayout changes
     * */
    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    public void expandToolBar() {
        expandAppBar(mAppBarLayout, false);
    }

    public void setAppBarMode(final boolean isScroll) {
        if (isTablet()) {
            setAppBarFix(mToolbar);
        } else {
            if (isScroll) {
                setAppBarScroll(mToolbar);
            } else {
                setAppBarFix(mToolbar);
            }
        }
    }

    public BottomNavigationView getNavBar() {
        return mNavigationView;
    }

    public void checkAccountInfo() {
        mMainActivityPresenter.checkAccountInfo();
    }

    public static void show(final Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

}