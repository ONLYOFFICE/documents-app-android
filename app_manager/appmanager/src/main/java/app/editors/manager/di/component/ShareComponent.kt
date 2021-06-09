package app.editors.manager.di.component

import app.editors.manager.di.module.ShareModule
import app.editors.manager.di.module.ShareScope
import app.documents.core.share.ShareService
import dagger.Component

@Component(modules = [ShareModule::class], dependencies = [AppComponent::class])
@ShareScope
interface ShareComponent {

    val shareService: ShareService

}