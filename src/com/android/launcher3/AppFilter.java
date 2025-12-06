package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;

import com.android.launcher3.dagger.ApplicationContext;
import com.neoapps.neolauncher.NeoLauncher;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Utility class to filter out components from various lists
 */
public class AppFilter {

    private final Set<ComponentName> mFilteredComponents;

    // Edited
    @Inject
    public AppFilter(@ApplicationContext Context context) {
        mFilteredComponents = Arrays.stream(
                context.getResources().getStringArray(R.array.filtered_components))
                .map(ComponentName::unflattenFromString)
                .collect(Collectors.toSet());
        mFilteredComponents.add(new ComponentName(context, NeoLauncher.class.getName()));
    }

    public boolean shouldShowApp(ComponentName app) {
        return !mFilteredComponents.contains(app);
    }
}
