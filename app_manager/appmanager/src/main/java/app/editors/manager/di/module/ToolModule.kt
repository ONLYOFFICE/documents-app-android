package app.editors.manager.di.module

import android.content.Context
import app.editors.manager.managers.tools.AccountSqlTool
import app.editors.manager.managers.tools.CacheTool
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.states.OperationsState
import dagger.Module
import dagger.Provides
import lib.toolkit.base.managers.tools.GlideTool
import lib.toolkit.base.managers.tools.LocalContentTools
import javax.inject.Singleton

@Module
class ToolModule {

    @Provides
    @Singleton
    fun providePref(context: Context): PreferenceTool {
        return PreferenceTool(context)
    }

    @Provides
    @Singleton
    fun provideCountryCodes(context: Context): CountriesCodesTool {
        return CountriesCodesTool(context)
    }

    @Provides
    @Singleton
    fun provideGlide(context: Context): GlideTool {
        return GlideTool(context)
    }

    @Provides
    @Singleton
    fun provideAccountsSql(context: Context): AccountSqlTool {
        return AccountSqlTool(context)
    }

    @Provides
    @Singleton
    fun provideCacheTool(context: Context): CacheTool {
        return CacheTool(context)
    }

    @Provides
    @Singleton
    fun provideSectionsState(): OperationsState {
        return OperationsState()
    }

    @Provides
    @Singleton
    fun provideContentTool(context: Context): LocalContentTools {
        return LocalContentTools(context)
    }

}