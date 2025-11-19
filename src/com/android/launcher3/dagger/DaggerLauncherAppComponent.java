package com.android.launcher3.dagger;

import android.content.Context;
import com.android.launcher3.contextualeducation.ContextualEduStatsManager;
import com.android.launcher3.contextualeducation.ContextualEduStatsManager_Factory;
import com.android.launcher3.graphics.IconShape;
import com.android.launcher3.graphics.IconShape_Factory;
import com.android.launcher3.model.ItemInstallQueue;
import com.android.launcher3.model.ItemInstallQueue_Factory;
import com.android.launcher3.pm.InstallSessionHelper;
import com.android.launcher3.pm.InstallSessionHelper_Factory;
import com.android.launcher3.util.ApiWrapper;
import com.android.launcher3.util.ApiWrapper_Factory;
import com.android.launcher3.util.DaggerSingletonTracker;
import com.android.launcher3.util.DaggerSingletonTracker_Factory;
import com.android.launcher3.util.DynamicResource;
import com.android.launcher3.util.DynamicResource_Factory;
import com.android.launcher3.util.MSDLPlayerWrapper;
import com.android.launcher3.util.MSDLPlayerWrapper_Factory;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PackageManagerHelper_Factory;
import com.android.launcher3.util.PluginManagerWrapper;
import com.android.launcher3.util.PluginManagerWrapper_Factory;
import com.android.launcher3.util.ScreenOnTracker;
import com.android.launcher3.util.ScreenOnTracker_Factory;
import com.android.launcher3.util.SettingsCache;
import com.android.launcher3.util.SettingsCache_Factory;
import com.android.launcher3.util.VibratorWrapper;
import com.android.launcher3.util.VibratorWrapper_Factory;
import com.android.launcher3.util.window.RefreshRateTracker;
import com.android.launcher3.util.window.RefreshRateTracker_Factory;
import com.android.launcher3.widget.custom.CustomWidgetManager;
import com.android.launcher3.widget.custom.CustomWidgetManager_Factory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import javax.annotation.processing.Generated;

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
public final class DaggerLauncherAppComponent {
    private DaggerLauncherAppComponent() {
    }

    public static LauncherAppComponent.Builder builder() {
        return new Builder();
    }

    private static final class Builder implements LauncherAppComponent.Builder {
        private Context appContext;

        @Override
        public Builder appContext(Context context) {
            this.appContext = Preconditions.checkNotNull(context);
            return this;
        }

        @Override
        public LauncherAppComponent build() {
            Preconditions.checkBuilderRequirement(appContext, Context.class);
            return new LauncherAppComponentImpl(appContext);
        }
    }

    private static final class LauncherAppComponentImpl implements LauncherAppComponent {
        private final LauncherAppComponentImpl launcherAppComponentImpl = this;

        private Provider<DaggerSingletonTracker> daggerSingletonTrackerProvider;

        private Provider<Context> appContextProvider;

        private Provider<ApiWrapper> apiWrapperProvider;

        private Provider<ContextualEduStatsManager> contextualEduStatsManagerProvider;

        private Provider<PluginManagerWrapper> pluginManagerWrapperProvider;

        private Provider<CustomWidgetManager> customWidgetManagerProvider;

        private Provider<DynamicResource> dynamicResourceProvider;

        private Provider<IconShape> iconShapeProvider;

        private Provider<InstallSessionHelper> installSessionHelperProvider;

        private Provider<ItemInstallQueue> itemInstallQueueProvider;

        private Provider<RefreshRateTracker> refreshRateTrackerProvider;

        private Provider<ScreenOnTracker> screenOnTrackerProvider;

        private Provider<SettingsCache> settingsCacheProvider;

        private Provider<PackageManagerHelper> packageManagerHelperProvider;

        private Provider<VibratorWrapper> vibratorWrapperProvider;

        private Provider<MSDLPlayerWrapper> mSDLPlayerWrapperProvider;

        private LauncherAppComponentImpl(Context appContextParam) {

            initialize(appContextParam);

        }

