package app.editors.manager.ui.fragments.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import app.editors.manager.R;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class OnBoardingPageFragment extends BaseAppFragment {

    public static final String TAG = OnBoardingPageFragment.class.getSimpleName();
    public static final String TAG_HEADER = "TAG_HEADER";
    public static final String TAG_INFO = "TAG_INFO";
    public static final String TAG_IMAGE = "TAG_IMAGE";

    private Unbinder mUnbinder;
    @BindView(R.id.on_boarding_page_header)
    protected AppCompatTextView mOnBoardingPageHeader;
    @BindView(R.id.on_boarding_page_info)
    protected AppCompatTextView mOnBoardingPageInfo;
    @BindView(R.id.on_boarding_page_image)
    protected AppCompatImageView mOnBoardingPageImage;

    @StringRes
    private int mHeaderResId;
    @StringRes
    private int mInfoResId;
    @DrawableRes
    private int mImageResId;

    public static OnBoardingPageFragment newInstance(@StringRes int headerId, @StringRes int infoId, @DrawableRes int imageId) {
        final OnBoardingPageFragment fragment = new OnBoardingPageFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(TAG_HEADER, headerId);
        bundle.putInt(TAG_INFO, infoId);
        bundle.putInt(TAG_IMAGE, imageId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void dispatchTouchEvent(@NonNull MotionEvent ev) {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_on_boarding_page, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void init() {
        getArgs();
        mOnBoardingPageHeader.setText(mHeaderResId);
        mOnBoardingPageInfo.setText(mInfoResId);
        mOnBoardingPageImage.setImageResource(mImageResId);
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mHeaderResId = bundle.getInt(TAG_HEADER);
            mInfoResId = bundle.getInt(TAG_INFO);
            mImageResId = bundle.getInt(TAG_IMAGE);
        }
    }

}
