package app.editors.manager.ui.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;

import app.editors.manager.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.ui.dialogs.base.BaseDialog;

public class MoveCopyDialog extends BaseDialog {

    public static final String TAG = MoveCopyDialog.class.getSimpleName();

    public interface DialogButtonOnClick {
        void continueClick(String tag, String action);
    }

    Unbinder mUnbinder;

    @BindView(R.id.title_text_view)
    AppCompatTextView mTitleTextView;
    @BindView(R.id.overwrite_radio_button)
    RadioButton mUpdateRadioButton;
    @BindView(R.id.copy_radio_button)
    RadioButton mReplaceRadioButton;
    @BindView(R.id.skip_radio_button)
    RadioButton mSkipRadioButton;
    @BindView(R.id.options_radio_group)
    RadioGroup mOptionsRadioGroup;
    @BindView(R.id.continue_button)
    AppCompatButton mContinueButton;
    @BindView(R.id.cancel_button)
    AppCompatButton mCancelButton;
    @BindView(R.id.descriptionTitle)
    TextView mDescriptionTitle;

    public final static String TAG_OVERWRITE = "TAG_OVERWRITE";
    public final static String TAG_DUPLICATE = "TAG_DUPLICATE";
    public final static String TAG_SKIP = "TAG_SKIP";

    public final static String ACTION_MOVE = "ACTION_MOVE";
    public final static String ACTION_COPY = "ACTION_COPY";

    private final static String TAG_NAME_FILES = "TAG_NAME_FILES";
    private final static String TAG_ACTION = "TAG_ACTION";
    private final static String TAG_FOLDER_NAME = "TAG_FOLDER_NAME";

    private String mTag = TAG_OVERWRITE;
    private String mAction;
    private String mFolderTitle;

    private DialogButtonOnClick mDialogButtonOnClick;

    private RadioGroup.OnCheckedChangeListener mChangeListener = (group, checkedId) -> {
        mContinueButton.setEnabled(true);
        switch (checkedId) {
            case R.id.overwrite_radio_button:
                mTag = TAG_OVERWRITE;
                break;
            case R.id.copy_radio_button:
                mTag = TAG_DUPLICATE;
                break;
            case R.id.skip_radio_button:
                mTag = TAG_SKIP;
                break;
        }
    };

    public static MoveCopyDialog newInstance(ArrayList<String> filesName, String action, String titleFolder) {
        Bundle args = new Bundle();
        args.putStringArrayList(TAG_NAME_FILES, filesName);
        args.putString(TAG_ACTION, action);
        args.putString(TAG_FOLDER_NAME, titleFolder);
        MoveCopyDialog fragment = new MoveCopyDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_fragment_move_copy, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        mOptionsRadioGroup.setOnCheckedChangeListener(mChangeListener);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAction = bundle.getString(TAG_ACTION);
            mFolderTitle = bundle.getString(TAG_FOLDER_NAME);
            ArrayList<String> names = bundle.getStringArrayList(TAG_NAME_FILES);
            if (names != null) {
                setTitle(names);
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void setTitle(ArrayList<String> names) {
        if (names.size() == 1) {
            String text = String.format(getString(R.string.dialog_move_copy_title_one_file), names.get(0), mFolderTitle);
            mDescriptionTitle.append(text);
        } else {
            String text = String.format(getString(R.string.dialog_move_copy_title_files), names.size(), mFolderTitle);
            mDescriptionTitle.append(text);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick({R.id.continue_button, R.id.cancel_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.continue_button:
                mDialogButtonOnClick.continueClick(mTag, mAction);
                break;
            case R.id.cancel_button:
                onBackPressed();
                break;
        }
    }

    public void setOnClick(DialogButtonOnClick mDialogButtonOnClick) {
        this.mDialogButtonOnClick = mDialogButtonOnClick;
    }
}
