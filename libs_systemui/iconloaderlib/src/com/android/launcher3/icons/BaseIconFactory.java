package com.android.launcher3.icons;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.DITHER_FLAG;
import static android.graphics.Paint.FILTER_BITMAP_FLAG;
import static android.graphics.drawable.AdaptiveIconDrawable.getExtraInsetFraction;
import static com.android.launcher3.icons.BitmapInfo.FLAG_INSTANT;
import static com.android.launcher3.icons.BitmapInfo.FLAG_WORK;
import static com.android.launcher3.icons.IconProvider.ICON_TYPE_DEFAULT;
import static com.android.launcher3.icons.ShadowGenerator.BLUR_FACTOR;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.util.SparseBooleanArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.icons.BitmapInfo.Extender;
import com.android.launcher3.util.FlagOp;
import com.saulhdev.neolauncher.icons.CustomAdaptiveIconDrawable;
import com.saulhdev.neolauncher.icons.ExtendedBitmapDrawable;
import com.saulhdev.neolauncher.icons.IconPreferences;

/**
 * This class will be moved to androidx library. There shouldn't be any dependency outside
 * this package.
 */
public class BaseIconFactory implements AutoCloseable {

    private static final int DEFAULT_WRAPPER_BACKGROUND = Color.WHITE;
    static final boolean ATLEAST_P = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;

    protected static final int BITMAP_GENERATION_MODE_DEFAULT = 0;
    protected static final int BITMAP_GENERATION_MODE_ALPHA = 1;
    protected static final int BITMAP_GENERATION_MODE_WITH_SHADOW = 2;

    private static final float ICON_BADGE_SCALE = 0.444f;

    @NonNull
    private final Rect mOldBounds = new Rect();
    private boolean mBadgeOnLeft = false;
    @NonNull
    private final SparseBooleanArray mIsUserBadged = new SparseBooleanArray();

    @NonNull
    protected final Context mContext;

    @NonNull
    private final Canvas mCanvas;

    @NonNull
    private final PackageManager mPm;

    @NonNull
    private final ColorExtractor mColorExtractor;

    private boolean mDisableColorExtractor;

    protected final int mFillResIconDpi;
    protected final int mIconBitmapSize;

    protected boolean mMonoIconEnabled;

    @Nullable
    private IconNormalizer mNormalizer;

    @Nullable
    private ShadowGenerator mShadowGenerator;

    private final boolean mShapeDetection;

    // Shadow bitmap used as background for theme icons
    private Bitmap mWhiteShadowLayer;

    private Drawable mWrapperIcon;
    private int mWrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND;
    private Bitmap mUserBadgeBitmap;
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final float PLACEHOLDER_TEXT_SIZE = 20f;
    private static int PLACEHOLDER_BACKGROUND_COLOR = Color.rgb(245, 245, 245);

