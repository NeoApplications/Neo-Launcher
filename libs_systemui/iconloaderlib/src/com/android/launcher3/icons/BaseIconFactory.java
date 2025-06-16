package com.android.launcher3.icons;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.DITHER_FLAG;
import static android.graphics.Paint.FILTER_BITMAP_FLAG;
import static android.graphics.drawable.AdaptiveIconDrawable.getExtraInsetFraction;

import static com.android.launcher3.icons.BitmapInfo.FLAG_INSTANT;
import static com.android.launcher3.icons.ShadowGenerator.BLUR_FACTOR;

import static java.lang.annotation.RetentionPolicy.SOURCE;

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
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.UserHandle;
import android.util.SparseArray;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.icons.BitmapInfo.Extender;
import com.android.launcher3.util.FlagOp;
import com.android.launcher3.util.UserIconInfo;

import java.lang.annotation.Retention;

/**
 * This class will be moved to androidx library. There shouldn't be any dependency outside
 * this package.
 */
public class BaseIconFactory implements AutoCloseable {

    private static final int DEFAULT_WRAPPER_BACKGROUND = Color.WHITE;
    private static final float LEGACY_ICON_SCALE = .7f * (1f / (1 + 2 * getExtraInsetFraction()));

    public static final int MODE_DEFAULT = 0;
    public static final int MODE_ALPHA = 1;
    public static final int MODE_WITH_SHADOW = 2;
    public static final int MODE_HARDWARE = 3;
    public static final int MODE_HARDWARE_WITH_SHADOW = 4;

    @Retention(SOURCE)
    @IntDef({MODE_DEFAULT, MODE_ALPHA, MODE_WITH_SHADOW, MODE_HARDWARE_WITH_SHADOW, MODE_HARDWARE})
    @interface BitmapGenerationMode {
    }

    private static final float ICON_BADGE_SCALE = 0.444f;

    @NonNull
    private final Rect mOldBounds = new Rect();

    @NonNull
    private final SparseArray<UserIconInfo> mCachedUserInfo = new SparseArray<>();

    @NonNull
    protected final Context mContext;

    @NonNull
    private final Canvas mCanvas;

    @NonNull
    private final PackageManager mPm;

    @NonNull
    private final ColorExtractor mColorExtractor;

    protected final int mFullResIconDpi;
    protected final int mIconBitmapSize;

    protected IconThemeController mThemeController;

    @Nullable
    private IconNormalizer mNormalizer;

    @Nullable
    private ShadowGenerator mShadowGenerator;

    private final boolean mShapeDetection;

    // Shadow bitmap used as background for theme icons
    private Bitmap mWhiteShadowLayer;

    private Drawable mWrapperIcon;
    private int mWrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND;

    private static int PLACEHOLDER_BACKGROUND_COLOR = Color.rgb(245, 245, 245);

