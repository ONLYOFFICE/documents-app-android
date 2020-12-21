package app.editors.manager.mvp.presenters.login;

import androidx.annotation.NonNull;

import moxy.InjectViewState;;

import java.util.Set;
import java.util.TreeSet;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.base.Capabilities;
import app.editors.manager.mvp.models.response.ResponseCapabilities;
import app.editors.manager.mvp.models.response.ResponseSettings;
import app.editors.manager.mvp.views.login.EnterprisePortalView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

@InjectViewState
public class EnterprisePortalPresenter extends BaseLoginPresenter<EnterprisePortalView, ResponseCapabilities> {

    public static final String TAG = EnterprisePortalPresenter.class.getSimpleName();

    private static Set<String> BANNED_ADDRESSES = new TreeSet<String>() {{
        add(".r7-");
    }};

    private static final String TAG_SSH = "/#ssloff";

    private Disposable mDisposable;

    public EnterprisePortalPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        if (mDisposable != null){
            mDisposable.dispose();
        }
    }

    public void checkPortal(String portal) {
        // Reset preferences
        mPreferenceTool.setDefaultPortal();

        // Check portal for disable ssh
        if (portal.endsWith(TAG_SSH)) {
            mPreferenceTool.setSslState(false);
            portal = portal.replace(TAG_SSH, "");
        }

        // Check portal syntax
        portal = getPortal(portal);
        if (portal == null) {
            getViewState().onPortalSyntax(mContext.getString(R.string.login_enterprise_edit_error_hint));
            return;
        }

        // Check banned addresses
        if (checkBannedAddress(portal)) {
            getViewState().onError(mContext.getString(R.string.errors_client_host_not_found));
            return;
        }
        getViewState().onShowDialog();
        portalCapabilities(portal);
    }

    private boolean checkBannedAddress(@NonNull String portal) {
        for (String item : BANNED_ADDRESSES) {
            if (portal.contains(item)) {
                return true;
            }
        }
        return false;
    }

    protected void portalCapabilities(final String portal) {
        try {
            initRetrofitPref(portal);
            mRequestCall = mRetrofitTool.getApiWithPreferences().capabilities();
            mRequestCall.enqueue(new BaseCallback() {

                @Override
                public void onSuccessResponse(Response<ResponseCapabilities> response) {
                    FirebaseUtils.addAnalyticsCheckPortal(portal, FirebaseUtils.AnalyticsKeys.SUCCESS, null);
                    final Capabilities capabilities = response.body().getResponse();
                    mPreferenceTool.setPortal(portal);
                    mPreferenceTool.setSsoUrl(capabilities.getSsoUrl());
                    mPreferenceTool.setSsoLabel(capabilities.getSsoLabel());
                    mPreferenceTool.setLdap(capabilities.getLdapEnabled());

//                    if (capabilities.getProviders() != null && !capabilities.getProviders().isEmpty()) {
//                        mPreferenceTool.setAuthProviders(capabilities.getProviders());
//                    }

                    checkPortalVersion();

                    // Show user security message for http connection
                    if (mPreferenceTool.isHttpsConnect()) {
                        getViewState().onSuccessPortal(portal);
                    } else {
                        getViewState().onHttpPortal(portal);
                    }
                }

                @Override
                public void onErrorResponse(Response<ResponseCapabilities> response) {
                    super.onErrorResponse(response);
                    FirebaseUtils.addAnalyticsCheckPortal(portal, FirebaseUtils.AnalyticsKeys.FAILED, "Code: " + response.code());
                }

                @Override
                public void onFailResponse(Throwable t) {
                    if (isConfigConnection(t)) {
                        portalCapabilities(portal);
                    } else {
                        FirebaseUtils.addAnalyticsCheckPortal(portal, FirebaseUtils.AnalyticsKeys.FAILED, "Error: " + t.getMessage());
                        super.onFailResponse(t);
                    }
                }
            });
        } catch (UrlSyntaxMistake e) {
            // No need handle
        }
    }

    private void checkPortalVersion() {
        mDisposable = mRetrofitTool.getApiWithPreferences().getSettings()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(ResponseSettings::getResponse)
                .subscribe(settings -> mPreferenceTool.setServerVersion(settings.getCommunityServer())
                        , throwable -> mPreferenceTool.setServerVersion(""));
    }

}
