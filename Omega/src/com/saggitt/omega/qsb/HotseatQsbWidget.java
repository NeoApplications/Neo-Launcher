package com.saggitt.omega.qsb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Process;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.util.PackageManagerHelper;
import com.saggitt.omega.OmegaPreferences;
import com.saggitt.omega.search.SearchProvider;
import com.saggitt.omega.search.SearchProviderController;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.saggitt.omega.util.Config.GOOGLE_QSB;

public class HotseatQsbWidget extends AbstractQsbLayout implements QsbChangeListener,
        OmegaPreferences.OnPreferenceChangeListener {
    public static final String KEY_DOCK_HIDE = "pref_hideHotseat";
    public static final String KEY_DOCK_COLORED_GOOGLE = "pref_dockColoredGoogle";
    public static final String KEY_DOCK_SEARCHBAR = "pref_dockSearchBar";
    private final QsbConfiguration Ds;
    private boolean mIsGoogleColored;
    private boolean widgetMode;

    public HotseatQsbWidget(Context context) {
        this(context, null);
    }

    public HotseatQsbWidget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HotseatQsbWidget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.Ds = QsbConfiguration.getInstance(context);
        setOnClickListener(this);
    }

    static void a(HotseatQsbWidget hotseatQsbWidget) {
        if (hotseatQsbWidget.mIsGoogleColored != hotseatQsbWidget.isGoogleColored()) {
            hotseatQsbWidget.mIsGoogleColored = !hotseatQsbWidget.mIsGoogleColored;
            hotseatQsbWidget.onChangeListener();
        }
    }

    public static boolean isGoogleColored(Context context) {
        if (Utilities.getOmegaPrefs(context).getDockColoredGoogle()) {
            return true;
        }
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(context).getWallpaperInfo();
        return wallpaperInfo != null && wallpaperInfo.getComponent().flattenToString()
                .equals(context.getString(R.string.default_live_wallpaper));
    }

    public static int getBottomMargin(Launcher launcher, boolean widgetMode) {
        Resources resources = launcher.getResources();
        int minBottom = launcher.getDeviceProfile().getInsets().bottom + launcher.getResources()
                .getDimensionPixelSize(R.dimen.hotseat_qsb_bottom_margin);

        DeviceProfile profile = launcher.getDeviceProfile();
        Rect rect = widgetMode ? new Rect(0, 0, 0, 0) : profile.getInsets();
        Rect hotseatLayoutPadding = profile.getHotseatLayoutPadding();

        int hotseatTop = profile.hotseatBarSizePx + rect.bottom;
        int hotseatIconsTop = hotseatTop - hotseatLayoutPadding.top;

        float f = ((hotseatIconsTop - hotseatLayoutPadding.bottom) + (profile.iconSizePx * 0.92f)) / 2.0f;
        float f2 = ((float) rect.bottom) * 0.67f;
        int bottomMargin = Math.round(f2 + (
                ((((((float) hotseatTop) - f2) - f) - resources
                        .getDimension(R.dimen.qsb_widget_height))
                        - ((float) profile.verticalDragHandleSizePx)) / 2.0f));

        return Math.max(minBottom, bottomMargin);
    }

    public void setWidgetMode(boolean widgetMode) {
        this.widgetMode = widgetMode;
    }

    protected void onAttachedToWindow() {
        dW();
        super.onAttachedToWindow();
        Ds.addListener(this);
        addOrUpdateSearchRipple();
        setOnFocusChangeListener(mLauncher.mFocusHandler);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Ds.removeListener(this);
    }

    @Override
    public void onValueChanged(@NotNull String key, @NotNull OmegaPreferences prefs, boolean force) {
        if (key.equals(KEY_DOCK_COLORED_GOOGLE)) {
            mIsGoogleColored = isGoogleColored();
            onChangeListener();
        } else if (!widgetMode && (key.equals(KEY_DOCK_SEARCHBAR) || key.equals(KEY_DOCK_HIDE))) {
            //boolean visible = prefs.getDockSearchBar() && !prefs.getDockHide();
            //setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected Drawable getIcon() {
        return getIcon(mIsGoogleColored);
    }

    @Override
    protected Drawable getMicIcon() {
        return getMicIcon(mIsGoogleColored);
    }

    public final void onChangeListener() {
        removeAllViews();
        setColors();
        dW();
        dy();
        addOrUpdateSearchRipple();
    }

    private void dW() {
        y(false);
    }

    private void y(boolean z) {
        View findViewById = findViewById(R.id.g_icon);
        if (findViewById != null) {
            findViewById.setAlpha(1.0f);
        }
    }

    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i != 0) {
            y(false);
        }
    }

    private void setColors() {
        View.inflate(new ContextThemeWrapper(getContext(),
                        mIsGoogleColored ? R.style.HotseatQsbTheme_Colored : R.style.HotseatQsbTheme),
                R.layout.qsb_hotseat_content, this);
    }

    public boolean isGoogleColored() {
        return isGoogleColored(getContext());
    }

    private void doOnClick() {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mLauncher);
        if (controller.isGoogle()) {
            startGoogleSearch();
        } else {
            controller.getSearchProvider().startSearch(intent -> {
                mLauncher.openQsb();
                getContext().startActivity(intent, ActivityOptionsCompat.makeClipRevealAnimation(this, 0, 0, getWidth(), getHeight()).toBundle());
                return null;
            });
        }
    }

    private void startGoogleSearch() {
        final ConfigBuilder f = new ConfigBuilder(this, false);
        if (!forceFallbackSearch() && mLauncher.getGoogleNow()
                .startSearch(f.build(), f.getExtras())) {
            SharedPreferences devicePrefs = Utilities.getDevicePrefs(getContext());
            devicePrefs.edit().putInt("key_hotseat_qsb_tap_count",
                    devicePrefs.getInt("key_hotseat_qsb_tap_count", 0) + 1).apply();
            mLauncher.playQsbAnimation();
        } else {
            getContext().sendOrderedBroadcast(getSearchIntent(), null,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (getResultCode() == 0) {
                                fallbackSearch(
                                        "com.google.android.googlequicksearchbox.TEXT_ASSIST");
                            } else {
                                mLauncher.playQsbAnimation();
                            }
                        }
                    }, null, 0, null, null);
        }
    }

    private boolean forceFallbackSearch() {
        return !PackageManagerHelper.isAppEnabled(getContext().getPackageManager(),
                "com.google.android.apps.nexuslauncher", 0);
    }

    @Override
    protected void noGoogleAppSearch() {
        final Intent searchIntent = new Intent("com.google.android.apps.searchlite.WIDGET_ACTION")
                .setComponent(ComponentName.unflattenFromString(
                        "com.google.android.apps.searchlite/.ui.SearchActivity"))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("showKeyboard", true)
                .putExtra("contentType", 12);

        final Context context = getContext();
        final PackageManager pm = context.getPackageManager();

        if (pm.queryIntentActivities(searchIntent, 0).isEmpty()) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")));
                mLauncher.openQsb();
            } catch (ActivityNotFoundException ignored) {
                try {
                    getContext().getPackageManager().getPackageInfo(GOOGLE_QSB, 0);
                    LauncherAppsCompat.getInstance(getContext())
                            .showAppDetailsForProfile(new ComponentName(GOOGLE_QSB, ".SearchActivity"),
                                    Process.myUserHandle(), null, null);
                } catch (PackageManager.NameNotFoundException ignored2) {
                }
            }
        } else {
            mLauncher.openQsb().addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    context.startActivity(searchIntent);
                }
            });
        }
    }

    public final boolean dI() {
        return false;
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setTranslationY((float) (-getBottomMargin(this.mLauncher, widgetMode)));
    }

    public void setInsets(Rect rect) {
        super.setInsets(rect);
        if (!widgetMode) {
            setVisibility(mLauncher.getDeviceProfile().isVerticalBarLayout() ? View.GONE : View.VISIBLE);
        }
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view == this) {
            startSearch("", mResult);
        }
    }

    protected final Intent createSettingsBroadcast() {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mLauncher);
        SearchProvider provider = controller.getSearchProvider();
        if (provider.isBroadcast()) {
            Intent intent = provider.getSettingsIntent();
            List queryBroadcastReceivers = getContext().getPackageManager()
                    .queryBroadcastReceivers(intent, 0);
            if (!(queryBroadcastReceivers == null || queryBroadcastReceivers.isEmpty())) {
                return intent;
            }
        }
        return null;
    }

    @Override
    protected final Intent createSettingsIntent() {
        SearchProviderController controller = SearchProviderController.Companion.getInstance(mLauncher);
        SearchProvider provider = controller.getSearchProvider();
        return provider.isBroadcast() ? null : provider.getSettingsIntent();
    }

    public final void l(String str) {
        startSearch(str, 0);
    }

    private Intent getSearchIntent() {
        int[] array = new int[2];
        getLocationInWindow(array);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        rect.offset(array[0], array[1]);
        rect.inset(getPaddingLeft(), getPaddingTop());
        return ConfigBuilder.getSearchIntent(rect, findViewById(R.id.g_icon), mMicIconView);
    }

    @Override
    public final void startSearch(String str, int i) {
        doOnClick();
    }

    @Nullable
    @Override
    protected String getClipboardText() {
        return null;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        mLauncher.findViewById(R.id.scrim_view).invalidate();
    }

    @Override
    public void onChange() {

    }
}
