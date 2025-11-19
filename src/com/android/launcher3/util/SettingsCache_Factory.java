package com.android.launcher3.util;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("com.android.launcher3.dagger.LauncherAppSingleton")
@QualifierMetadata("com.android.launcher3.dagger.ApplicationContext")
@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes",
        "KotlinInternal",
        "KotlinInternalInJava",
        "cast",
        "deprecation",
        "nullness:initialization.field.uninitialized"
})
public final class SettingsCache_Factory implements Factory<SettingsCache> {
    private final Provider<Context> contextProvider;

    private final Provider<DaggerSingletonTracker> trackerProvider;

    public SettingsCache_Factory(Provider<Context> contextProvider,
                                 Provider<DaggerSingletonTracker> trackerProvider) {
        this.contextProvider = contextProvider;
        this.trackerProvider = trackerProvider;
    }

    @Override
    public SettingsCache get() {
        return newInstance(contextProvider.get(), trackerProvider.get());
    }

    public static SettingsCache_Factory create(javax.inject.Provider<Context> contextProvider,
                                               javax.inject.Provider<DaggerSingletonTracker> trackerProvider) {
        return new SettingsCache_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(trackerProvider));
    }

    public static SettingsCache_Factory create(Provider<Context> contextProvider,
                                               Provider<DaggerSingletonTracker> trackerProvider) {
        return new SettingsCache_Factory(contextProvider, trackerProvider);
    }

    public static SettingsCache newInstance(Context context, DaggerSingletonTracker tracker) {
        return new SettingsCache(context, tracker);
    }
}
