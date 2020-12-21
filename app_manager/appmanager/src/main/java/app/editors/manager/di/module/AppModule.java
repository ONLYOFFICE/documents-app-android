package app.editors.manager.di.module;


import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Context mAppContext;

    public AppModule(Context context) {
        mAppContext = context;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return mAppContext;
    }

}
