package app.editors.manager.ui.fragments.main;

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
import androidx.appcompat.widget.AppCompatButton;

import app.editors.manager.R;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.main.CloudsActivity;
import app.editors.manager.ui.activities.main.IMainActivity;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.activities.main.SettingsActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class OnlyOfficeCloudFragment extends BaseAppFragment {

    public static final String TAG = OnlyOfficeCloudFragment.class.getSimpleName();

    private static final String KEY_PROFILE = "KEY_PROFILE";

    public static OnlyOfficeCloudFragment newInstance(boolean isProfile) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_PROFILE, isProfile);
        OnlyOfficeCloudFragment fragment = new OnlyOfficeCloudFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.startButton)
    AppCompatButton mStartButton;
    @BindView(R.id.otherStorageButton)
    AppCompatButton mOtherButton;

    private IMainActivity mMainActivity;
    private boolean mIsAccounts = false;
    private Unbinder mUnbinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IMainActivity) {
            mMainActivity = (IMainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cloud_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkArguments();
        init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cloud_settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settingsItem) {
            SettingsActivity.show(requireContext());
        }
        return true;
    }

    private void checkArguments() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_PROFILE)) {
            mIsAccounts = args.getBoolean(KEY_PROFILE);
            setHasOptionsMenu(mIsAccounts);
        }
    }

    private void init() {
        if (mIsAccounts) {
            setActionBarTitle(getString(R.string.cloud_accounts_title));
        } else {
            setActionBarTitle(getString(R.string.fragment_clouds_title));

        }
        mMainActivity.showActionButton(false);
    }

    @OnClick({R.id.startButton, R.id.otherStorageButton})
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.startButton: {
                PortalsActivity.show(requireActivity());
                break;
            }
            case R.id.otherStorageButton: {
                CloudsActivity.Companion.show(requireContext());
                break;
            }
        }
    }


}
