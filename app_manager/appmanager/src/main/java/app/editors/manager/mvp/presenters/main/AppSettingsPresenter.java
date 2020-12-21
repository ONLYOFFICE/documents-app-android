package app.editors.manager.mvp.presenters.main;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.views.main.AppSettingsView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import lib.toolkit.base.managers.utils.FileUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class AppSettingsPresenter extends MvpPresenter<AppSettingsView> {

    private static final String EMPTY = "0";

    @Inject
    protected Context mContext;
    @Inject
    protected PreferenceTool mPreferenceTool;

    private Disposable mCacheDisposable;

    public AppSettingsPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCacheDisposable != null) {
            mCacheDisposable.dispose();
        }
    }

    public void getCacheSize() {
        File cacheDir = mContext.getExternalCacheDir();
        File internalCache = mContext.getCacheDir();
        if (cacheDir != null) {
            mCacheDisposable = Single.just(FileUtils.getSize(cacheDir.getAbsoluteFile()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(aLong -> aLong + FileUtils.getSize(internalCache.getAbsoluteFile()))
                    .map(size -> StringUtils.getFormattedSize(mContext, size))
                    .subscribe(s -> getViewState().onSetCacheSize(s));
        } else {
            getViewState().onSetCacheSize(EMPTY);
        }
    }

    public void initWifiState() {
        getViewState().onSetWifiState(mPreferenceTool.getUploadWifiState());
    }

    public void setWifiState(boolean isChecked) {
        mPreferenceTool.setWifiState(isChecked);
    }

    @SuppressLint("MissingPermission")
    public void clearCache() {
        if (mContext != null) {
            FileUtils.clearCacheDir(mContext);
            FileUtils.clearCacheDir(mContext, false);
        }
        getCacheSize();
        getViewState().onMessage(mContext.getString(R.string.setting_cache_cleared));
    }

    public void setAnalyticState(boolean isEnable) {
        App.getApp().setAnalyticEnable(isEnable);
        mPreferenceTool.setAnalyticEnable(isEnable);
    }

    public void initAnalyticState() {
        getViewState().onAnalyticState(mPreferenceTool.isAnalyticEnable());
    }
}
