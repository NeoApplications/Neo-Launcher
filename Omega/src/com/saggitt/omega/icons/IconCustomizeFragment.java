/*
 * Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.saggitt.omega.icons;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.util.OmegaUtilsKt;

public class IconCustomizeFragment extends Fragment {

    private RecyclerView shapeView;
    private Context mContext;
    private IconCustomizeAdapter adapter;
    private View coloredView;
    private View shapeLessView;
    private View legacyView;
    private View whiteView;
    private View adaptiveView;
    private OmegaPreferences prefs;
    private boolean coloredIcons;
    private boolean shapeLess;
    private boolean legacy;
    private boolean white;
    private boolean adaptive;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_icon_customization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        prefs = Utilities.getOmegaPrefs(mContext);
        coloredIcons = prefs.getColorizedLegacyTreatment();
        shapeLess = prefs.getForceShapeless();
        legacy = prefs.getEnableLegacyTreatment();
        white = prefs.getEnableWhiteOnlyTreatment();
        adaptive = prefs.getAdaptifyIconPacks();

        shapeView = view.findViewById(R.id.shape_view);
        shapeView.setLayoutManager(new LinearLayoutManager(mContext));
        StaggeredGridLayoutManager mStaggeredGridLayoutManager =
                new StaggeredGridLayoutManager(4, GridLayoutManager.VERTICAL);
        shapeView.setLayoutManager(mStaggeredGridLayoutManager);
        adapter = new IconCustomizeAdapter(mContext);
        shapeView.setAdapter(adapter);

        coloredView = view.findViewById(R.id.colored_icons);
        shapeLessView = view.findViewById(R.id.shapeless_icons);
        legacyView = view.findViewById(R.id.legacy_icons);
        whiteView = view.findViewById(R.id.white_icons);
        adaptiveView = view.findViewById(R.id.adaptive_icons);

        setupSwitchView(shapeLessView, shapeLess);
        setupSwitchView(legacyView, legacy);
        setupSwitchView(whiteView, white);
        setupSwitchView(adaptiveView, adaptive);
        setupSwitchView(coloredView, coloredIcons);
        hideViews();
    }

    public void onPause() {
        super.onPause();
        prefs.reloadIcons();
    }

    /*
     * Hidde options when the android version is lower than oreo
     * */
    private void hideViews() {
        if (!Utilities.ATLEAST_OREO) {
            coloredView.setVisibility(View.GONE);
            shapeLessView.setVisibility(View.GONE);
            legacyView.setVisibility(View.GONE);
            whiteView.setVisibility(View.GONE);
            adaptiveView.setVisibility(View.GONE);
        }
    }

    /*
     * Sync switch view according to the preference state.
     * */
    private void setupSwitchView(View itemView, boolean isChecked) {
        Switch switchView = itemView.findViewById(R.id.switchWidget);
        OmegaUtilsKt.applyColor(switchView, prefs.getAccentColor());
        syncSwitch(switchView, isChecked);
        itemView.setOnClickListener(view -> {
            doOnClick(view, switchView);
        });
    }

    public void doOnClick(View view, Switch switchView) {
        if (view == coloredView) {
            coloredIcons = !coloredIcons;
            syncSwitch(switchView, coloredIcons);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_colorizeGeneratedBackgrounds", coloredIcons)
                    .apply();
            if (!coloredIcons) {
                updateWhite(false);
            } else {
                updateWhite(true);
            }
        } else if (view == shapeLessView) {
            shapeLess = !shapeLess;
            syncSwitch(switchView, shapeLess);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_forceShapeless", shapeLess)
                    .apply();
        } else if (view == legacyView) {
            legacy = !legacy;
            syncSwitch(switchView, legacy);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_enableLegacyTreatment", legacy)
                    .apply();
            if (!legacy) {
                updateColoredBackground(false);
                updateAdaptive(false);
                updateWhite(false);
            } else {
                updateColoredBackground(true);
                updateAdaptive(true);
                updateWhite(true);
            }
        } else if (view == whiteView) {
            white = !white;
            syncSwitch(switchView, white);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_enableWhiteOnlyTreatment", white)
                    .apply();
        } else if (view == adaptiveView) {
            adaptive = !adaptive;
            syncSwitch(switchView, adaptive);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_generateAdaptiveForIconPack", adaptive)
                    .apply();
        }
    }

    private void updateColoredBackground(boolean state) {
        if (!state) {
            coloredView.setClickable(false);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_colorizeGeneratedBackgrounds", false)
                    .apply();
            coloredView.findViewById(R.id.switchWidget).setEnabled(false);
        } else {
            coloredView.setClickable(true);
            coloredView.findViewById(R.id.switchWidget).setEnabled(true);
        }
    }

    private void updateWhite(boolean state) {
        if (!state) {
            whiteView.setClickable(false);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_enableWhiteOnlyTreatment", false)
                    .apply();
            whiteView.findViewById(R.id.switchWidget).setEnabled(false);
        } else {
            whiteView.setClickable(true);
            whiteView.findViewById(R.id.switchWidget).setEnabled(true);
        }
    }

    private void updateAdaptive(boolean state) {
        if (!state) {
            adaptiveView.setClickable(false);
            Utilities.getPrefs(mContext)
                    .edit()
                    .putBoolean("pref_generateAdaptiveForIconPack", false)
                    .apply();
            adaptiveView.findViewById(R.id.switchWidget).setEnabled(false);
        } else {
            adaptiveView.setClickable(true);
            adaptiveView.findViewById(R.id.switchWidget).setEnabled(true);
        }
    }

    private void syncSwitch(Switch switchCompat, boolean checked) {
        switchCompat.setChecked(checked);
    }
}
