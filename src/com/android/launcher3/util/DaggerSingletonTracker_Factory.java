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
public final class DaggerSingletonTracker_Factory implements Factory<DaggerSingletonTracker> {
    @Override
    public DaggerSingletonTracker get() {
        return newInstance();
    }

    public static DaggerSingletonTracker_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static DaggerSingletonTracker newInstance() {
        return new DaggerSingletonTracker();
    }

    private static final class InstanceHolder {
        static final DaggerSingletonTracker_Factory INSTANCE = new DaggerSingletonTracker_Factory();
    }
}
