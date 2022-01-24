package app.editors.manager.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Singleton

@Module
class ToolModule {

    @Provides
    @Singleton
    fun providePhoneUtils(context: Context): PhoneNumberUtil {
        return PhoneNumberUtil.createInstance(context)
    }

}