    protected BaseIconFactory(Context context, int fullResIconDpi, int iconBitmapSize,
            boolean shapeDetection) {
        mContext = context.getApplicationContext();
        mShapeDetection = shapeDetection;
        mFullResIconDpi = fullResIconDpi;
        mIconBitmapSize = iconBitmapSize;

        mPm = mContext.getPackageManager();
        mColorExtractor = new ColorExtractor();

        mCanvas = new Canvas();
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(DITHER_FLAG, FILTER_BITMAP_FLAG));
        clear();
    }

    public BaseIconFactory(Context context, int fullResIconDpi, int iconBitmapSize) {
        this(context, fullResIconDpi, iconBitmapSize, false);
    }

    protected void clear() {
        mWrapperBackgroundColor = DEFAULT_WRAPPER_BACKGROUND;
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

    @Nullable
    public IconThemeController getThemeController() {
        return mThemeController;
    }

    public int getFullResIconDpi() {
        return mFullResIconDpi;
    }

    public int getIconBitmapSize() {
        return mIconBitmapSize;
    }

    @SuppressWarnings("deprecation")
    public BitmapInfo createIconBitmap(Intent.ShortcutIconResource iconRes) {
        try {
            Resources resources = mPm.getResourcesForApplication(iconRes.packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(iconRes.resourceName, null, null);
                // do not stamp old legacy shortcuts as the app may have already forgotten about it
                return createBadgedIconBitmap(resources.getDrawableForDensity(id, mFullResIconDpi));
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
     */
    public BitmapInfo createIconBitmap(String placeholder, int color) {
        AdaptiveIconDrawable drawable = new AdaptiveIconDrawable(
                new ColorDrawable(PLACEHOLDER_BACKGROUND_COLOR),
                new CenterTextDrawable(placeholder, color));
        Bitmap icon = createIconBitmap(drawable, IconNormalizer.ICON_VISIBLE_AREA_FACTOR);
        return BitmapInfo.of(icon, color);
    }

    public BitmapInfo createIconBitmap(Bitmap icon) {
        if (mIconBitmapSize != icon.getWidth() || mIconBitmapSize != icon.getHeight()) {
            icon = createIconBitmap(new BitmapDrawable(mContext.getResources(), icon), 1f);
        }

        return BitmapInfo.of(icon, mColorExtractor.findDominantColorByHue(icon));
    }

    /**
     * Creates an icon from the bitmap cropped to the current device icon shape
     */
    @NonNull
    public AdaptiveIconDrawable createShapedAdaptiveIcon(Bitmap iconBitmap) {
        Drawable drawable = new FixedSizeBitmapDrawable(iconBitmap);
        float inset = getExtraInsetFraction();
        inset = inset / (1 + 2 * inset);
        return new AdaptiveIconDrawable(new ColorDrawable(Color.BLACK),
                new InsetDrawable(drawable, inset, inset, inset, inset));
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
     * @return a bitmap suitable for displaying as an icon at various system UIs.
     */
    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @NonNull
    public BitmapInfo createBadgedIconBitmap(@NonNull Drawable icon,
            @Nullable IconOptions options) {
        float[] scale = new float[1];
        Drawable tempIcon = icon;
        if (options != null
                && options.mIsArchived
                && icon instanceof BitmapDrawable bitmapDrawable) {
            // b/358123888
            // Pre-archived apps can have BitmapDrawables without insets.
            // Need to convert to Adaptive Icon with insets to avoid cropping.
            tempIcon = createShapedAdaptiveIcon(bitmapDrawable.getBitmap());
        }
        AdaptiveIconDrawable adaptiveIcon = normalizeAndWrapToAdaptiveIcon(tempIcon, null, scale);
        Bitmap bitmap = createIconBitmap(adaptiveIcon, scale[0],
                options == null ? MODE_WITH_SHADOW : options.mGenerationMode);

        int color = (options != null && options.mExtractedColor != null)
                ? options.mExtractedColor : mColorExtractor.findDominantColorByHue(bitmap);
        BitmapInfo info = BitmapInfo.of(bitmap, color);

        if (adaptiveIcon instanceof BitmapInfo.Extender extender) {
            info = extender.getExtendedInfo(bitmap, color, this, scale[0]);
        } else if (IconProvider.ATLEAST_T && mThemeController != null && adaptiveIcon != null) {
            info.setThemedBitmap(mThemeController.createThemedBitmap(adaptiveIcon, info, this));
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

            UserIconInfo info = options.mUserIconInfo;
            if (info == null && options.mUserHandle != null) {
                info = getUserInfo(options.mUserHandle);
            }
            if (info != null) {
                op = info.applyBitmapInfoFlags(op);
            }
        }
        return op;
    }

    @NonNull
    protected UserIconInfo getUserInfo(@NonNull UserHandle user) {
        int key = user.hashCode();
        UserIconInfo info = mCachedUserInfo.get(key);
        /*
         * We do not have the ability to distinguish between different badged users here.
         * As such all badged users will have the work profile badge applied.
         */
        if (info == null) {
            // Simple check to check if the provided user is work profile or not based on badging
            NoopDrawable d = new NoopDrawable();
            boolean isWork = (d != mPm.getUserBadgedIcon(d, user));
            info = new UserIconInfo(user, isWork ? UserIconInfo.TYPE_WORK : UserIconInfo.TYPE_MAIN);
            mCachedUserInfo.put(key, info);
        }
        return info;
    }

    @NonNull
    public Bitmap getWhiteShadowLayer() {
        if (mWhiteShadowLayer == null) {
            mWhiteShadowLayer = createScaledBitmap(
                    new AdaptiveIconDrawable(new ColorDrawable(Color.WHITE), null),
                    MODE_HARDWARE_WITH_SHADOW);
        }
        return mWhiteShadowLayer;
    }

    @NonNull
    public Bitmap createScaledBitmap(@NonNull Drawable icon, @BitmapGenerationMode int mode) {
        RectF iconBounds = new RectF();
        float[] scale = new float[1];
        icon = normalizeAndWrapToAdaptiveIcon(icon, iconBounds, scale);
        return createIconBitmap(icon,
                Math.min(scale[0], ShadowGenerator.getScaleForBounds(iconBounds)), mode);
    }

    /**
     * Sets the background color used for wrapped adaptive icon
     */
    public void setWrapperBackgroundColor(final int color) {
        mWrapperBackgroundColor = (Color.alpha(color) < 255) ? DEFAULT_WRAPPER_BACKGROUND : color;
    }

    @Nullable
    protected AdaptiveIconDrawable normalizeAndWrapToAdaptiveIcon(@Nullable Drawable icon,
            @Nullable final RectF outIconBounds, @NonNull final float[] outScale) {
        if (icon == null) {
            return null;
        }

        AdaptiveIconDrawable adaptiveIcon;
        float scale;
        adaptiveIcon = wrapToAdaptiveIcon(icon, outIconBounds);
        scale = getNormalizer().getScale(adaptiveIcon, outIconBounds, null, null);
        outScale[0] = scale;
        return adaptiveIcon;
    }

    /**
     * Returns a drawable which draws the original drawable at a fixed scale
     */
    private Drawable createScaledDrawable(@NonNull Drawable main, float scale) {
        float h = main.getIntrinsicHeight();
        float w = main.getIntrinsicWidth();
        float scaleX = scale;
        float scaleY = scale;
        if (h > w && w > 0) {
            scaleX *= w / h;
        } else if (w > h && h > 0) {
            scaleY *= h / w;
        }
        scaleX = (1 - scaleX) / 2;
        scaleY = (1 - scaleY) / 2;
        return new InsetDrawable(main, scaleX, scaleY, scaleX, scaleY);
    }

    /**
     * Wraps the provided icon in an adaptive icon drawable
     */
    public AdaptiveIconDrawable wrapToAdaptiveIcon(@NonNull Drawable icon,
            @Nullable final RectF outIconBounds) {
        if (icon instanceof AdaptiveIconDrawable aid) {
            return aid;
        } else {
            EmptyWrapper foreground = new EmptyWrapper();
            AdaptiveIconDrawable dr = new AdaptiveIconDrawable(
                    new ColorDrawable(mWrapperBackgroundColor), foreground);
            dr.setBounds(0, 0, 1, 1);
            boolean[] outShape = new boolean[1];
            float scale = getNormalizer().getScale(icon, outIconBounds, dr.getIconMask(), outShape);
            if (!outShape[0]) {
                foreground.setDrawable(createScaledDrawable(icon, scale * LEGACY_ICON_SCALE));
            } else {
                foreground.setDrawable(createScaledDrawable(icon, 1 - getExtraInsetFraction()));
            }
            return dr;
        }
    }

    @NonNull
    public Bitmap createIconBitmap(@Nullable final Drawable icon, final float scale) {
        return createIconBitmap(icon, scale, MODE_DEFAULT);
    }

    @NonNull
    public Bitmap createIconBitmap(@Nullable final Drawable icon, final float scale,
            @BitmapGenerationMode int bitmapGenerationMode) {
        final int size = mIconBitmapSize;
        final Bitmap bitmap;
        switch (bitmapGenerationMode) {
            case MODE_ALPHA:
                bitmap = Bitmap.createBitmap(size, size, Config.ALPHA_8);
                break;
            case MODE_HARDWARE:
            case MODE_HARDWARE_WITH_SHADOW: {
                return BitmapRenderer.createHardwareBitmap(size, size, canvas ->
                        drawIconBitmap(canvas, icon, scale, bitmapGenerationMode, null));
            }
            case MODE_WITH_SHADOW:
            default:
                bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
                break;
        }
        if (icon == null) {
            return bitmap;
        }
        mCanvas.setBitmap(bitmap);
        drawIconBitmap(mCanvas, icon, scale, bitmapGenerationMode, bitmap);
        mCanvas.setBitmap(null);
        return bitmap;
    }

    private void drawIconBitmap(@NonNull Canvas canvas, @Nullable final Drawable icon,
            final float scale, @BitmapGenerationMode int bitmapGenerationMode,
            @Nullable Bitmap targetBitmap) {
        final int size = mIconBitmapSize;
        mOldBounds.set(icon.getBounds());

        if (icon instanceof AdaptiveIconDrawable) {
            // We are ignoring KEY_SHADOW_DISTANCE because regular icons ignore this at the
            // moment b/298203449
            int offset = Math.max((int) Math.ceil(BLUR_FACTOR * size),
                    Math.round(size * (1 - scale) / 2));
            // b/211896569: AdaptiveIconDrawable do not work properly for non top-left bounds
            icon.setBounds(0, 0, size - offset - offset, size - offset - offset);
            int count = canvas.save();
            canvas.translate(offset, offset);
            if (bitmapGenerationMode == MODE_WITH_SHADOW
                    || bitmapGenerationMode == MODE_HARDWARE_WITH_SHADOW) {
                getShadowGenerator().addPathShadow(
                        ((AdaptiveIconDrawable) icon).getIconMask(), canvas);
            }

            if (icon instanceof BitmapInfo.Extender) {
                ((Extender) icon).drawForPersistence(canvas);
            } else {
                icon.draw(canvas);
            }
            canvas.restoreToCount(count);
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

            canvas.save();
            canvas.scale(scale, scale, size / 2, size / 2);
            icon.draw(canvas);
            canvas.restore();

            if (bitmapGenerationMode == MODE_WITH_SHADOW && targetBitmap != null) {
                // Shadow extraction only works in software mode
                getShadowGenerator().drawShadow(targetBitmap, canvas);

                // Draw the icon again on top:
                canvas.save();
                canvas.scale(scale, scale, size / 2, size / 2);
                icon.draw(canvas);
                canvas.restore();
            }
        }
        icon.setBounds(mOldBounds);
    }

    @Override
    public void close() {
        clear();
    }

    @NonNull
    public BitmapInfo makeDefaultIcon(IconProvider iconProvider) {
        return createBadgedIconBitmap(iconProvider.getFullResDefaultActivityIcon(mFullResIconDpi));
    }

    /**
     * Returns the correct badge size given an icon size
     */
    public static int getBadgeSizeForIconSize(final int iconSize) {
        return (int) (ICON_BADGE_SCALE * iconSize);
    }

    public static class IconOptions {

        boolean mIsInstantApp;

        boolean mIsArchived;

        @BitmapGenerationMode
        int mGenerationMode = MODE_WITH_SHADOW;

        @Nullable
        UserHandle mUserHandle;
        @Nullable
        UserIconInfo mUserIconInfo;

        @ColorInt
        @Nullable
        Integer mExtractedColor;


        /**
         * User for this icon, in case of badging
         */
        @NonNull
        public IconOptions setUser(@Nullable final UserHandle user) {
            mUserHandle = user;
            return this;
        }

        /**
         * User for this icon, in case of badging
         */
        @NonNull
        public IconOptions setUser(@Nullable final UserIconInfo user) {
            mUserIconInfo = user;
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

        /**
         * If the icon represents an archived app
         */
        public IconOptions setIsArchived(boolean isArchived) {
            mIsArchived = isArchived;
            return this;
        }

        /**
         * Disables auto color extraction and overrides the color to the provided value
         */
        @NonNull
        public IconOptions setExtractedColor(@ColorInt int color) {
            mExtractedColor = color;
            return this;
        }

        /**
         * Sets the bitmap generation mode to use for the bitmap info. Note that some generation
         * modes do not support color extraction, so consider setting a extracted color manually
         * in those cases.
         */
        public IconOptions setBitmapGenerationMode(@BitmapGenerationMode int generationMode) {
            mGenerationMode = generationMode;
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

    private static class EmptyWrapper extends DrawableWrapper {

        EmptyWrapper() {
            super(new ColorDrawable());
        }

        @Override
        public ConstantState getConstantState() {
            Drawable d = getDrawable();
            return d == null ? null : d.getConstantState();
        }
    }
}
