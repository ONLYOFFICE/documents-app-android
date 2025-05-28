package app.editors.manager.di.component

import android.content.Context
import app.documents.core.account.AddAccountHelper
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.datasource.RecentDataSource
import app.documents.core.database.migration.MigrationHelper
import app.documents.core.di.dagger.CoreModule
import app.documents.core.login.LoginComponent
import app.documents.core.manager.ManagerRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.interceptors.WebDavInterceptor
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.network.share.ShareService
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.OneDriveFileProvider
import app.documents.core.providers.RecentFileProvider
import app.documents.core.providers.RoomProvider
import app.documents.core.providers.WebDavFileProvider
import app.editors.manager.di.module.AppModule
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.tools.AppLocaleHelper
import app.editors.manager.managers.tools.CacheTool
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.managers.tools.ErrorHandler
import app.editors.manager.managers.tools.FontManager
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.usecase.SaveAccessSettingsUseCase
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.presenters.filter.BaseFilterPresenter
import app.editors.manager.mvp.presenters.filter.FilterAuthorPresenter
import app.editors.manager.mvp.presenters.login.EnterpriseAppAuthPresenter
import app.editors.manager.mvp.presenters.login.EnterpriseCreateLoginPresenter
import app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter
import app.editors.manager.mvp.presenters.login.EnterpriseSSOPresenter
import app.editors.manager.mvp.presenters.login.EnterpriseSmsPresenter
import app.editors.manager.mvp.presenters.login.OnlyOfficeCloudPresenter
import app.editors.manager.mvp.presenters.login.PasswordRecoveryPresenter
import app.editors.manager.mvp.presenters.login.PersonalLoginPresenter
import app.editors.manager.mvp.presenters.login.PersonalSignUpPresenter
import app.editors.manager.mvp.presenters.login.WebDavSignInPresenter
import app.editors.manager.mvp.presenters.main.CloudAccountPresenter
import app.editors.manager.mvp.presenters.main.DocsCloudPresenter
import app.editors.manager.mvp.presenters.main.DocsOnDevicePresenter
import app.editors.manager.mvp.presenters.main.DocsRecentPresenter
import app.editors.manager.mvp.presenters.main.DocsWebDavPresenter
import app.editors.manager.mvp.presenters.main.MainActivityPresenter
import app.editors.manager.mvp.presenters.main.MainPagerPresenter
import app.editors.manager.mvp.presenters.storage.ConnectPresenter
import app.editors.manager.mvp.presenters.storage.SelectPresenter
import app.editors.manager.mvp.presenters.storages.DocsDropboxPresenter
import app.editors.manager.mvp.presenters.storages.DocsGoogleDrivePresenter
import app.editors.manager.mvp.presenters.storages.DocsOneDrivePresenter
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.MediaAdapter
import app.editors.manager.ui.dialogs.fragments.OperationDialogFragment
import app.editors.manager.ui.fragments.login.AuthPagerFragment
import app.editors.manager.ui.fragments.login.CountriesCodesFragment
import app.editors.manager.ui.fragments.login.EnterprisePhoneFragment
import app.editors.manager.ui.fragments.login.EnterprisePortalFragment
import app.editors.manager.ui.fragments.login.EnterpriseSignInFragment
import app.editors.manager.ui.fragments.login.EnterpriseSmsFragment
import app.editors.manager.ui.fragments.login.PersonalPortalFragment
import app.editors.manager.ui.fragments.main.CloudsFragment
import app.editors.manager.ui.fragments.main.DocsBaseFragment
import app.editors.manager.ui.fragments.main.WebViewerFragment
import app.editors.manager.ui.fragments.main.settings.AppSettingsFragment
import app.editors.manager.ui.fragments.media.MediaImageFragment
import app.editors.manager.ui.fragments.media.MediaVideoFragment
import app.editors.manager.ui.fragments.operations.DocsOperationSectionFragment
import app.editors.manager.ui.fragments.room.order.RoomOrderDialogFragment
import app.editors.manager.ui.fragments.room.order.RoomOrderFragment
import app.editors.manager.ui.fragments.storage.ConnectFragment
import app.editors.manager.ui.fragments.storage.SelectFragment
import app.editors.manager.ui.fragments.storage.WebDavStorageFragment
import app.editors.manager.ui.fragments.storage.WebTokenFragment
import app.editors.manager.ui.fragments.storages.DocsDropboxFragment
import app.editors.manager.viewModels.login.EnterpriseCreateValidateViewModel
import app.editors.manager.viewModels.login.EnterprisePhoneViewModel
import app.editors.manager.viewModels.login.EnterprisePortalViewModel
import app.editors.manager.viewModels.login.RemoteUrlViewModel
import app.editors.manager.viewModels.main.ExplorerContextViewModel
import app.editors.manager.viewModels.room.RoomOrderHelper
import dagger.BindsInstance
import dagger.Component
import lib.toolkit.base.managers.tools.GlideTool
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import javax.inject.Singleton

