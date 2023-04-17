package app.editors.manager.di.component

import android.content.Context
import app.documents.core.storage.account.AccountDao
import app.documents.core.storage.account.AccountsDataBase
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.recent.RecentDao
import app.documents.core.storage.preference.NetworkSettings
import app.documents.core.network.common.interceptors.WebDavInterceptor
import app.editors.manager.di.module.AppModule
import app.editors.manager.managers.tools.CacheTool
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.filter.BaseFilterPresenter
import app.editors.manager.mvp.presenters.filter.FilterAuthorPresenter
import app.editors.manager.mvp.presenters.login.*
import app.editors.manager.mvp.presenters.main.*
import app.editors.manager.mvp.presenters.share.AddPresenter
import app.editors.manager.mvp.presenters.share.SettingsPresenter
import app.editors.manager.mvp.presenters.storage.ConnectPresenter
import app.editors.manager.mvp.presenters.storage.SelectPresenter
import app.documents.core.network.storages.dropbox.login.DropboxLoginHelper
import app.documents.core.providers.OneDriveFileProvider
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.ui.fragments.storages.DocsDropboxFragment
import app.editors.manager.mvp.presenters.storages.DocsGoogleDrivePresenter
import app.editors.manager.mvp.presenters.storages.GoogleDriveSignInPresenter
import app.editors.manager.mvp.presenters.storages.DocsOneDrivePresenter
import app.editors.manager.mvp.presenters.storages.OneDriveSingInPresenter
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.OperationActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.MediaAdapter
import app.editors.manager.ui.dialogs.explorer.ExplorerContextBottomDialog
import app.editors.manager.ui.fragments.login.*
import app.editors.manager.ui.fragments.main.CloudsFragment
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.main.WebViewerFragment
import app.editors.manager.ui.fragments.media.MediaImageFragment
import app.editors.manager.ui.fragments.media.MediaVideoFragment
import app.editors.manager.ui.fragments.onboarding.OnBoardingPagerFragment
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment
import app.editors.manager.ui.fragments.storage.ConnectFragment
import app.editors.manager.ui.fragments.storage.SelectFragment
import app.editors.manager.ui.fragments.storage.WebDavStorageFragment
import app.editors.manager.ui.fragments.storage.WebTokenFragment
import app.editors.manager.viewModels.login.EnterpriseCreateValidateViewModel
import app.editors.manager.viewModels.login.EnterprisePhoneViewModel
import app.editors.manager.viewModels.login.EnterprisePortalViewModel
import app.editors.manager.viewModels.login.RemoteUrlViewModel
import app.editors.manager.viewModels.main.AppSettingsViewModel
import app.editors.manager.viewModels.main.ExplorerContextViewModel
import dagger.BindsInstance
import dagger.Component
import lib.toolkit.base.managers.tools.GlideTool
import lib.toolkit.base.managers.tools.LocalContentTools
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {

    @Component.Builder
    interface Builder{

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent

    }
    /*
    * TODO scopes!
    * */
    val context: Context
    val preference: PreferenceTool
    val countriesCodes: CountriesCodesTool
    val cacheTool: CacheTool
    val sectionsState: OperationsState
    val contentTools: LocalContentTools
    val glideTools: GlideTool
    val networkSettings: NetworkSettings
    val accountsDataBase: AccountsDataBase
    val accountsDao: AccountDao
    val accountOnline: CloudAccount?
    val recentDao: RecentDao?

    /*
   * Login
   * */
    fun inject(enterpriseSignInPresenter: EnterpriseLoginPresenter?)
    fun inject(enterpriseSmsPresenter: EnterpriseSmsPresenter?)
    fun inject(enterpriseCreateSignInPresenter: EnterpriseCreateLoginPresenter?)
    fun inject(personalSignInPresenter: PersonalLoginPresenter?)
    fun inject(personalSignUpPresenter: PersonalSignUpPresenter?)
    fun inject(enterpriseSSOPresenter: EnterpriseSSOPresenter?)
    fun inject(codesFragment: CountriesCodesFragment?)
    fun inject(phoneFragment: EnterprisePhoneFragment?)
    fun inject(portalFragment: EnterprisePortalFragment?)
    fun inject(signInFragment: EnterpriseSignInFragment?)
    fun inject(enterpriseSmsFragment: EnterpriseSmsFragment?)
    fun inject(personalPortalFragment: PersonalPortalFragment?)
    fun inject(webDavInterceptor: WebDavInterceptor?)
    fun inject(passwordRecoveryPresenter: PasswordRecoveryPresenter)
    fun inject(oneDriveSignInPresenter: OneDriveSingInPresenter?)
    fun inject(splashFragment: SplashFragment?)
    fun inject(googleDriveSignInPresenter: GoogleDriveSignInPresenter?)
    fun inject(onlyOfficeCloudPresenter: OnlyOfficeCloudPresenter)
    fun inject(dropboxLoginHelper: DropboxLoginHelper)
    fun inject(cloudsFragment: CloudsFragment)
    fun inject(webDavLoginActivity: WebDavLoginActivity)
    fun inject(DocsDropboxFragment: DocsDropboxFragment)

    /*
    * Main
    * */
    fun inject(mainActivityPresenter: MainActivityPresenter?)
    fun inject(onlyOfficePresenter: DocsCloudPresenter?)
    fun inject(webDavPresenter: DocsWebDavPresenter?)
    fun inject(docsOnDevicePresenter: DocsOnDevicePresenter?)
    fun inject(operationActivity: OperationActivity?)
    fun inject(webViewerFragment: WebViewerFragment?)
    fun inject(docsBaseFragment: DocsBaseFragment?)
    fun inject(docsOperationSectionFragment: DocsOperationSectionFragment?)
    fun inject(explorerAdapter: ExplorerAdapter?)
    fun inject(mediaAdapter: MediaAdapter?)
    fun inject(accountsPresenter: CloudAccountPresenter?)
    fun inject(mainPagerPresenter: MainPagerPresenter?)
    fun inject(docsOneDrivePresenter: DocsOneDrivePresenter?)
    fun inject(docsDropboxPresenter: DocsDropboxPresenter?)
    fun inject(docsGoogleDrivePresenter: DocsGoogleDrivePresenter?)

    /*
    * Media
    * */
    fun inject(mediaVideoFragment: MediaVideoFragment?)
    fun inject(mediaImageFragment: MediaImageFragment?)

    /*
    * Share
    * */
    fun inject(settingsPresenter: SettingsPresenter?)
    fun inject(addPresenter: AddPresenter?)

    /*
    * Storage
    * */
    fun inject(selectFragment: SelectFragment?)
    fun inject(webTokenFragment: WebTokenFragment?)
    fun inject(connectFragment: ConnectFragment?)
    fun inject(settingsFragment: ConnectPresenter?)
    fun inject(webDavFragment: WebDavStorageFragment?)

    /*
    * On boarding
    * */
    fun inject(onBoardingPagerFragment: OnBoardingPagerFragment?)
    fun inject(portalsActivity: PortalsActivity?)

    /*
    * Content provider
    * */
    fun inject(settingsPresenter: ProfilePresenter?)
    fun inject(oneDriveFileProvider: OneDriveFileProvider?)
    fun inject(docsRecentPresenter: DocsRecentPresenter?)
    fun inject(authPagerFragment: AuthPagerFragment?)
    fun inject(enterpriseAppAuthPresenter: EnterpriseAppAuthPresenter?)
    fun inject(selectPresenter: SelectPresenter?)
    fun inject(webDavSignInPresenter: WebDavSignInPresenter?)

    fun inject(viewModel: AppSettingsViewModel)
    fun inject(viewModel: EnterprisePhoneViewModel)
    fun inject(viewModel: EnterprisePortalViewModel)
    fun inject(viewModel: EnterpriseCreateValidateViewModel)
    fun inject(viewModel: RemoteUrlViewModel)
    fun inject(viewModel: ExplorerContextViewModel)
    fun inject(passcodeActivity: PasscodeActivity?)
    fun inject(baseFilterPresenter: BaseFilterPresenter)
    fun inject(filterAuthorPresenter: FilterAuthorPresenter)
}