package app.editors.manager.ui.fragments.share;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.models.ModelShareStack;
import app.editors.manager.ui.activities.main.ShareActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.animation.HeightValueAnimator;
import app.editors.manager.ui.views.custom.SharePanelViews;
import app.editors.manager.ui.views.pager.PagingViewPager;
import app.editors.manager.ui.views.pager.ViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AddPagerFragment extends BaseAppFragment implements SharePanelViews.OnEventListener {

    public static final String TAG = AddPagerFragment.class.getSimpleName();
    public static final String TAG_ITEM = "TAG_ITEM";

    private static final int ANIMATION_DURATION = 200;

    /*
     * Pager layout
     * */
    @BindView(R.id.share_add_view_pager)
    protected PagingViewPager mViewPager;

    /*
     * Panel layout
     * */
    @BindView(R.id.share_panel_layout)
    protected CardView mSharePanelLayout;

    private Unbinder mUnbinder;
    private ModelShareStack mModelShareStack;
    private ShareActivity mShareActivity;
    private SharePanelViews mSharePanelViews;
    private HeightValueAnimator mHeightValueAnimator;
    private ViewPagerAdapter mViewPagerAdapter;
    private Item mInputItem;

    public static AddPagerFragment newInstance(final Item item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null!");
        }

        final AddPagerFragment settingsFragment = new AddPagerFragment();
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
            throw new RuntimeException(AddPagerFragment.class.getSimpleName() + " - must implement - " +
                    ShareActivity.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_share_add_pager, container, false);
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
        showTabLayout(false);
        mSharePanelViews.popupDismiss();
        mSharePanelViews.unbind();
        mUnbinder.unbind();
    }

    @Override
    public boolean onBackPressed() {
        if (mSharePanelViews.popupDismiss()) {
            return true;
        }

        if (mSharePanelViews.hideMessageView()) {
            mShareActivity.expandAppBar();
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.share_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share_add_search:
                showFragment(AddSearchFragment.newInstance(mInputItem), AddSearchFragment.TAG, false);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPanelAccessClick(final int accessCode) {
        mModelShareStack.setAccessCode(accessCode);
    }

    @Override
    public void onPanelResetClick() {
        resetChecked();
        updateAdaptersFragments();
    }

    @Override
    public void onPanelMessageClick(boolean isShow) {
        if (isShow) {
            mShareActivity.collapseAppBar();
        } else {
            mShareActivity.expandAppBar();
        }
    }

    @Override
    public void onPanelAddClick() {
        requestAddFragments();
    }

    @Override
    public void onMessageInput(String message) {
        setMessageAddFragments(message);
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.share_title_add));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mShareActivity.expandAppBar();
        mHeightValueAnimator = new HeightValueAnimator(mShareActivity.getTabLayout(), ANIMATION_DURATION);
        showTabLayout(true);

        getArgs();
        initViews();
    }

    private void showTabLayout(final boolean isShow) {
        if (isTablet()) {
            mShareActivity.getTabLayout().setVisibility(isShow ? View.VISIBLE : View.GONE);
        } else {
            mHeightValueAnimator.animate(isShow);
        }
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mInputItem = (Item) bundle.getSerializable(TAG_ITEM);
        mModelShareStack = ModelShareStack.getInstance();
    }

    private void initViews() {
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), getFragments());
        mViewPager.addOnPageChangeListener(mViewPagerAdapter);
        mViewPager.setAdapter(mViewPagerAdapter);
        mShareActivity.getTabLayout().setupWithViewPager(mViewPager, true);
        mSharePanelViews = new SharePanelViews(mSharePanelLayout, getActivity());
        mSharePanelViews.setOnEventListener(this);
        mSharePanelViews.setAccessIcon(mModelShareStack.getAccessCode());

        setChecked();
    }

    public void setChecked() {
        final int countChecked = mModelShareStack.getCountChecked();
        mSharePanelViews.setCount(countChecked);
        mSharePanelViews.setAddButtonEnable(countChecked > 0);
    }

    public void resetChecked() {
        mModelShareStack.resetChecked();
        mSharePanelViews.setCount(0);
    }

    public boolean isActivePage(final Fragment fragment) {
        return mViewPagerAdapter.isActiveFragment(mViewPager, fragment);
    }

    private void updateAdaptersFragments() {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            ((AddFragment) fragment).updateAdapterState();
        }
    }

    private void requestAddFragments() {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            ((AddFragment) fragment).addAccess();
            return;
        }
    }

    private void setMessageAddFragments(final String message) {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            ((AddFragment) fragment).setMessage(message);
        }
    }

    private List<ViewPagerAdapter.Container> getFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        pairs.add(new ViewPagerAdapter.Container(AddFragment.newInstance(mInputItem, AddFragment.Type.USERS),
                getString(R.string.share_tab_users)));
        pairs.add(new ViewPagerAdapter.Container(AddFragment.newInstance(mInputItem, AddFragment.Type.GROUPS),
                getString(R.string.share_tab_groups)));
        return pairs;
    }

}
