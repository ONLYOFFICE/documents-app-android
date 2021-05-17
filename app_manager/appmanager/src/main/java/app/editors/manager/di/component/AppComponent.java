package app.editors.manager.di.component;

import android.content.Context;

import javax.inject.Singleton;

import app.documents.core.account.AccountDao;
import app.documents.core.di.module.AccountModule;
import app.documents.core.di.module.RecentModule;
import app.documents.core.di.module.SettingsModule;
import app.documents.core.settings.NetworkSettings;
import app.documents.core.settings.WebDavInterceptor;
import app.editors.manager.app.MigrateDb;
import app.editors.manager.di.module.AppModule;
import app.editors.manager.di.module.ToolModule;
import app.editors.manager.managers.providers.AccountProvider;
import app.editors.manager.managers.tools.AccountManagerTool;
import app.editors.manager.managers.tools.CacheTool;
import app.editors.manager.managers.tools.CountriesCodesTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.mvp.presenters.login.AccountsPresenter;
import app.editors.manager.mvp.presenters.login.EnterpriseAppAuthPresenter;
import app.editors.manager.mvp.presenters.login.EnterpriseCreateLoginPresenter;
import app.editors.manager.mvp.presenters.login.EnterpriseCreateValidatePresenter;
import app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter;
import app.editors.manager.mvp.presenters.login.EnterprisePhonePresenter;
import app.editors.manager.mvp.presenters.login.EnterprisePortalPresenter;
import app.editors.manager.mvp.presenters.login.EnterpriseSSOPresenter;
import app.editors.manager.mvp.presenters.login.EnterpriseSmsPresenter;
import app.editors.manager.mvp.presenters.login.PersonalLoginPresenter;
import app.editors.manager.mvp.presenters.login.PersonalSignUpPresenter;
import app.editors.manager.mvp.presenters.login.WebDavSignInPresenter;
import app.editors.manager.mvp.presenters.main.AppSettingsPresenter;
import app.editors.manager.mvp.presenters.main.CloudAccountPresenter;
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter;
import app.editors.manager.mvp.presenters.main.DocsOnDevicePresenter;
import app.editors.manager.mvp.presenters.main.DocsRecentPresenter;
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter;
import app.editors.manager.mvp.presenters.main.MainActivityPresenter;
import app.editors.manager.mvp.presenters.main.MainPagerPresenter;
import app.editors.manager.mvp.presenters.main.ProfilePresenter;
import app.editors.manager.mvp.presenters.share.AddPresenter;
import app.editors.manager.mvp.presenters.share.SettingsPresenter;
import app.editors.manager.mvp.presenters.storage.ConnectPresenter;
import app.editors.manager.mvp.presenters.storage.SelectPresenter;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.activities.main.OperationActivity;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import app.editors.manager.ui.adapters.MediaAdapter;
import app.editors.manager.ui.adapters.ShareAddAdapter;
import app.editors.manager.ui.dialogs.AccountBottomDialog;
import app.editors.manager.ui.fragments.login.AuthPagerFragment;
import app.editors.manager.ui.fragments.login.CountriesCodesFragment;
import app.editors.manager.ui.fragments.login.EnterprisePhoneFragment;
import app.editors.manager.ui.fragments.login.EnterprisePortalFragment;
import app.editors.manager.ui.fragments.login.EnterpriseSignInFragment;
import app.editors.manager.ui.fragments.login.EnterpriseSmsFragment;
import app.editors.manager.ui.fragments.login.PersonalPortalFragment;
import app.editors.manager.ui.fragments.main.DocsBaseFragment;
import app.editors.manager.ui.fragments.main.WebViewerFragment;
import app.editors.manager.ui.fragments.media.MediaImageFragment;
import app.editors.manager.ui.fragments.media.MediaVideoFragment;
import app.editors.manager.ui.fragments.onboarding.OnBoardingPagerFragment;
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment;
import app.editors.manager.ui.fragments.storage.ConnectFragment;
import app.editors.manager.ui.fragments.storage.SelectFragment;
import app.editors.manager.ui.fragments.storage.WebDavFragment;
import app.editors.manager.ui.fragments.storage.WebTokenFragment;
import dagger.Component;
import lib.toolkit.base.managers.tools.GlideTool;
import lib.toolkit.base.managers.tools.LocalContentTools;

