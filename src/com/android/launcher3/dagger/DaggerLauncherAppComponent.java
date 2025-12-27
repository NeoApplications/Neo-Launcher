package com.android.launcher3.dagger;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherPrefs;
import com.android.launcher3.MainProcessInitializer;
import com.android.launcher3.RemoveAnimationSettingsTracker;
import com.android.launcher3.backuprestore.LauncherRestoreEventLogger;
import com.android.launcher3.compose.core.widgetpicker.WidgetPickerComposeWrapper;
import com.android.launcher3.contextualeducation.ContextualEduStatsManager;
import com.android.launcher3.contextualeducation.ContextualEduStatsManager_Factory;
import com.android.launcher3.dragndrop.SystemDragController;
import com.android.launcher3.folder.FolderNameSuggestionLoader;
import com.android.launcher3.graphics.GridCustomizationsProxy;
import com.android.launcher3.graphics.ThemeManager;
import com.android.launcher3.graphics.theme.ThemePreference;
import com.android.launcher3.homescreenfiles.HomeScreenFilesProvider;
import com.android.launcher3.icons.IconChangeTracker;
import com.android.launcher3.icons.LauncherIcons;
import com.android.launcher3.logging.DumpManager;
import com.android.launcher3.logging.StatsLogManager;
import com.android.launcher3.model.GridSizeMigrationLogic;
import com.android.launcher3.model.LayoutParserFactory;
import com.android.launcher3.model.LoaderCursor;
import com.android.launcher3.model.TestableModelState;
import com.android.launcher3.model.WidgetsFilterDataProvider;
import com.android.launcher3.notification.NotificationRepository;
import com.android.launcher3.pm.UserCache;
import com.android.launcher3.popup.PopupDataRepository;
import com.android.launcher3.qsb.OSEManager;
import com.android.launcher3.qsb.OseWidgetManager;
import com.android.launcher3.qsb.QsbAppWidgetHost;
import com.android.launcher3.testing.TestInformationHandler;
import com.android.launcher3.util.DisplayController;
import com.android.launcher3.util.InstantAppResolver;
import com.android.launcher3.util.LayoutImportExportHelper;
import com.android.launcher3.util.LockedUserState;
import com.android.launcher3.util.TaskbarModeUtil;
import com.android.launcher3.util.WallpaperColorHints;
import com.android.launcher3.util.window.WindowManagerProxy;
import com.android.launcher3.widget.LauncherWidgetHolder;
import com.android.launcher3.widget.util.WidgetSizeHandler;
import com.saggitt.omega.icons.IconShape;
import com.android.launcher3.model.ItemInstallQueue;
import com.android.launcher3.pm.InstallSessionHelper;
import com.android.launcher3.util.ApiWrapper;
import com.android.launcher3.util.ApiWrapper_Factory;
import com.android.launcher3.util.DaggerSingletonTracker;
import com.android.launcher3.util.DaggerSingletonTracker_Factory;
import com.android.launcher3.util.DynamicResource;
import com.android.launcher3.util.MSDLPlayerWrapper;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PluginManagerWrapper;
import com.android.launcher3.util.PluginManagerWrapper_Factory;
import com.android.launcher3.util.ScreenOnTracker;
import com.android.launcher3.util.SettingsCache;
import com.android.launcher3.util.SettingsCache_Factory;
import com.android.launcher3.util.VibratorWrapper;
import com.android.launcher3.util.window.RefreshRateTracker;
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
        public LauncherBaseAppComponent.Builder iconsDbName(@Nullable String dbFileName) {
            return null;
        }

        @Override
        public LauncherBaseAppComponent.Builder setSafeModeEnabled(boolean safeModeEnabled) {
            return null;
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
            /*this.dynamicResourceProvider = DoubleCheck.provider(DynamicResource_Factory.create(appContextProvider, pluginManagerWrapperProvider, daggerSingletonTrackerProvider));
            this.iconShapeProvider = DoubleCheck.provider(IconShape_Factory.create(appContextProvider));
            this.installSessionHelperProvider = DoubleCheck.provider(InstallSessionHelper_Factory.create(appContextProvider));
            this.itemInstallQueueProvider = DoubleCheck.provider(ItemInstallQueue_Factory.create(appContextProvider));
            this.refreshRateTrackerProvider = DoubleCheck.provider(RefreshRateTracker_Factory.create(appContextProvider, daggerSingletonTrackerProvider));
            this.screenOnTrackerProvider = DoubleCheck.provider(ScreenOnTracker_Factory.create(appContextProvider, daggerSingletonTrackerProvider));
            this.settingsCacheProvider = DoubleCheck.provider(SettingsCache_Factory.create(appContextProvider, daggerSingletonTrackerProvider));
            this.packageManagerHelperProvider = DoubleCheck.provider(PackageManagerHelper_Factory.create(appContextProvider));
            this.vibratorWrapperProvider = DoubleCheck.provider(VibratorWrapper_Factory.create(appContextProvider, settingsCacheProvider, daggerSingletonTrackerProvider));
            this.mSDLPlayerWrapperProvider = DoubleCheck.provider(MSDLPlayerWrapper_Factory.create(appContextProvider));*/
        }

        @Override
        public DaggerSingletonTracker getDaggerSingletonTracker() {
            return daggerSingletonTrackerProvider.get();
        }

        @Override
        public ApiWrapper getApiWrapper() {
            return apiWrapperProvider.get();
        }

        public ContextualEduStatsManager getContextualEduStatsManager() {
            return contextualEduStatsManagerProvider.get();
        }

        @Override
        public LauncherWidgetHolder.WidgetHolderFactory getWidgetHolderFactory() {
            return null;
        }

        @Override
        public RefreshRateTracker getFrameRateProvider() {
            return null;
        }

        @Override
        public InstantAppResolver getInstantAppResolver() {
            return null;
        }

        @Override
        public DumpManager getDumpManager() {
            return null;
        }

        @Override
        public StatsLogManager.StatsLogManagerFactory getStatsLogManagerFactory() {
            return null;
        }

        @Override
        public ActivityContextComponent.Builder getActivityContextComponentBuilder() {
            return null;
        }

        @Override
        public WidgetPickerComposeWrapper getWidgetPickerComposeWrapper() {
            return null;
        }

        @Override
        public WidgetSizeHandler getWidgetSizeHandler() {
            return null;
        }

        @Override
        public MainProcessInitializer getMainProcessInitializer() {
            return null;
        }

        @Override
        public OseWidgetManager getOseWidgetManager() {
            return null;
        }

        @Override
        public OSEManager getOseManager() {
            return null;
        }

        @Override
        public QsbAppWidgetHost getQsbAppWidgetHost() {
            return null;
        }

        @Override
        public TestInformationHandler getTestInformationHandler() {
            return null;
        }

        @Override
        public TaskbarModeUtil getTaskbarModeUtil() {
            return null;
        }

        @Override
        public SystemDragController getSystemDragController() {
            return null;
        }

        @Override
        public LayoutImportExportHelper getLayoutImportExportHelper() {
            return null;
        }

        @Override
        public LayoutParserFactory getLayoutParserFactory() {
            return null;
        }

        @Override
        public GridSizeMigrationLogic createNewGridSizeMigrationLogic() {
            return null;
        }

        @Override
        public TestableModelState getTestableModelState() {
            return null;
        }

        @Override
        public PopupDataRepository getPopupDataRepository() {
            return null;
        }

        @Override
        public NotificationRepository getNotificationRepository() {
            return null;
        }

        @Override
        public HomeScreenFilesProvider getHomeScreenFilesProvider() {
            return null;
        }

        @Override
        public ThemePreference getThemePreference() {
            return null;
        }

        @Override
        public IconChangeTracker getIconChangeTracker() {
            return null;
        }

        @Override
        public CustomWidgetManager getCustomWidgetManager() {
            return customWidgetManagerProvider.get();
        }

        @Override
        public DynamicResource getDynamicResource() {
            return dynamicResourceProvider.get();
        }

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

        @Override
        public WindowManagerProxy getWmProxy() {
            return null;
        }

        @Override
        public LauncherPrefs getLauncherPrefs() {
            return null;
        }

        @Override
        public ThemeManager getThemeManager() {
            return null;
        }

        @Override
        public UserCache getUserCache() {
            return null;
        }

        @Override
        public DisplayController getDisplayController() {
            return null;
        }

        @Override
        public WallpaperColorHints getWallpaperColorHints() {
            return null;
        }

        @Override
        public LockedUserState getLockedUserState() {
            return null;
        }

        @Override
        public InvariantDeviceProfile getIDP() {
            return null;
        }

        @Override
        public LauncherIcons.IconPool getIconPool() {
            return null;
        }

        @Override
        public RemoveAnimationSettingsTracker getRemoveAnimationSettingsTracker() {
            return null;
        }

        @Override
        public LauncherAppState getLauncherAppState() {
            return null;
        }

        @Override
        public LauncherRestoreEventLogger getLauncherRestoreEventLogger() {
            return null;
        }

        @Override
        public GridCustomizationsProxy getGridCustomizationsProxy() {
            return null;
        }

        @Override
        public FolderNameSuggestionLoader getFolderNameSuggestionLoader() {
            return null;
        }

        public WidgetsFilterDataProvider getWidgetsFilterDataProvider() {
            return null;
        }

        @Override
        public LoaderCursor.LoaderCursorFactory getLoaderCursorFactory() {
            return null;
        }
    }
}
