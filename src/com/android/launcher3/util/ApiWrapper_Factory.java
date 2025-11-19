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
public final class ApiWrapper_Factory implements Factory<ApiWrapper> {
    private final Provider<Context> contextProvider;

    public ApiWrapper_Factory(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public ApiWrapper get() {
        return newInstance(contextProvider.get());
    }

    public static ApiWrapper_Factory create(javax.inject.Provider<Context> contextProvider) {
        return new ApiWrapper_Factory(Providers.asDaggerProvider(contextProvider));
    }

    public static ApiWrapper_Factory create(Provider<Context> contextProvider) {
        return new ApiWrapper_Factory(contextProvider);
    }

    public static ApiWrapper newInstance(Context context) {
        return new ApiWrapper(context);
    }
}