@Component(modules = {AppModule.class, ToolModule.class, SettingsModule.class, AccountModule.class, RecentModule.class})
@Singleton
public interface AppComponent {

    /*
    * TODO scopes!
    * */
    Context getContext();
    PreferenceTool getPreference();
    CountriesCodesTool getCountriesCodes();
    AccountManagerTool getAccountsManager();
    CacheTool getCacheTool();
    OperationsState getSectionsState();
    LocalContentTools getContentTools();
    GlideTool getGlideTools();
    NetworkSettings getNetworkSettings();
    AccountDao getAccountsDao();

    /*
    * Login
    * */
    void inject(EnterprisePortalPresenter enterprisePortalPresenter);
    void inject(EnterpriseLoginPresenter enterpriseSignInPresenter);
    void inject(EnterpriseSmsPresenter enterpriseSmsPresenter);
    void inject(EnterprisePhonePresenter enterprisePhonePresenter);
    void inject(EnterpriseCreateValidatePresenter enterpriseCreateValidatePresenter);
    void inject(EnterpriseCreateLoginPresenter enterpriseCreateSignInPresenter);
    void inject(PersonalLoginPresenter personalSignInPresenter);
    void inject(PersonalSignUpPresenter personalSignUpPresenter);
    void inject(EnterpriseSSOPresenter enterpriseSSOPresenter);
    void inject(MigrateDb migrateDb);

    void inject(CountriesCodesFragment codesFragment);
    void inject(EnterprisePhoneFragment phoneFragment);
    void inject(EnterprisePortalFragment portalFragment);
    void inject(EnterpriseSignInFragment signInFragment);
    void inject(EnterpriseSmsFragment enterpriseSmsFragment);
    void inject(PersonalPortalFragment personalPortalFragment);
    void inject(WebDavInterceptor webDavInterceptor);

    /*
    * Main
    * */
    void inject(AccountsPresenter accountsPresenter);
    void inject(MainActivityPresenter mainActivityPresenter);
    void inject(DocsCloudPresenter onlyOfficePresenter);
    void inject(DocsWebDavPresenter webDavPresenter);
    void inject(DocsOnDevicePresenter docsOnDevicePresenter);
    void inject(OperationActivity operationActivity);
    void inject(WebViewerFragment webViewerFragment);
    void inject(DocsBaseFragment docsBaseFragment);
    void inject(DocsOperationSectionFragment docsOperationSectionFragment);
    void inject(ExplorerAdapter explorerAdapter);
    void inject(MediaAdapter mediaAdapter);
    void inject(AppSettingsPresenter settingsPresenter);
    void inject(CloudAccountPresenter accountsPresenter);
    void inject(MainPagerPresenter mainPagerPresenter);

    /*
    * Media
    * */
    void inject(MediaVideoFragment mediaVideoFragment);
    void inject(MediaImageFragment mediaImageFragment);

    /*
    * Share
    * */
    void inject(SettingsPresenter settingsPresenter);
    void inject(AddPresenter addPresenter);
    void inject(ShareAddAdapter shareAddAdapter);

    /*
    * Storage
    * */
    void inject(SelectFragment selectFragment);
    void inject(WebTokenFragment webTokenFragment);
    void inject(ConnectFragment connectFragment);
    void inject(ConnectPresenter settingsFragment);
    void inject(WebDavFragment webDavFragment);

    /*
    * On boarding
    * */
    void inject(OnBoardingPagerFragment onBoardingPagerFragment);
    void inject(PortalsActivity portalsActivity);

    /*
    * Content provider
    * */
    void inject(AccountProvider accountProvider);

    void inject(ProfilePresenter settingsPresenter);
    void inject(DocsRecentPresenter docsRecentPresenter);

    void inject(AuthPagerFragment authPagerFragment);
    void inject(EnterpriseAppAuthPresenter  enterpriseAppAuthPresenter);
    void inject(SelectPresenter selectPresenter);
    void inject(AccountBottomDialog accountsBottomFragment);

    void inject(WebDavSignInPresenter webDavSignInPresenter);
}
