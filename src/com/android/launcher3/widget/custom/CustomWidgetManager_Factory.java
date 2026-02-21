package com.android.launcher3.widget.custom;

import android.content.Context;
import com.android.launcher3.util.DaggerSingletonTracker;
import com.android.launcher3.util.PluginManagerWrapper;
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
public final class CustomWidgetManager_Factory implements Factory<CustomWidgetManager> {
    private final Provider<Context> contextProvider;

    private final Provider<PluginManagerWrapper> pluginManagerProvider;

    private final Provider<DaggerSingletonTracker> trackerProvider;

    public CustomWidgetManager_Factory(Provider<Context> contextProvider,
                                       Provider<PluginManagerWrapper> pluginManagerProvider,
                                       Provider<DaggerSingletonTracker> trackerProvider) {
        this.contextProvider = contextProvider;
        this.pluginManagerProvider = pluginManagerProvider;
        this.trackerProvider = trackerProvider;
    }

    @Override
    public CustomWidgetManager get() {
        return newInstance(contextProvider.get(), pluginManagerProvider.get(), trackerProvider.get());
    }

    public static CustomWidgetManager_Factory create(javax.inject.Provider<Context> contextProvider,
                                                     javax.inject.Provider<PluginManagerWrapper> pluginManagerProvider,
                                                     javax.inject.Provider<DaggerSingletonTracker> trackerProvider) {
        return new CustomWidgetManager_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(pluginManagerProvider), Providers.asDaggerProvider(trackerProvider));
    }

    public static CustomWidgetManager_Factory create(Provider<Context> contextProvider,
                                                     Provider<PluginManagerWrapper> pluginManagerProvider,
                                                     Provider<DaggerSingletonTracker> trackerProvider) {
        return new CustomWidgetManager_Factory(contextProvider, pluginManagerProvider, trackerProvider);
    }

    public static CustomWidgetManager newInstance(Context context, PluginManagerWrapper pluginManager,
                                                  DaggerSingletonTracker tracker) {
        return new CustomWidgetManager(context, pluginManager, tracker);
    }
}