    protected BaseIconFactory(Context context, int fillResIconDpi, int iconBitmapSize,
                              boolean shapeDetection) {
        mContext = context.getApplicationContext();
        mShapeDetection = shapeDetection;
        mFillResIconDpi = fillResIconDpi;
        mIconBitmapSize = iconBitmapSize;

        mPm = mContext.getPackageManager();
        mColorExtractor = new ColorExtractor();

        mCanvas = new Canvas();
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(DITHER_FLAG, FILTER_BITMAP_FLAG));
        clear();
    }

    public BaseIconFactory(Context context, int fillResIconDpi, int iconBitmapSize) {
        this(context, fillResIconDpi, iconBitmapSize, false);
    }

    protected void clear() {
        mWrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND;
        mDisableColorExtractor = false;
        mBadgeOnLeft = false;
    }

    @NonNull
    public ShadowGenerator getShadowGenerator() {
        if (mShadowGenerator == null) {
            mShadowGenerator = new ShadowGenerator(mIconBitmapSize);
        }
        return mShadowGenerator;
    }

    @NonNull
    public IconNormalizer getNormalizer() {
        if (mNormalizer == null) {
            mNormalizer = new IconNormalizer(mContext, mIconBitmapSize, mShapeDetection);
        }
        return mNormalizer;
    }

    @SuppressWarnings("deprecation")
    public BitmapInfo createIconBitmap(Intent.ShortcutIconResource iconRes) {
        try {
            Resources resources = mPm.getResourcesForApplication(iconRes.packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(iconRes.resourceName, null, null);
                // do not stamp old legacy shortcuts as the app may have already forgotten about it
                return createBadgedIconBitmap(
                        resources.getDrawableForDensity(id, mFillResIconDpi),
                        Process.myUserHandle() /* only available on primary user */,
                        false /* do not apply legacy treatment */);
            }
        } catch (Exception e) {
            // Icon not found.
        }
        return null;
    }

    /**
     * Create a placeholder icon using the passed in text.
     *
     * @param placeholder used for foreground element in the icon bitmap
     * @param color       used for the foreground text color
     * @return
     */
    public BitmapInfo createIconBitmap(String placeholder, int color) {
        Bitmap placeholderBitmap = Bitmap.createBitmap(mIconBitmapSize, mIconBitmapSize,
                Bitmap.Config.ARGB_8888);
        mTextPaint.setColor(color);
        Canvas canvas = new Canvas(placeholderBitmap);
        canvas.drawText(placeholder, mIconBitmapSize / 2, mIconBitmapSize * 5 / 8, mTextPaint);
        AdaptiveIconDrawable drawable = new CustomAdaptiveIconDrawable(
                new ColorDrawable(PLACEHOLDER_BACKGROUND_COLOR),
                new BitmapDrawable(mContext.getResources(), placeholderBitmap));
        Bitmap icon = createIconBitmap(drawable, IconNormalizer.ICON_VISIBLE_AREA_FACTOR);
        return BitmapInfo.of(icon, extractColor(icon));
    }

    public BitmapInfo createIconBitmap(Bitmap icon) {
        if (mIconBitmapSize != icon.getWidth() || mIconBitmapSize != icon.getHeight()) {
            icon = createIconBitmap(new BitmapDrawable(mContext.getResources(), icon), 1f);
        }

        return BitmapInfo.of(icon, extractColor(icon));
    }

    public BitmapInfo createShapedIconBitmap(Bitmap icon, UserHandle user) {
        Drawable d = new FixedSizeBitmapDrawable(icon);
        float inset = AdaptiveIconDrawable.getExtraInsetFraction();
        inset = inset / (1 + 2 * inset);
        d = new CustomAdaptiveIconDrawable(new ColorDrawable(Color.BLACK),
                new InsetDrawable(d, inset, inset, inset, inset));
        return createBadgedIconBitmap(d, user, true);
    }

    /**
     * Creates an icon from the bitmap cropped to the current device icon shape
     */
    @NonNull
    public BitmapInfo createShapedIconBitmap(Bitmap icon, IconOptions options) {
        Drawable d = new FixedSizeBitmapDrawable(icon);
        float inset = getExtraInsetFraction();
        inset = inset / (1 + 2 * inset);
        d = new AdaptiveIconDrawable(new ColorDrawable(Color.BLACK),
                new InsetDrawable(d, inset, inset, inset, inset));
        return createBadgedIconBitmap(d, options);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user,
                                             boolean shrinkNonAdaptiveIcons) {
        return createBadgedIconBitmap(icon, user, shrinkNonAdaptiveIcons, false, null);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user,
                                             int iconAppTargetSdk) {
        return createBadgedIconBitmap(icon, user, iconAppTargetSdk, false);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user,
                                             int iconAppTargetSdk, boolean isInstantApp) {
        return createBadgedIconBitmap(icon, user, iconAppTargetSdk, isInstantApp, null);
    }

    public BitmapInfo createBadgedIconBitmap(Drawable icon, UserHandle user,
                                             int iconAppTargetSdk, boolean isInstantApp, float[] scale) {
        boolean shrinkNonAdaptiveIcons = ATLEAST_P || iconAppTargetSdk >= Build.VERSION_CODES.O;
        return createBadgedIconBitmap(icon, user, shrinkNonAdaptiveIcons, isInstantApp, scale);
    }

    public Bitmap createScaledBitmapWithoutShadow(Drawable icon, int iconAppTargetSdk) {
        boolean shrinkNonAdaptiveIcons = ATLEAST_P || iconAppTargetSdk >= Build.VERSION_CODES.O;
        return createScaledBitmapWithoutShadow(icon, shrinkNonAdaptiveIcons);
    }

    /**
     * Creates bitmap using the source drawable and various parameters.
     * The bitmap is visually normalized with other icons and has enough spacing to add shadow.
     *
     * @param icon                   source of the icon
     * @param user                   info can be used for a badge
     * @param shrinkNonAdaptiveIcons {@code true} if non adaptive icons should be treated
     * @param isInstantApp           info can be used for a badge
     * @param scale                  returns the scale result from normalization
     * @return a bitmap suitable for disaplaying as an icon at various system UIs.
     */
    public BitmapInfo createBadgedIconBitmap(@NonNull Drawable icon, UserHandle user,
                                             boolean shrinkNonAdaptiveIcons, boolean isInstantApp, float[] scale) {
        if (scale == null) {
            scale = new float[1];
        }
        icon = normalizeAndWrapToAdaptiveIcon(icon, shrinkNonAdaptiveIcons, null, scale);
        Bitmap bitmap = createIconBitmap(icon, scale[0]);
        if (icon instanceof AdaptiveIconDrawable) {
            mCanvas.setBitmap(bitmap);
            getShadowGenerator().recreateIcon(Bitmap.createBitmap(bitmap), mCanvas);
            mCanvas.setBitmap(null);
        }

        if (isInstantApp) {
            badgeWithDrawable(bitmap, mContext.getDrawable(R.drawable.ic_instant_app_badge));
        }
        if (user != null) {
            BitmapDrawable drawable = new FixedSizeBitmapDrawable(bitmap);
            Drawable badged = mPm.getUserBadgedIcon(drawable, user);
            if (badged instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) badged).getBitmap();
            } else {
                bitmap = createIconBitmap(badged, 1f);
            }
        }
        int color = extractColor(bitmap);
        return icon instanceof BitmapInfo.Extender
                ? ((BitmapInfo.Extender) icon).getExtendedInfo(bitmap, color, this, scale[0], user)
                : BitmapInfo.of(bitmap, color);
    }

    @NonNull
    public BitmapInfo createBadgedIconBitmap(@NonNull Drawable icon) {
        return createBadgedIconBitmap(icon, null);
    }

    /**
     * Creates bitmap using the source drawable and various parameters.
     * The bitmap is visually normalized with other icons and has enough spacing to add shadow.
     *
     * @param icon source of the icon
     * @return a bitmap suitable for disaplaying as an icon at various system UIs.
     */
    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @NonNull
    public BitmapInfo createBadgedIconBitmap(@NonNull Drawable icon,
                                             @Nullable IconOptions options) {
        boolean shrinkNonAdaptiveIcons = options == null || options.mShrinkNonAdaptiveIcons;
        float[] scale = new float[1];
        icon = normalizeAndWrapToAdaptiveIcon(icon, shrinkNonAdaptiveIcons, null, scale);
        Bitmap bitmap = createIconBitmap(icon, scale[0], BITMAP_GENERATION_MODE_WITH_SHADOW);

        int color = extractColor(bitmap);
        BitmapInfo info = BitmapInfo.of(bitmap, color);

        if (icon instanceof BitmapInfo.Extender) {
            info = ((BitmapInfo.Extender) icon).getExtendedInfo(bitmap, color, this, scale[0], Process.myUserHandle());
        } else if (mMonoIconEnabled && IconProvider.ATLEAST_T
                && icon instanceof AdaptiveIconDrawable) {
            Drawable mono = ((AdaptiveIconDrawable) icon).getMonochrome();
            if (mono != null) {
                // Convert mono drawable to bitmap
                Drawable paddedMono = new ClippedMonoDrawable(mono);
                info.setMonoIcon(
                        createIconBitmap(paddedMono, scale[0], BITMAP_GENERATION_MODE_ALPHA), this);
            }
        }
        info = info.withFlags(getBitmapFlagOp(options));
        return info;
    }

    @NonNull
    public FlagOp getBitmapFlagOp(@Nullable IconOptions options) {
        FlagOp op = FlagOp.NO_OP;
        if (options != null) {
            if (options.mIsInstantApp) {
                op = op.addFlag(FLAG_INSTANT);
            }

            if (options.mUserHandle != null) {
                int key = options.mUserHandle.hashCode();
                boolean isBadged;
                int index;
                if ((index = mIsUserBadged.indexOfKey(key)) >= 0) {
                    isBadged = mIsUserBadged.valueAt(index);
                } else {
                    // Check packageManager if the provided user needs a badge
                    NoopDrawable d = new NoopDrawable();
                    isBadged = (d != mPm.getUserBadgedIcon(d, options.mUserHandle));
                    mIsUserBadged.put(key, isBadged);
                }
                op = op.setFlag(FLAG_WORK, isBadged);
            }
        }
        return op;
    }

    /**
     * package private
     */
    @NonNull
    Bitmap getWhiteShadowLayer() {
        if (mWhiteShadowLayer == null) {
            mWhiteShadowLayer = createScaledBitmapWithShadow(
                    new AdaptiveIconDrawable(new ColorDrawable(Color.WHITE), null));
        }
        return mWhiteShadowLayer;
    }

    @NonNull
    public Bitmap createScaledBitmapWithShadow(@NonNull final Drawable d) {
        float scale = getNormalizer().getScale(d, null, null, null);
        Bitmap bitmap = createIconBitmap(d, scale);
        return BitmapRenderer.createHardwareBitmap(bitmap.getWidth(), bitmap.getHeight(),
                canvas -> getShadowGenerator().recreateIcon(bitmap, canvas));
    }

    @NonNull
    public Bitmap createScaledBitmapWithoutShadow(@Nullable Drawable icon) {
        RectF iconBounds = new RectF();
        float[] scale = new float[1];
        icon = normalizeAndWrapToAdaptiveIcon(icon, true, iconBounds, scale);
        return createIconBitmap(icon,
                Math.min(scale[0], ShadowGenerator.getScaleForBounds(iconBounds)));
    }

    public Bitmap getUserBadgeBitmap(UserHandle user) {
        if (mUserBadgeBitmap == null) {
            Bitmap bitmap = Bitmap.createBitmap(
                    mIconBitmapSize, mIconBitmapSize, Bitmap.Config.ARGB_8888);
            Drawable badgedDrawable = mPm.getUserBadgedIcon(
                    new FixedSizeBitmapDrawable(bitmap), user);
            if (badgedDrawable instanceof BitmapDrawable) {
                mUserBadgeBitmap = ((BitmapDrawable) badgedDrawable).getBitmap();
            } else {
                badgedDrawable.setBounds(0, 0, mIconBitmapSize, mIconBitmapSize);
                mUserBadgeBitmap = BitmapRenderer.createSoftwareBitmap(
                        mIconBitmapSize, mIconBitmapSize, badgedDrawable::draw);
            }
        }
        return mUserBadgeBitmap;
    }


    public Bitmap createScaledBitmapWithoutShadow(Drawable icon, boolean shrinkNonAdaptiveIcons) {
        RectF iconBounds = new RectF();
        float[] scale = new float[1];
        icon = normalizeAndWrapToAdaptiveIcon(icon, shrinkNonAdaptiveIcons, iconBounds, scale);
        return createIconBitmap(icon,
                Math.min(scale[0], ShadowGenerator.getScaleForBounds(iconBounds)));
    }

    /**
     * Sets the background color used for wrapped adaptive icon
     */
    public void setWrapperBackgroundColor(final int color) {
        mWrapperBackgroundColor = (Color.alpha(color) < 255) ? DEFAULT_WRAPPER_BACKGROUND : color;
    }

    /**
     * Disables the dominant color extraction for all icons loaded.
     */
    public void disableColorExtraction() {
        mDisableColorExtractor = true;
    }

    @Nullable
    private Drawable normalizeAndWrapToAdaptiveIcon(@NonNull Drawable icon,
                                                    boolean shrinkNonAdaptiveIcons, RectF outIconBounds, float[] outScale) {
        IconPreferences prefs = new IconPreferences(mContext);
        if (shrinkNonAdaptiveIcons) {
            boolean isFromIconPack = ExtendedBitmapDrawable.isFromIconPack(icon);
            shrinkNonAdaptiveIcons = !isFromIconPack && prefs.shouldWrapAdaptive();
        }

        if (icon == null) {
            return null;
        }
        float scale = 1f;

        if (shrinkNonAdaptiveIcons) {
            if (mWrapperIcon == null) {
                Drawable background = new ColorDrawable(mContext.getColor(R.color.legacy_icon_background));
                Drawable foreground = new FixedScaleDrawable();
                mWrapperIcon = new CustomAdaptiveIconDrawable(background, foreground);
            }
            AdaptiveIconDrawable dr = (AdaptiveIconDrawable) mWrapperIcon;
            dr.setBounds(0, 0, 1, 1);
            boolean[] outShape = new boolean[1];
            scale = getNormalizer().getScale(icon, outIconBounds, dr.getIconMask(), outShape);
            if (!(icon instanceof AdaptiveIconDrawable) && !outShape[0]) {
                ThemedIconDrawable.ThemeData themeData = null;
                if (icon instanceof ThemedIconDrawable.ThemedBitmapIcon) {
                    themeData = ((ThemedIconDrawable.ThemedBitmapIcon) icon).mThemeData;
                }

                if (prefs.coloredIconBackground()) {
                    mWrapperBackgroundColor = prefs.getWrapperBackgroundColor(icon);
                }

                FixedScaleDrawable fsd = ((FixedScaleDrawable) dr.getForeground());
                fsd.setDrawable(icon);
                fsd.setScale(scale);
                icon = dr;
                if (themeData != null) {
                    icon = themeData.wrapDrawable(icon, ICON_TYPE_DEFAULT);
                }
                scale = getNormalizer().getScale(icon, outIconBounds, null, null);
                ((ColorDrawable) dr.getBackground()).setColor(mWrapperBackgroundColor);
            }
        } else {
            scale = getNormalizer().getScale(icon, outIconBounds, null, null);
        }

        outScale[0] = scale;
        return icon;
    }

    /**
     * Adds the {@param badge} on top of {@param target} using the badge dimensions.
     */
    public void badgeWithDrawable(Bitmap target, Drawable badge) {
        mCanvas.setBitmap(target);
        badgeWithDrawable(mCanvas, badge);
        mCanvas.setBitmap(null);
    }

    /**
     * Adds the {@param badge} on top of {@param target} using the badge dimensions.
     */
    public void badgeWithDrawable(Canvas target, Drawable badge) {
        int badgeSize = getBadgeSizeForIconSize(mIconBitmapSize);
        if (mBadgeOnLeft) {
            badge.setBounds(0, mIconBitmapSize - badgeSize, badgeSize, mIconBitmapSize);
        } else {
            badge.setBounds(mIconBitmapSize - badgeSize, mIconBitmapSize - badgeSize,
                    mIconBitmapSize, mIconBitmapSize);
        }
        badge.draw(target);
    }

    @NonNull
    protected Bitmap createIconBitmap(@Nullable final Drawable icon, final float scale) {
        return createIconBitmap(icon, scale, BITMAP_GENERATION_MODE_DEFAULT);
    }

    @NonNull
    protected Bitmap createIconBitmap(@Nullable final Drawable icon, final float scale,
                                      final int bitmapGenerationMode) {
        final int size = mIconBitmapSize;

        final Bitmap bitmap;
        switch (bitmapGenerationMode) {
            case BITMAP_GENERATION_MODE_ALPHA:
                bitmap = Bitmap.createBitmap(size, size, Config.ALPHA_8);
                break;
            case BITMAP_GENERATION_MODE_WITH_SHADOW:
            default:
                bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
                break;
        }
        if (icon == null) {
            return bitmap;
        }
        mCanvas.setBitmap(bitmap);
        mOldBounds.set(icon.getBounds());

        if (icon instanceof AdaptiveIconDrawable) {
            int offset = Math.max((int) Math.ceil(BLUR_FACTOR * size),
                    Math.round(size * (1 - scale) / 2));
            // b/211896569: AdaptiveIconDrawable do not work properly for non top-left bounds
            icon.setBounds(0, 0, size - offset - offset, size - offset - offset);
            int count = mCanvas.save();
            mCanvas.translate(offset, offset);
            if (bitmapGenerationMode == BITMAP_GENERATION_MODE_WITH_SHADOW) {
                getShadowGenerator().addPathShadow(
                        ((AdaptiveIconDrawable) icon).getIconMask(), mCanvas);
            }

            if (icon instanceof BitmapInfo.Extender) {
                ((Extender) icon).drawForPersistence(mCanvas);
            } else {
                icon.draw(mCanvas);
            }
            mCanvas.restoreToCount(count);
        } else {
            if (icon instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap b = bitmapDrawable.getBitmap();
                if (b != null && b.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(mContext.getResources().getDisplayMetrics());
                }
            }
            int width = size;
            int height = size;

            int intrinsicWidth = icon.getIntrinsicWidth();
            int intrinsicHeight = icon.getIntrinsicHeight();
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) intrinsicWidth / intrinsicHeight;
                if (intrinsicWidth > intrinsicHeight) {
                    height = (int) (width / ratio);
                } else if (intrinsicHeight > intrinsicWidth) {
                    width = (int) (height * ratio);
                }
            }
            final int left = (size - width) / 2;
            final int top = (size - height) / 2;
            icon.setBounds(left, top, left + width, top + height);
            mCanvas.save();
            mCanvas.scale(scale, scale, size / 2, size / 2);
            icon.draw(mCanvas);
            mCanvas.restore();

        }
        icon.setBounds(mOldBounds);
        mCanvas.setBitmap(null);
        return bitmap;
    }

    @Override
    public void close() {
        clear();
    }


    public BitmapInfo makeDefaultIcon(UserHandle user) {
        return createBadgedIconBitmap(getFullResDefaultActivityIcon(mFillResIconDpi),
                user, Build.VERSION.SDK_INT);
    }

    @NonNull
    public static Drawable getFullResDefaultActivityIcon(final int iconDpi) {
        Drawable icon = Resources.getSystem().getDrawableForDensity(
                android.R.drawable.sym_def_app_icon, iconDpi);
        return CustomAdaptiveIconDrawable.wrapNonNull(icon);
    }

    private int extractColor(@NonNull final Bitmap bitmap) {
        return mDisableColorExtractor ? 0 : mColorExtractor.findDominantColorByHue(bitmap);
    }

    /**
     * Returns the correct badge size given an icon size
     */
    public static int getBadgeSizeForIconSize(final int iconSize) {
        return (int) (ICON_BADGE_SCALE * iconSize);
    }

    public static class IconOptions {

        boolean mShrinkNonAdaptiveIcons = true;

        boolean mIsInstantApp;

        @Nullable
        UserHandle mUserHandle;

        /**
         * Set to false if non-adaptive icons should not be treated
         */
        @NonNull
        public IconOptions setShrinkNonAdaptiveIcons(final boolean shrink) {
            mShrinkNonAdaptiveIcons = shrink;
            return this;
        }

        /**
         * User for this icon, in case of badging
         */
        @NonNull
        public IconOptions setUser(@Nullable final UserHandle user) {
            mUserHandle = user;
            return this;
        }

        /**
         * If this icon represents an instant app
         */
        @NonNull
        public IconOptions setInstantApp(final boolean instantApp) {
            mIsInstantApp = instantApp;
            return this;
        }
    }

    /**
     * An extension of {@link BitmapDrawable} which returns the bitmap pixel size as intrinsic size.
     * This allows the badging to be done based on the action bitmap size rather than
     * the scaled bitmap size.
     */
    private static class FixedSizeBitmapDrawable extends BitmapDrawable {

        public FixedSizeBitmapDrawable(@Nullable final Bitmap bitmap) {
            super(null, bitmap);
        }

        @Override
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }

    private static class NoopDrawable extends ColorDrawable {
        @Override
        public int getIntrinsicHeight() {
            return 1;
        }

        @Override
        public int getIntrinsicWidth() {
            return 1;
        }
    }

    private static class ClippedMonoDrawable extends InsetDrawable {

        @NonNull
        private final AdaptiveIconDrawable mCrop;

        public ClippedMonoDrawable(@Nullable final Drawable base) {
            super(base, -getExtraInsetFraction());
            mCrop = new AdaptiveIconDrawable(new ColorDrawable(Color.BLACK), null);
        }

        @Override
        public void draw(Canvas canvas) {
            mCrop.setBounds(getBounds());
            int saveCount = canvas.save();
            canvas.clipPath(mCrop.getIconMask());
            super.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    private static class CenterTextDrawable extends ColorDrawable {

        @NonNull
        private final Rect mTextBounds = new Rect();

        @NonNull
        private final Paint mTextPaint = new Paint(ANTI_ALIAS_FLAG | FILTER_BITMAP_FLAG);

        @NonNull
        private final String mText;

        CenterTextDrawable(@NonNull final String text, final int color) {
            mText = text;
            mTextPaint.setColor(color);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            mTextPaint.setTextSize(bounds.height() / 3f);
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            canvas.drawText(mText,
                    bounds.exactCenterX() - mTextBounds.exactCenterX(),
                    bounds.exactCenterY() - mTextBounds.exactCenterY(),
                    mTextPaint);
        }
    }
}