@Component(modules = [CoreModule::class, AppModule::class])
@Singleton
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent

    }

    fun loginComponent(): LoginComponent.Factory

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
    val accountOnline: CloudAccount?
    val appLocaleHelper: AppLocaleHelper
    val resourcesProvider: ResourcesProvider
    val fontManager: FontManager
    val themePreferencesTools: ThemePreferencesTools
    val errorHandler: ErrorHandler
    val downloadReceiver: DownloadReceiver

    val cloudDataSource: CloudDataSource
    val recentDataSource: RecentDataSource

    val accountHelper: AddAccountHelper
    val migrationHelper: MigrationHelper
    val roomOrderHelper: RoomOrderHelper

    val shareService: ShareService
    val managerService: ManagerService
    val webDavService: WebDavService
    val roomService: RoomService

    val managerRepository: ManagerRepository
    val cloudFileProvider: CloudFileProvider
    val localFileProvider: LocalFileProvider
    val roomProvider: RoomProvider
    val webDavFileProvider: WebDavFileProvider
    val recentFileProvider: RecentFileProvider

    val saveAccessSettingsUseCase: SaveAccessSettingsUseCase

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
    fun inject(onlyOfficeCloudPresenter: OnlyOfficeCloudPresenter)
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
    fun inject(operationDialogFragment: OperationDialogFragment)
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
    fun inject(appSettingsFragment: AppSettingsFragment)

    /*
    * Media
    * */
    fun inject(mediaVideoFragment: MediaVideoFragment?)
    fun inject(mediaImageFragment: MediaImageFragment?)

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
    fun inject(portalsActivity: PortalsActivity?)

    /*
    * Content provider
    * */
    fun inject(oneDriveFileProvider: OneDriveFileProvider?)
    fun inject(docsRecentPresenter: DocsRecentPresenter?)
    fun inject(authPagerFragment: AuthPagerFragment?)
    fun inject(enterpriseAppAuthPresenter: EnterpriseAppAuthPresenter?)
    fun inject(selectPresenter: SelectPresenter?)
    fun inject(webDavSignInPresenter: WebDavSignInPresenter?)

    fun inject(viewModel: EnterprisePhoneViewModel)
    fun inject(viewModel: EnterprisePortalViewModel)
    fun inject(viewModel: EnterpriseCreateValidateViewModel)
    fun inject(viewModel: RemoteUrlViewModel)
    fun inject(viewModel: ExplorerContextViewModel)
    fun inject(passcodeActivity: PasscodeActivity?)
    fun inject(baseFilterPresenter: BaseFilterPresenter)
    fun inject(filterAuthorPresenter: FilterAuthorPresenter)
    fun inject(roomOrderDialogFragment: RoomOrderDialogFragment)
    fun inject(roomOrderFragment: RoomOrderFragment)
}