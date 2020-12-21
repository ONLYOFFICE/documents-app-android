package app.editors.manager.ui.views.animation;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

public class HeightValueAnimator {

    private static final int DEFAULT_DURATION = 300;
    private static final int MIN_HEIGHT = 0;

    public interface OnAnimationListener {
        void onStart(boolean isShow);
        void onEnd(boolean isShow);
    }

    private final int mDuration;
    private final int mViewHeight;
    private final View mViewAnimation;
    private ValueAnimator mLayoutAnimation;
    private OnAnimationListener mOnAnimationListener;

    public HeightValueAnimator(final View view, final int duration) {
        mViewAnimation = view;
        mViewAnimation.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mViewHeight = view.getMeasuredHeight();
        mDuration = duration;
    }

    public HeightValueAnimator(final View view) {
        this(view, DEFAULT_DURATION);
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        mOnAnimationListener = onAnimationListener;
    }

    public void animate(final boolean isShow) {
        final int newVisibility = isShow? View.VISIBLE : View.GONE;
        final int viewVisibility = mViewAnimation.getVisibility();
        if (newVisibility != viewVisibility) {
            if (mLayoutAnimation != null && mLayoutAnimation.isRunning()) {
                mLayoutAnimation.cancel();
            }

            if (isShow) {
                mLayoutAnimation = ValueAnimator.ofInt(MIN_HEIGHT, mViewHeight);
            } else {
                mLayoutAnimation = ValueAnimator.ofInt(mViewHeight, MIN_HEIGHT);
            }

            mLayoutAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mViewAnimation.setVisibility(View.INVISIBLE);
                    if (mOnAnimationListener != null) {
                        mOnAnimationListener.onStart(isShow);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isShow) {
                        mViewAnimation.setVisibility(View.VISIBLE);
                    } else {
                        mViewAnimation.setVisibility(View.GONE);
                    }

                    if (mOnAnimationListener != null) {
                        mOnAnimationListener.onEnd(isShow);
                    }
                }
            });

            mLayoutAnimation.setDuration(mDuration);
            mLayoutAnimation.addUpdateListener(animation -> {
                final int value = (int) animation.getAnimatedValue();
                mViewAnimation.getLayoutParams().height = value;
                mViewAnimation.requestLayout();
            });

            mLayoutAnimation.start();
        }
    }

    public void animate() {
        animate(mViewAnimation.getVisibility() == View.GONE);
    }

    public void clear() {
        mOnAnimationListener = null;
        if (mLayoutAnimation != null) {
            mLayoutAnimation.removeAllUpdateListeners();
            mLayoutAnimation.cancel();
        }
    }

}