        @SuppressWarnings("unchecked")
        private void initialize(final Context appContextParam) {
            this.daggerSingletonTrackerProvider = DoubleCheck.provider(DaggerSingletonTracker_Factory.create());
            this.appContextProvider = InstanceFactory.create(appContextParam);
            this.apiWrapperProvider = DoubleCheck.provider(ApiWrapper_Factory.create(appContextProvider));
            this.contextualEduStatsManagerProvider = DoubleCheck.provider(ContextualEduStatsManager_Factory.create());
            this.pluginManagerWrapperProvider = DoubleCheck.provider(PluginManagerWrapper_Factory.create());
            this.customWidgetManagerProvider = DoubleCheck.provider(CustomWidgetManager_Factory.create(appContextProvider, pluginManagerWrapperProvider, daggerSingletonTrackerProvider));
            this.dynamicResourceProvider = DoubleCheck.provider(DynamicResource_Factory.create(appContextProvider, pluginManagerWrapperProvider, daggerSingletonTrackerProvider));
            this.iconShapeProvider = DoubleCheck.provider(IconShape_Factory.create(appContextProvider));
            this.installSessionHelperProvider = DoubleCheck.provider(InstallSessionHelper_Factory.create(appContextProvider));
            this.itemInstallQueueProvider = DoubleCheck.provider(ItemInstallQueue_Factory.create(appContextProvider));
            this.refreshRateTrackerProvider = DoubleCheck.provider(RefreshRateTracker_Factory.create(appContextProvider, daggerSingletonTrackerProvider));
            this.screenOnTrackerProvider = DoubleCheck.provider(ScreenOnTracker_Factory.create(appContextProvider, daggerSingletonTrackerProvider));
            this.settingsCacheProvider = DoubleCheck.provider(SettingsCache_Factory.create(appContextProvider, daggerSingletonTrackerProvider));
            this.packageManagerHelperProvider = DoubleCheck.provider(PackageManagerHelper_Factory.create(appContextProvider));
            this.vibratorWrapperProvider = DoubleCheck.provider(VibratorWrapper_Factory.create(appContextProvider, settingsCacheProvider, daggerSingletonTrackerProvider));
            this.mSDLPlayerWrapperProvider = DoubleCheck.provider(MSDLPlayerWrapper_Factory.create(appContextProvider));
        }

        @Override
        public DaggerSingletonTracker getDaggerSingletonTracker() {
            return daggerSingletonTrackerProvider.get();
        }

        @Override
        public ApiWrapper getApiWrapper() {
            return apiWrapperProvider.get();
        }

        @Override
        public ContextualEduStatsManager getContextualEduStatsManager() {
            return contextualEduStatsManagerProvider.get();
        }

        @Override
        public CustomWidgetManager getCustomWidgetManager() {
            return customWidgetManagerProvider.get();
        }

        @Override
        public DynamicResource getDynamicResource() {
            return dynamicResourceProvider.get();
        }

        @Override
        public IconShape getIconShape() {
            return iconShapeProvider.get();
        }

        @Override
        public InstallSessionHelper getInstallSessionHelper() {
            return installSessionHelperProvider.get();
        }

        @Override
        public ItemInstallQueue getItemInstallQueue() {
            return itemInstallQueueProvider.get();
        }

        @Override
        public RefreshRateTracker getRefreshRateTracker() {
            return refreshRateTrackerProvider.get();
        }

        @Override
        public ScreenOnTracker getScreenOnTracker() {
            return screenOnTrackerProvider.get();
        }

        @Override
        public SettingsCache getSettingsCache() {
            return settingsCacheProvider.get();
        }

        @Override
        public PackageManagerHelper getPackageManagerHelper() {
            return packageManagerHelperProvider.get();
        }

        @Override
        public PluginManagerWrapper getPluginManagerWrapper() {
            return pluginManagerWrapperProvider.get();
        }

        @Override
        public VibratorWrapper getVibratorWrapper() {
            return vibratorWrapperProvider.get();
        }

        @Override
        public MSDLPlayerWrapper getMSDLPlayerWrapper() {
            return mSDLPlayerWrapperProvider.get();
        }
    }
}
