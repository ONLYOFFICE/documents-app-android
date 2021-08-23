package app.editors.manager.di.module

import android.content.Context
import app.editors.manager.managers.tools.AccountSqlTool
import app.editors.manager.managers.tools.CacheTool
import dagger.Module
import dagger.Provides
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Singleton

@Module
class ToolModule {

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
    fun providePhoneUtils(context: Context): PhoneNumberUtil {
        return PhoneNumberUtil.createInstance(context)
    }

}