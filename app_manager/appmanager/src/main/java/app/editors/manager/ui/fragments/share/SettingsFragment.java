package app.editors.manager.ui.fragments.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.TransitionManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import app.documents.core.network.ApiContract;
import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.models.models.ModelShareStack;
import app.editors.manager.mvp.models.ui.ShareUi;
import app.editors.manager.mvp.models.ui.ViewType;
import app.editors.manager.mvp.presenters.share.SettingsPresenter;
import app.editors.manager.mvp.views.share.SettingsView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.ShareActivity;
import app.editors.manager.ui.adapters.share.ShareAdapter;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import app.editors.manager.ui.views.popup.SharePopup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kotlin.Unit;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.presenter.InjectPresenter;


public class SettingsFragment extends BaseAppFragment implements SettingsView, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();
    public static final String TAG_ITEM = "TAG_ITEM";
    private static final String TAG_SHOW_POPUP = "TAG_SHOW_POPUP";
    private static final String TAG_POSITION_POPUP = "TAG_POSITION_POPUP";

    protected Unbinder mUnbinder;
    @BindView(R.id.placeholder_layout)
    protected ConstraintLayout mPlaceholderLayout;
    @BindView(R.id.share_settings_list_content_layout)
    protected LinearLayout mContentLayout;
    @BindView(R.id.share_settings_external_copy_link)
    protected AppCompatButton mShareCopyLink;
    @BindView(R.id.share_settings_external_send_link)
    protected AppCompatButton mShareSendLink;
    @BindView(R.id.share_main_list_of_items)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.share_settings_list_swipe_refresh)
    protected SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.share_settings_add_item)
    protected FloatingActionButton mFloatingActionButton;
    @BindView(R.id.share_settings_layout)
    protected CoordinatorLayout mShareLayout;

    /*
     * External access
     * */
    @BindView(R.id.share_settings_header_layout)
    protected ConstraintLayout mShareHeaderLayout;
    @BindView(R.id.share_settings_external_access_title)
    protected AppCompatTextView mShareExternalAccessTitle;
    @BindView(R.id.share_settings_external_access_info_title)
    protected AppCompatTextView mShareExternalAccessInfoTitle;
    @BindView(R.id.share_settings_external_access_frame_layout)
    protected FrameLayout mShareExternalAccessFrameLayout;
    @BindView(R.id.share_settings_external_access_layout)
    protected ConstraintLayout mShareExternalAccessLayout;

    /*
     * Popup item
     * */
    @BindView(R.id.button_popup_image)
    protected AppCompatImageView mShareAccessButtonView;
    @BindView(R.id.button_popup_arrow)
    protected AppCompatImageView mShareAccessButtonArrow;

    @InjectPresenter
    SettingsPresenter mSettingsPresenter;

    private MenuItem mShareItem;
    private SharePopup mSharePopup;
    private ShareActivity mShareActivity;
    private ShareAdapter mShareSettingsAdapter;
    private PlaceholderViews mPlaceholderViews;

    public static SettingsFragment newInstance(@NonNull final Item item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null!");
        }

        final SettingsFragment settingsFragment = new SettingsFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_ITEM, item);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mShareActivity = (ShareActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(SettingsFragment.class.getSimpleName() + " - must implement - " +
                    ShareActivity.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSharePopup != null && mSharePopup.isShowing()) {
            outState.putBoolean(TAG_SHOW_POPUP, mSharePopup.isShowing());
            outState.putInt(TAG_POSITION_POPUP, mSettingsPresenter.getSharePosition());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_share_settings_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSharePopup != null && mSharePopup.isShowing()) {
            mSharePopup.hide();
        }
        mUnbinder.unbind();
    }

    @Override
    public boolean onBackPressed() {
        if (mSharePopup != null && mSharePopup.isShowing()) {
            mSharePopup.hide();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.share_settings, menu);
        mShareItem = menu.findItem(R.id.menu_share_settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share_settings:
                mSettingsPresenter.getInternalLink();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.share_settings_external_copy_link,
            R.id.share_settings_external_send_link,
            R.id.share_settings_add_item,
            R.id.share_settings_access_button_layout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.share_settings_access_button_layout:
                if (getView() != null) {
                    showAccessPopup(getView());
                }
                break;
            case R.id.share_settings_external_copy_link:
                copySharedLinkToClipboard(mSettingsPresenter.getExternalLink(), getString(R.string.share_clipboard_external_copied));
                break;
            case R.id.share_settings_add_item:
                mSettingsPresenter.addShareItems();
                break;
            case R.id.share_settings_external_send_link:
                mSettingsPresenter.sendLink(mSettingsPresenter.getExternalLink());
        }
    }

    @Override
    public void onError(@Nullable String message) {
        mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION);
        mSwipeRefresh.setRefreshing(false);
        showSnackBar(message);
    }

    @Override
    public void onUnauthorized(@Nullable String message) {
        requireActivity().finish();
        MainActivity.show(getContext());
    }

    @Override
    public void onGetShare(@NotNull List<? extends ViewType> list, int accessCode) {
        mSwipeRefresh.setRefreshing(false);
        mShareSettingsAdapter.setItems((List<ViewType>) list);
        setExternalViewState(accessCode, false);
    }

    @Override
    public void onGetShareItem(ViewType entity, int position, int accessCode) {
        mSwipeRefresh.setRefreshing(false);
        mShareSettingsAdapter.setItem(entity, position);
        setExternalViewState(accessCode, false);
    }

    @Override
    public void onRemove(ShareUi share, int sharePosition) {
        mSwipeRefresh.setRefreshing(false);
        mShareSettingsAdapter.removeItem(share);
        if (mShareSettingsAdapter.getItemList().size() > 1
                && mShareSettingsAdapter.getItem(0) instanceof Header
                && mShareSettingsAdapter.getItem(1) instanceof Header) {
            mShareSettingsAdapter.removeHeader(getString(R.string.share_goal_user));
        }
    }

    @Override
    public void onExternalAccess(final int accessCode, boolean isMessage) {
        setExternalViewState(accessCode, isMessage);
    }

    @Override
    public void onInternalLink(@Nullable String internalLink) {
        copySharedLinkToClipboard(internalLink, getString(R.string.share_clipboard_internal_copied));
    }

    @Override
    public void onItemType(boolean isFolder) {
        mShareHeaderLayout.setVisibility(isFolder ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAddShare(Item item) {
        showFragment(AddPagerFragment.newInstance(item), AddFragment.TAG, false);
    }

    @Override
    public void onPlaceholderState(PlaceholderViews.Type type) {
        mPlaceholderViews.setTemplatePlaceholder(type);
    }

    @Override
    public void onActionButtonState(boolean isVisible) {
        if (isVisible) {
            mFloatingActionButton.show();
        } else {
            mFloatingActionButton.hide();
        }
    }

    @Override
    public void onResultState(boolean isShared) {
        final Intent intent = new Intent();
        intent.putExtra(ShareActivity.TAG_RESULT, isShared);
        requireActivity().setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public void onRefresh() {
        getSharedItems();
    }

    public void onItemContextClick(View view, int position) {
        ShareUi share = (ShareUi) mShareSettingsAdapter.getItem(position);
        if (share.isLocked()) {
            return;
        }
        mSettingsPresenter.setShared(share, position);
        setPopup(view);
    }

    private void setPopup(View view) {
        view.post(() -> {
            if (getContext() != null && getActivity() != null) {
                mSharePopup = new SharePopup(getContext(), R.layout.popup_share_menu);
                mSharePopup.setContextListener(mListContextListener);
                if (mSettingsPresenter.getItem() instanceof CloudFolder) {
                    mSharePopup.setIsFolder(true);
                } else {
                    StringUtils.Extension extension = StringUtils.getExtension(StringUtils
                            .getExtensionFromPath(mSettingsPresenter.getItem().getTitle()));
                    mSharePopup.setIsDoc(extension == StringUtils.Extension.DOC);
                }
                mSharePopup.setFullAccess(true);
                mSharePopup.showDropAt(view, getActivity());
            }
        });
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.share_title_main));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mShareActivity.expandAppBar();

        getArgs(savedInstanceState);
        initViews();
        restoreViews(savedInstanceState);
        ModelShareStack.getInstance().clearModel();
    }

    private void getArgs(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final Item item = (Item) bundle.getSerializable(TAG_ITEM);
        mSettingsPresenter.setItem(item);
    }

    private void initViews() {
        mFloatingActionButton.hide();
        mPlaceholderViews = new PlaceholderViews(mPlaceholderLayout);
        mPlaceholderViews.setViewForHide(mRecyclerView);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorAccent));
        mShareSettingsAdapter = new ShareAdapter((view, integer) -> {
            onItemContextClick(view, integer);
            return Unit.INSTANCE;

        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mShareSettingsAdapter);
        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);
    }

    private void restoreViews(final Bundle savedInstanceState) {
        mSettingsPresenter.updateSharedExternalState(false);
        mSettingsPresenter.updatePlaceholderState();
        mSettingsPresenter.updateHeaderState();

        if (savedInstanceState == null || ModelShareStack.getInstance().isRefresh()) {
            getSharedItems();
        } else {
            mSettingsPresenter.updateActionButtonState();
            mSettingsPresenter.updateSharedListState();
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(TAG_SHOW_POPUP)
                && savedInstanceState.containsKey(TAG_POSITION_POPUP)) {
            mSettingsPresenter.setIsPopupShow(true);
        } else {
            mSettingsPresenter.setIsPopupShow(false);
        }
    }

    private void getSharedItems() {
        mSwipeRefresh.setRefreshing(true);
        mSettingsPresenter.getShared();
    }

    private void setExternalViewState(final int accessCode, final boolean isMessage) {
        @StringRes int messageRes = R.string.share_access_denied;
        @DrawableRes int iconRes = R.drawable.ic_access_deny;
        switch (accessCode) {
            case ApiContract.ShareCode.NONE:
            case ApiContract.ShareCode.RESTRICT:
                iconRes = R.drawable.ic_access_deny;
                messageRes = R.string.share_access_denied;
                onButtonState(false);
                break;
            case ApiContract.ShareCode.REVIEW:
                iconRes = R.drawable.ic_access_review;
                messageRes = R.string.share_access_success;
                onButtonState(true);
                break;
            case ApiContract.ShareCode.READ:
                iconRes = R.drawable.ic_access_read;
                messageRes = R.string.share_access_success;
                onButtonState(true);
                break;
            case ApiContract.ShareCode.READ_WRITE:
                iconRes = R.drawable.ic_access_full;
                messageRes = R.string.share_access_success;
                onButtonState(true);
                break;
            case ApiContract.ShareCode.COMMENT:
                iconRes = R.drawable.ic_access_comment;
                messageRes = R.string.share_access_success;
                onButtonState(true);
                break;
            case ApiContract.ShareCode.FILL_FORMS:
                iconRes = R.drawable.ic_access_fill_form;
                messageRes = R.string.share_access_success;
                onButtonState(true);
                break;
        }
        mShareAccessButtonView.setImageResource(iconRes);
        if (isMessage) {
            showSnackBar(messageRes);
        }
    }

    @Override
    public void onButtonState(boolean isVisible) {
        if (mShareExternalAccessLayout != null) {
            TransitionManager.beginDelayedTransition(mShareExternalAccessLayout);
            mShareExternalAccessLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onPopupState(boolean state) {
        mShareAccessButtonView.setVisibility(View.GONE);
        mShareAccessButtonArrow.setVisibility(View.GONE);
    }

    @Override
    public void onShowPopup(int sharePosition) {
        if (mRecyclerView != null) {
            mRecyclerView.post(() -> {
                if (sharePosition != 0) {
                    setPopup(mRecyclerView.getLayoutManager().findViewByPosition(sharePosition).findViewById(R.id.button_popup_arrow));
                } else {
                    if (getView() != null) {
                        showAccessPopup(getView());
                    }
                }
            });
        } else {
            setPopup(mContentLayout);
        }
    }

    private void showAccessPopup(View view) {
        mSharePopup = new SharePopup(requireContext(), R.layout.popup_share_menu);
        mSharePopup.setContextListener(mExternalContextListener);
        mSharePopup.setFullAccess(false);
        if (mSettingsPresenter.getItem() instanceof CloudFolder) {
            mSharePopup.setIsFolder(true);
        } else {
            StringUtils.Extension extension = StringUtils.getExtension(StringUtils
                    .getExtensionFromPath(mSettingsPresenter.getItem().getTitle()));
            mSharePopup.setIsDoc(extension == StringUtils.Extension.DOC);
        }
        mSharePopup.showDropAt(view.findViewById(R.id.share_settings_access_button_layout), requireActivity());
    }

    @Override
    public void onSendLink(Intent intent) {
        startActivity(Intent.createChooser(intent, getString(R.string.operation_share_send_link)));
    }

    private SharePopup.PopupContextListener mListContextListener = new SharePopup.PopupContextListener() {
        @Override
        public void onContextClick(View v, SharePopup sharePopup) {
            sharePopup.hide();
            switch (v.getId()) {
                case R.id.popup_share_access_full:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.READ_WRITE);
                    break;
                case R.id.popup_share_access_review:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.REVIEW);
                    break;
                case R.id.popup_share_access_read:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.READ);
                    break;
                case R.id.popup_share_access_deny:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.RESTRICT);
                    break;
                case R.id.popup_share_access_remove:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.NONE);
                    break;
                case R.id.popup_share_access_comment:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.COMMENT);
                    break;
                case R.id.popup_share_access_fill_forms:
                    mSettingsPresenter.setItemAccess(ApiContract.ShareCode.FILL_FORMS);
                    break;
            }
        }
    };

    private SharePopup.PopupContextListener mExternalContextListener = new SharePopup.PopupContextListener() {

        @Override
        public void onContextClick(View v, SharePopup sharePopup) {
            sharePopup.hide();
            switch (v.getId()) {
                case R.id.popup_share_access_full:
                    mSettingsPresenter.getExternalLink(ApiContract.ShareType.READ_WRITE);
                    break;
                case R.id.popup_share_access_review:
                    mSettingsPresenter.getExternalLink(ApiContract.ShareType.REVIEW);
                    break;
                case R.id.popup_share_access_read:
                    mSettingsPresenter.getExternalLink(ApiContract.ShareType.READ);
                    break;
                case R.id.popup_share_access_deny:
                    mSettingsPresenter.getExternalLink(ApiContract.ShareType.NONE);
                    break;
                case R.id.popup_share_access_comment:
                    mSettingsPresenter.getExternalLink(ApiContract.ShareType.COMMENT);
                    break;
                case R.id.popup_share_access_fill_forms:
                    mSettingsPresenter.getExternalLink(ApiContract.ShareType.FILL_FORMS);
                    break;
            }
        }

    };

}
