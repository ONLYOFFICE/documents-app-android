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
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;

import moxy.presenter.InjectPresenter;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.base.ItemProperties;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.models.ModelShareStack;
import app.editors.manager.mvp.presenters.share.AddPresenter;
import app.editors.manager.mvp.views.share.AddView;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.ShareActivity;
import app.editors.manager.ui.adapters.ShareAddAdapter;
import app.editors.manager.ui.fragments.base.ListFragment;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import app.editors.manager.ui.views.custom.SharePanelViews;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AddSearchFragment extends ListFragment implements AddView, SearchView.OnQueryTextListener,
        ShareAddAdapter.OnItemClickListener, SharePanelViews.OnEventListener {

    public static final String TAG = AddSearchFragment.class.getSimpleName();
    public static final String TAG_ITEM = "TAG_ITEM";

    /*
     * Panel layout
     * */
    @BindView(R.id.share_panel_layout)
    protected CardView mSharePanelLayout;

    @InjectPresenter
    AddPresenter mAddPresenter;

    private ShareActivity mShareActivity;
    private ShareAddAdapter mShareAddAdapter;
    private SharePanelViews mSharePanelViews;

    private Menu mToolbarMenu;
    private MenuItem mSearchItem;
    private SearchView mSearchView;

    public static AddSearchFragment newInstance(final Item item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null!");
        }

        final AddSearchFragment settingsFragment = new AddSearchFragment();
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
            throw new RuntimeException(ShareActivity.class.getSimpleName() + " - must implement - " +
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
        final View view = inflater.inflate(R.layout.fragment_share_add_list_search, container, false);
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
        mSharePanelViews.unbind();
    }

    @Override
    public boolean onBackPressed() {
        if (mSharePanelViews.popupDismiss()) {
            return true;
        }

        if (mSharePanelViews.hideMessageView()) {
            return true;
        }

        if (mSearchView.getQuery().length() > 0) {
            mSearchView.setQuery("", true);
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.share_add_search, menu);
        mToolbarMenu = menu;
        mSearchItem = menu.findItem(R.id.menu_share_add_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconified(false);
        mAddPresenter.updateSearchState();

        // Action on close search
        mSearchView.setOnCloseListener(() -> {
            final CharSequence charSequence = mSearchView.getQuery();
            if (charSequence.length() > 0) {
                mSearchView.setQuery("", true);
                return true;
            }

            getActivity().onBackPressed();
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_item_search:
                item.setChecked(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        mSearchView.setQuery("", false);
        mAddPresenter.getCommons();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAddPresenter.setSearchValue(newText);
        mShareAddAdapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public void onItemClick(View view, int position) {
        final ItemProperties itemProperties = (ItemProperties) mShareAddAdapter.getItem(position);
        itemProperties.setSelected(!itemProperties.isSelected());
        mShareAddAdapter.notifyItemChanged(position);
        setCountChecked();
    }

    @Override
    public void onPanelAccessClick(final int accessCode) {
        mAddPresenter.setAccessCode(accessCode);
    }

    @Override
    public void onPanelResetClick() {
        mAddPresenter.resetChecked();
        mSharePanelViews.setCount(0);
        mSharePanelViews.setAddButtonEnable(false);
        mShareAddAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPanelMessageClick(boolean isShow) {
        // Stub
    }

    @Override
    public void onPanelAddClick() {
        mSwipeRefresh.setRefreshing(true);
        mAddPresenter.shareItem();
    }

    @Override
    public void onMessageInput(String message) {
        mAddPresenter.setMessage(message);
    }

    @Override
    public void onError(@Nullable String message) {
        mSwipeRefresh.setRefreshing(false);
        showSnackBar(message);
        if (mShareAddAdapter.getItemCount() == 0) {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION);
        }
    }

    @Override
    public void onUnauthorized(@Nullable String message) {
        getActivity().finish();
        MainActivity.show(getContext());
    }

    @Override
    public void onGetUsers(List<Entity> list) {
        // Stub
    }

    @Override
    public void onGetGroups(List<Entity> list) {
        // Stub
    }

    @Override
    public void onGetCommon(List<Entity> list) {
        setPlaceholder(list != null && !list.isEmpty());
        mSwipeRefresh.setRefreshing(false);
        mShareAddAdapter.setMode(ShareAddAdapter.Mode.COMMON);
        mShareAddAdapter.setItems(list);
    }

    @Override
    public void onSuccessAdd() {
        ModelShareStack.getInstance().setRefresh(true);
        showRootFragment();
    }

    @Override
    public void onSearchValue(@Nullable String value) {
        if (mSearchView != null && value != null) {
            mSearchView.setQuery(value, false);
        }
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.share_title_search));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mShareActivity.expandAppBar();

        getArgs(savedInstanceState);
        initViews();
        restoreViews(savedInstanceState);
    }

    private void getArgs(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        mAddPresenter.setItem((Item) bundle.getSerializable(TAG_ITEM));
    }

    private void initViews() {
        mSharePanelViews = new SharePanelViews(mSharePanelLayout, getActivity());
        mSharePanelViews.setOnEventListener(this);
        mSharePanelViews.setAccessIcon(mAddPresenter.getAccessCode());
        mShareAddAdapter = new ShareAddAdapter();
        mShareAddAdapter.setOnItemClickListener(this);
        mShareAddAdapter.setMode(ShareAddAdapter.Mode.COMMON);
        mRecyclerView.setAdapter(mShareAddAdapter);
        setCountChecked();
    }

    private void restoreViews(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAddPresenter.updateCommonSharedListState();
        } else {
            mAddPresenter.getCommons();
        }
    }

    private void setCountChecked() {
        final int countChecked = mAddPresenter.getCountChecked();
        mSharePanelViews.setCount(countChecked);
        mSharePanelViews.setAddButtonEnable(countChecked > 0);
    }

    private void setPlaceholder(final boolean isEmpty) {
        mPlaceholderViews.setTemplatePlaceholder(isEmpty ? PlaceholderViews.Type.NONE : PlaceholderViews.Type.COMMON);
    }

}
