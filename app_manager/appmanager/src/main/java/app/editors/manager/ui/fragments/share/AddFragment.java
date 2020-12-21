package app.editors.manager.ui.fragments.share;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import app.editors.manager.ui.adapters.ShareAddAdapter;
import app.editors.manager.ui.fragments.base.ListFragment;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import butterknife.ButterKnife;

public class AddFragment extends ListFragment implements AddView, ShareAddAdapter.OnItemClickListener {

    public static final String TAG = AddFragment.class.getSimpleName();
    public static final String TAG_ITEM = "TAG_ITEM";
    public static final String TAG_TYPE = "TAG_TYPE";

    public enum Type {
        USERS, GROUPS
    }

    @InjectPresenter
    AddPresenter mAddPresenter;

    private ShareAddAdapter mShareAddAdapter;

    public static AddFragment newInstance(final Item item, final Type type) {
        if (item == null) {
            throw new NullPointerException("Item must not be null!");
        }

        final AddFragment settingsFragment = new AddFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_ITEM, item);
        bundle.putSerializable(TAG_TYPE, type);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_share_add_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        requestData();
    }

    @Override
    public void onItemClick(View view, int position) {
        final ItemProperties itemProperties = (ItemProperties) mShareAddAdapter.getItem(position);
        itemProperties.setSelected(!itemProperties.isSelected());
        mShareAddAdapter.notifyItemChanged(position);
        setCountChecked();
    }

    @Override
    public void onError(@Nullable String message) {
        mSwipeRefresh.setRefreshing(false);

        if (mShareAddAdapter.getItemCount() == 0) {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION);
        }

        if (isActivePage()) {
            showSnackBar(message);
        }
    }

    @Override
    public void onUnauthorized(@Nullable String message) {
        getActivity().finish();
        MainActivity.show(getContext());
    }

    @Override
    public void onGetUsers(List<Entity> list) {
        setPlaceholder(true, list != null && !list.isEmpty());
        mSwipeRefresh.setRefreshing(false);
        mShareAddAdapter.setMode(ShareAddAdapter.Mode.USERS);
        mShareAddAdapter.setItems(list);
    }

    @Override
    public void onGetGroups(List<Entity> list) {
        setPlaceholder(false, list != null && !list.isEmpty());
        mSwipeRefresh.setRefreshing(false);
        mShareAddAdapter.setMode(ShareAddAdapter.Mode.GROUPS);
        mShareAddAdapter.setItems(list);
    }

    @Override
    public void onGetCommon(List<Entity> list) {
        // Stub
    }

    @Override
    public void onSuccessAdd() {
        ModelShareStack.getInstance().setRefresh(true);
        showParentRootFragment();
    }

    @Override
    public void onSearchValue(@Nullable String value) {
        // Stub
    }

    private void init(final Bundle savedInstanceState) {
        getArgs(savedInstanceState);
        initViews();
        restoreViews(savedInstanceState);
    }

    private void getArgs(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        mAddPresenter.setItem((Item) bundle.getSerializable(TAG_ITEM));
        mAddPresenter.setType((Type) bundle.getSerializable(TAG_TYPE));
    }

    private void initViews() {
        mShareAddAdapter = new ShareAddAdapter();
        mShareAddAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mShareAddAdapter);
    }

    private void restoreViews(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAddPresenter.updateTypeSharedListState();
        } else {
            requestData();
        }
    }

    private void requestData() {
        mSwipeRefresh.setRefreshing(true);
        mAddPresenter.getShared();
    }

    private void setPlaceholder(final boolean isUsers, final boolean isEmpty) {
        if (isUsers) {
            mPlaceholderViews.setTemplatePlaceholder(isEmpty ? PlaceholderViews.Type.NONE : PlaceholderViews.Type.USERS);
        } else {
            mPlaceholderViews.setTemplatePlaceholder(isEmpty ? PlaceholderViews.Type.NONE : PlaceholderViews.Type.GROUPS);
        }
    }

    public void updateAdapterState() {
        mShareAddAdapter.notifyDataSetChanged();
    }

    public void addAccess() {
        mAddPresenter.shareItem();
    }

    public void setMessage(final String message) {
        mAddPresenter.setMessage(message);
    }

    private void setCountChecked() {
        ((AddPagerFragment) getParentFragment()).setChecked();
    }

    private boolean isActivePage() {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof AddPagerFragment) {
            return ((AddPagerFragment) fragment).isActivePage(this);
        }

        return true;
    }

}
