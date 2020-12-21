package app.editors.manager.ui.fragments.login;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import java.util.concurrent.TimeUnit;

import app.editors.manager.R;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashFragment extends BaseAppFragment {

    public static final String TAG = SplashFragment.class.getSimpleName();

    private static final int DELAY_SPLASH = 1000;

    @BindView(R.id.appCompatImageView)
    protected AppCompatImageView mAppCompatImageView;

    private Unbinder mUnbinder;
    private Disposable mDisposable;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_splash, container, false);
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
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    private void init() {
        try {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.image_onlyoffice_text);
            mAppCompatImageView.setImageDrawable(drawable);
        } catch (InflateException | Resources.NotFoundException e) {
            FirebaseUtils.addCrash("Inflate error when start");
            e.printStackTrace();
        }

        mDisposable = Observable.just(1).delay(DELAY_SPLASH, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    MainActivity.show(getContext());
                    requireActivity().finish();
                });
    }

}
