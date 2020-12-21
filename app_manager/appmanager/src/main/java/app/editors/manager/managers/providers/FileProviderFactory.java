package app.editors.manager.managers.providers;

import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;

public class FileProviderFactory {

    public static BaseFileProvider getProvider(AccountsSqlData accountsSqlData) {
        PreferenceTool preferenceTool = App.getApp().getAppComponent().getPreference();

        if (accountsSqlData != null) {
            if (accountsSqlData.isWebDav()) {
                return new WebDavFileProvider(WebDavApi.getApi(accountsSqlData.getScheme() + accountsSqlData.getPortal()),
                        WebDavApi.Providers.valueOf(accountsSqlData.getWebDavProvider()));
            } else {
                return new CloudFileProvider(accountsSqlData.getToken(), App.getApp().getAppComponent().getRetrofit().getApiWithPreferences());
            }
        } else if (preferenceTool.getPortal() != null && preferenceTool.getToken() != null) {
            return new CloudFileProvider(preferenceTool.getToken(), App.getApp().getAppComponent().getRetrofit().getApiWithPreferences());
        } else {
            return null;
        }
    }
}
