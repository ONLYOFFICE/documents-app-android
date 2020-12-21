package app.editors.manager.ui.views.custom;


import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import app.editors.manager.R;
import app.editors.manager.managers.exceptions.ButterknifeInitException;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.UiUtils;

public class PlaceholderViews {

    public enum Type {
        NONE, CONNECTION, EMPTY, SEARCH, SHARE, ACCESS, SUBFOLDER,
        USERS, GROUPS, COMMON, MEDIA, LOAD
    }

    public interface OnClickListener {
        void onRetryClick();
    }

    @BindView(R.id.placeholder_layout)
    protected ConstraintLayout mPlaceholderLayout;
    @BindView(R.id.placeholder_text)
    protected AppCompatTextView mPlaceholderTitle;
    @BindView(R.id.placeholder_image)
    @Nullable
    protected AppCompatImageView mPlaceholderImage;
    @BindView(R.id.placeholder_retry)
    @Nullable
    protected AppCompatTextView mPlaceholderRetry;

    private View mView;
    private View mViewForHide;
    private Unbinder mUnbinder;
    private OnClickListener mOnClickListener;

    public PlaceholderViews(final View view) {
        try {
            mUnbinder = ButterKnife.bind(this, view);
        } catch (RuntimeException e) {
            throw new ButterknifeInitException(PlaceholderViews.class.getSimpleName() + " - must initWithPreferences with specific view!", e);
        }

        mView = view;
        mPlaceholderLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.placeholder_retry)
    @Optional
    protected void onClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onRetryClick();
        }
    }

    public void setVisibility(final boolean isVisible) {
        mPlaceholderLayout.setVisibility(isVisible? View.VISIBLE : View.GONE);
        if (mViewForHide != null) {
            mViewForHide.setVisibility(isVisible? View.GONE : View.VISIBLE);
        }
    }

    public void setViewForHide(final View viewForHide) {
        mViewForHide = viewForHide;
    }

    public void setTitle(@StringRes final int resId) {
        mPlaceholderTitle.setText(resId);
    }

    public void setTitleColor(@ColorRes final int resId) {
        mPlaceholderTitle.setTextColor(ContextCompat.getColor(mView.getContext(), resId));
    }

    public void setImage(@DrawableRes final int resId) {
        if (mPlaceholderImage != null) {
            mPlaceholderImage.setImageResource(resId);
        }
    }

    public void setImageTint(@ColorRes final int resId) {
        if (mPlaceholderImage != null) {
            UiUtils.setImageTint(mPlaceholderImage, resId);
        }
    }

    public void setRetryTint(@ColorRes final int resId) {
        if (mPlaceholderRetry != null) {
            mPlaceholderRetry.setTextColor(ContextCompat.getColor(mPlaceholderRetry.getContext(), resId));
        }
    }

    public void setTemplatePlaceholder(Type type) {
        switch (type) {
            case NONE:
                setVisibility(false);
                return;
            case CONNECTION:
                setTitle(R.string.placeholder_connection);
                break;
            case EMPTY:
                setTitle(R.string.placeholder_empty);
                break;
            case SEARCH:
                setTitle(R.string.placeholder_search);
                break;
            case SHARE:
                setTitle(R.string.placeholder_share);
                break;
            case ACCESS:
                setTitle(R.string.placeholder_access_denied);
                break;
            case SUBFOLDER:
                setTitle(R.string.placeholder_no_subfolders);
                break;
            case USERS:
                setTitle(R.string.placeholder_no_users);
                break;
            case GROUPS:
                setTitle(R.string.placeholder_no_groups);
                break;
            case COMMON:
                setTitle(R.string.placeholder_no_users_groups);
                break;
            case LOAD:
                setTitle(R.string.placeholder_loading_files);
                break;
            case MEDIA:
                setImage(R.drawable.ic_media_error);
                setImageTint(R.color.colorLightWhite);
                setTitle(R.string.placeholder_media_error);
                setTitleColor(R.color.colorLightWhite);
                setRetryTint(R.color.colorAccent);
                break;
        }

        setVisibility(true);
    }

    public void unbind() {
        mUnbinder.unbind();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

}
