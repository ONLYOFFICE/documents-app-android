package app.editors.manager.ui.fragments.storage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.mvp.presenters.storage.SelectPresenter;
import app.editors.manager.mvp.views.storage.SelectView;
import app.editors.manager.ui.adapters.StorageAdapter;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.ui.adapters.BaseAdapter;
import moxy.presenter.InjectPresenter;

public class SelectFragment extends BaseAppFragment implements BaseAdapter.OnItemClickListener, SelectView {

    public static final String TAG = SelectFragment.class.getSimpleName();

    @BindView(R.id.list_of_items)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout)
    protected SwipeRefreshLayout mRefreshLayout;

    @InjectPresenter
    SelectPresenter mPresenter;

    @Inject
    PreferenceTool mPreferenceTool;

    private Unbinder mUnbinder;
    private StorageAdapter mStorageAdapter;

    public static SelectFragment newInstance() {
        return new SelectFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_storage_select, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        mPresenter.getStorages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onItemClick(View view, int position) {
        String providerKey = mStorageAdapter.getItem(position);
        mPresenter.connect(providerKey);
    }

    private void init() {
        setActionBarTitle(getString(R.string.storage_select_title));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        initViews();
    }

    private void initViews() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mStorageAdapter = new StorageAdapter(requireContext());
        mStorageAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mStorageAdapter);
    }

    @Override
    public void onUpdate(List<String> storages) {
        mStorageAdapter.setItems(storages);
    }

    @Override
    public void showWebTokenFragment(Storage storage) {
        showFragment(WebTokenFragment.newInstance(storage), WebTokenFragment.TAG, false);
    }

    @Override
    public void showWebDavFragment(String providerKey, String url, String title) {
        showFragment(WebDavFragment.newInstance(providerKey, url, title), WebDavFragment.TAG, false);
    }

    @Override
    public void showProgress(boolean isVisible) {
        mRefreshLayout.setRefreshing(isVisible);
    }

    @Override
    public void onError(@Nullable String message) {
        if (message != null) {
            showSnackBar(message);
        }
    }
}
