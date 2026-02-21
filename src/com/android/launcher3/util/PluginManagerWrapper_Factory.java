package com.android.launcher3.util;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("com.android.launcher3.dagger.LauncherAppSingleton")
@QualifierMetadata
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
public final class PluginManagerWrapper_Factory implements Factory<PluginManagerWrapper> {
    @Override
    public PluginManagerWrapper get() {
        return newInstance();
    }

    public static PluginManagerWrapper_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static PluginManagerWrapper newInstance() {
        return new PluginManagerWrapper();
    }

    private static final class InstanceHolder {
        static final PluginManagerWrapper_Factory INSTANCE = new PluginManagerWrapper_Factory();
    }
}
