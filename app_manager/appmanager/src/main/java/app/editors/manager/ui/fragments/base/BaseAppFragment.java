package app.editors.manager.ui.fragments.base;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.ui.activities.base.BaseAppActivity;
import app.editors.manager.ui.activities.main.MediaActivity;
import app.editors.manager.ui.activities.main.OperationActivity;
import app.editors.manager.ui.activities.main.ShareActivity;
import app.editors.manager.ui.activities.main.StorageActivity;
import app.editors.manager.ui.activities.main.WebViewerActivity;
import app.editors.manager.ui.interfaces.ContextDialogInterface;
import lib.toolkit.base.managers.utils.FragmentUtils;
import lib.toolkit.base.ui.fragments.base.BaseFragment;

public abstract class BaseAppFragment extends BaseFragment {

    private final String TAG = getClass().getSimpleName();

    protected static final int PERMISSION_SMS = 0;
    protected static final int PERMISSION_WRITE_STORAGE = 1;
    protected static final int PERMISSION_READ_STORAGE = 2;
    protected static final int PERMISSION_CAMERA = 3;
    protected static final int PERMISSION_READ_UPLOAD = 4;

    @Nullable
    protected Menu mMenu;

    @Nullable
    protected MenuInflater mMenuInflater;

    public ContextDialogInterface mContextDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mContextDialogListener = (ContextDialogInterface) context;
            addOnDispatchTouchEvent();
        } catch (ClassCastException e) {
            throw new RuntimeException(BaseAppFragment.class.getSimpleName() + " - must implement - " +
                    BaseAppActivity.class.getSimpleName());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        mMenuInflater = inflater;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }


    /*
     * Fragment operations
     * */
    protected void showFragment(@NonNull final Fragment fragment, @Nullable final String tag, boolean isAdd) {
        if (getFragmentManager() != null) {
            FragmentUtils.showFragment(getFragmentManager(), fragment, R.id.frame_container, tag, isAdd);
        }
    }

    protected void showParentFragment(@NonNull final Fragment fragment, @Nullable final String tag, boolean isAdd) {
        if (getParentFragment() != null && getParentFragment().getFragmentManager() != null) {
            FragmentUtils.showFragment(getParentFragment().getFragmentManager(), fragment, R.id.frame_container, tag, isAdd);
        }
    }

    /*
     * Keyboard
     * */
    protected void copySharedLinkToClipboard(@Nullable final String value, @Nullable final String message) {
        copyToClipboard(getString(R.string.share_clipboard_external_link_label), value, message);
    }

    /*
     * Show activity
     * */
    protected void showOperationMoveActivity(@NonNull Explorer explorer) {
        OperationActivity.showMove(this, explorer);
    }

    protected void showOperationCopyActivity(@NonNull Explorer explorer) {
        OperationActivity.showCopy(this, explorer);
    }

    protected void showViewerActivity(final File file) {
        WebViewerActivity.show(requireActivity(), file);
    }

    protected void showMediaActivity(final Explorer explorer, final boolean isWebDAv) {
        MediaActivity.show(this, explorer, isWebDAv);
    }

    protected void showShareActivity(final Item item) {
        ShareActivity.show(this, item);
    }

    protected void showStorageActivity(final boolean isMySection) {
        StorageActivity.show(this, isMySection);
    }

}
