package app.editors.manager.di.module;

import android.content.Context;

import javax.inject.Singleton;

import app.documents.core.settings.NetworkSettings;
import app.editors.manager.managers.tools.AccountManagerTool;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.CacheTool;
import app.editors.manager.managers.tools.CountriesCodesTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.states.OperationsState;
import dagger.Module;
import dagger.Provides;
import lib.toolkit.base.managers.tools.GlideTool;
import lib.toolkit.base.managers.tools.LocalContentTools;

@Module
public class ToolModule {

    @Provides
    @Singleton
    public PreferenceTool providePref(Context context) {
        return new PreferenceTool(context);
    }

    @Provides
    @Singleton
    public CountriesCodesTool provideCountryCodes(Context context) {
        return new CountriesCodesTool(context);
    }

    @Provides
    @Singleton
    public GlideTool provideGlide(Context context) {
        return new GlideTool(context);
    }

    @Provides
    @Singleton
    public AccountManagerTool provideAccountsManager(Context context) {
        return new AccountManagerTool(context);
    }

    @Provides
    @Singleton
    public AccountSqlTool provideAccountsSql(Context context) {
        return new AccountSqlTool(context);
    }

    @Provides
    @Singleton
    public CacheTool provideCacheTool(Context context) {
        return new CacheTool(context);
    }

    @Provides
    @Singleton
    public OperationsState provideSectionsState() {
        return new OperationsState();
    }

    @Provides
    @Singleton
    public LocalContentTools provideContentTool(Context context) { return  new LocalContentTools(context); }

}
