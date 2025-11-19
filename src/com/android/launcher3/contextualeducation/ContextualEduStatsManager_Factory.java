package com.android.launcher3.contextualeducation;

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
public final class ContextualEduStatsManager_Factory implements Factory<ContextualEduStatsManager> {
    @Override
    public ContextualEduStatsManager get() {
        return newInstance();
    }

    public static ContextualEduStatsManager_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static ContextualEduStatsManager newInstance() {
        return new ContextualEduStatsManager();
    }

    private static final class InstanceHolder {
        static final ContextualEduStatsManager_Factory INSTANCE = new ContextualEduStatsManager_Factory();
    }
}
