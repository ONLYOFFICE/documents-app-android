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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.utils.Constants;
import app.editors.manager.managers.utils.StorageUtils;
import app.editors.manager.ui.adapters.StorageAdapter;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lib.toolkit.base.ui.adapters.BaseAdapter;

public class SelectFragment extends BaseAppFragment implements BaseAdapter.OnItemClickListener {

    public static final String TAG = SelectFragment.class.getSimpleName();

    @BindView(R.id.list_of_items)
    protected RecyclerView mRecyclerView;

    @Inject
    PreferenceTool mPreferenceTool;

    private Unbinder mUnbinder;
    private StorageAdapter mStorageAdapter;

    public static SelectFragment newInstance() {
        final SelectFragment selectFragment = new SelectFragment();
        return selectFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_storage_select, container, false);
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
        mUnbinder.unbind();
    }

    @Override
    public void onItemClick(View view, int position) {
        String providerKey = mStorageAdapter.getItem(position);
        final StorageUtils.Storage storage = StorageUtils.getStorageInstance(providerKey, mPreferenceTool.isPortalInfo());
        if (storage != null) {
            showFragment(WebTokenFragment.newInstance(storage), WebTokenFragment.TAG, false);
        } else {
            String url = "";
            String title = "WebDav service";
            switch (providerKey) {
                case Api.Storage.YANDEX:
                    url = Constants.WevDav.URL_YANDEX;
                    title = getString(R.string.storage_select_yandex);
                    break;
                case Api.Storage.SHAREPOINT:
                    title = getString(R.string.storage_select_share_point);
                    break;
                case Api.Storage.OWNCLOUD:
                    title = getString(R.string.storage_select_own_cloud);
                    providerKey = Api.Storage.WEBDAV;
                    break;
                case Api.Storage.NEXTCLOUD:
                    title = getString(R.string.storage_select_next_cloud);
                    providerKey = Api.Storage.WEBDAV;
                    break;
            }

            showFragment(WebDavFragment.newInstance(providerKey, url, title), WebDavFragment.TAG, false);
        }
    }

    private void init(final Bundle savedInstanceState) {
        setActionBarTitle(getString(R.string.storage_select_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        initViews();
    }

    private void initViews() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mStorageAdapter = new StorageAdapter(getContext());
        mStorageAdapter.setItems(getStorageList());
        mStorageAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mStorageAdapter);
    }

    private List<String> getStorageList() {
        final Set<String> stringSet = new LinkedHashSet<>();
        stringSet.add(Api.Storage.GOOGLEDRIVE);
        stringSet.add(Api.Storage.DROPBOX);
        stringSet.add(Api.Storage.ONEDRIVE);
        stringSet.add(Api.Storage.YANDEX);
        stringSet.add(Api.Storage.BOXNET);
        stringSet.add(Api.Storage.SHAREPOINT);
        stringSet.add(Api.Storage.OWNCLOUD);
        stringSet.add(Api.Storage.NEXTCLOUD);
        stringSet.add(Api.Storage.WEBDAV);
        return new ArrayList<>(stringSet);
    }

}
