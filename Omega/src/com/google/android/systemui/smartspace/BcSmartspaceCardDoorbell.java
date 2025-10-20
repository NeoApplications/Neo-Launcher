package com.google.android.systemui.smartspace;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.android.internal.util.LatencyTracker;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.plugin.BcSmartspaceDataPlugin;
import com.saulhdev.smartspace.SmartspaceAction;
import com.saulhdev.smartspace.SmartspaceTarget;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BcSmartspaceCardDoorbell extends BcSmartspaceCardGenericImage {
    public static final String TAG = "BcSmartspaceCardBell";
    public int mGifFrameDurationInMs = 200;
    public final LatencyInstrumentContext mLatencyInstrumentContext;
    public ImageView mLoadingIcon;
    public ViewGroup mLoadingScreenView;
    public String mPreviousTargetId;
    public ProgressBar mProgressBar;
    public final Map<Uri, DrawableWithUri> mUriToDrawable = new HashMap<>();

    public BcSmartspaceCardDoorbell(Context context) {
        this(context, null);
    }

    public BcSmartspaceCardDoorbell(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLatencyInstrumentContext = new LatencyInstrumentContext(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLoadingScreenView = findViewById(R.id.loading_screen);
        mProgressBar = findViewById(R.id.indeterminateBar);
        mLoadingIcon = findViewById(R.id.loading_screen_icon);
    }

    @Override
    public void resetUi() {
        super.resetUi();
        BcSmartspaceTemplateDataUtils.updateVisibility(mImageView, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mLoadingScreenView, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mProgressBar, View.GONE);
        BcSmartspaceTemplateDataUtils.updateVisibility(mLoadingIcon, View.GONE);
    }

    public void maybeResetImageView(SmartspaceTarget target) {
        String targetId = target.getSmartspaceTargetId();
        boolean sameTarget = targetId.equals(mPreviousTargetId);
        mPreviousTargetId = targetId;

        if (!sameTarget) {
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            mImageView.setImageDrawable(null);
            mUriToDrawable.clear();
        }
    }

    public void maybeUpdateLayoutHeight(Bundle extras, View view, String key) {
        if (extras.containsKey(key)) {
            Resources res = getContext().getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            float density = dm.density;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = (int) (extras.getInt(key) * density);
        }
    }

    public void maybeUpdateLayoutWidth(Bundle extras, View view, String key) {
        if (extras.containsKey(key)) {
            Resources res = getContext().getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            float density = dm.density;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (extras.getInt(key) * density);
        }
    }

    public boolean setSmartspaceActions(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier notifier,
            BcSmartspaceCardLoggingInfo loggingInfo) {
        if (!getContext().getPackageName().equals("com.android.systemui")) {
            return false;
        }

        SmartspaceAction baseAction = target.getBaseAction();
        Bundle extras = baseAction != null ? baseAction.getExtras() : null;

        List<String> imageUris =
                target.getIconGrid().stream()
                        .filter(action -> action.getExtras().containsKey("imageUri"))
                        .map(action -> action.getExtras().getString("imageUri"))
                        .map(Uri::parse)
                        .map(Uri::toString)
                        .collect(Collectors.toList());

        if (!imageUris.isEmpty()) {
            if (extras != null && extras.containsKey("frameDurationMs")) {
                mGifFrameDurationInMs = extras.getInt("frameDurationMs");
            }

            List<Uri> uris = imageUris.stream().map(Uri::parse).collect(Collectors.toList());
            Set<Uri> uriSet =
                    uris.stream()
                            .filter(uri -> !mUriToDrawable.containsKey(uri))
                            .collect(Collectors.toSet());

            if (!uriSet.isEmpty()) {
                mLatencyInstrumentContext.mUriSet.addAll(uriSet);
                if (!mLatencyInstrumentContext.mUriSet.isEmpty()) {
                    mLatencyInstrumentContext.mLatencyTracker.onActionStart(22);
                }
            }

            maybeResetImageView(target);
            BcSmartspaceTemplateDataUtils.updateVisibility(mImageView, View.VISIBLE);

            ContentResolver contentResolver =
                    getContext().getApplicationContext().getContentResolver();
            int height =
                    getResources().getDimensionPixelSize(R.dimen.enhanced_smartspace_card_height);
            float cornerRadius =
                    getResources()
                            .getDimension(R.dimen.enhanced_smartspace_secondary_card_corner_radius);

            WeakReference<ImageView> imageViewRef = new WeakReference<>(mImageView);
            WeakReference<ViewGroup> loadingScreenRef = new WeakReference<>(mLoadingScreenView);

            List<DrawableWithUri> drawables =
                    uris.stream()
                            .map(
                                    uri -> {
                                        DrawableWithUri drawable =
                                                mUriToDrawable.computeIfAbsent(
                                                        uri,
                                                        k ->
                                                                new DrawableWithUri(
                                                                        uri,
                                                                        contentResolver,
                                                                        height,
                                                                        cornerRadius,
                                                                        imageViewRef,
                                                                        loadingScreenRef));
                                        new LoadUriTask(mLatencyInstrumentContext)
                                                .execute(drawable);
                                        return drawable;
                                    })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            AnimationDrawable animation = new AnimationDrawable();
            for (DrawableWithUri drawable : drawables) {
                animation.addFrame(drawable, mGifFrameDurationInMs);
            }
            mImageView.setImageDrawable(animation);
            animation.start();
            Log.d(TAG, "imageUri is set");
            return true;
        } else if (extras != null && extras.containsKey("imageBitmap")) {
            Bitmap bitmap = (Bitmap) extras.get("imageBitmap");
            maybeResetImageView(target);
            BcSmartspaceTemplateDataUtils.updateVisibility(mImageView, View.VISIBLE);

            if (bitmap != null && bitmap.getHeight() != 0) {
                int height =
                        (int) getResources().getDimension(R.dimen.enhanced_smartspace_card_height);
                float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
                int width = (int) (height * aspectRatio);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            Resources res = getResources();
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(res, bitmap);
            drawable.setCornerRadius(
                    res.getDimension(R.dimen.enhanced_smartspace_secondary_card_corner_radius));
            mImageView.setImageDrawable(drawable);
            Log.d(TAG, "imageBitmap is set");
            return true;
        } else if (extras != null && extras.containsKey("loadingScreenState")) {
            int state = extras.getInt("loadingScreenState");
            String dimensionRatio = BcSmartSpaceUtil.getDimensionRatio(extras);
            if (dimensionRatio == null) {
                return false;
            }

            maybeResetImageView(target);
            BcSmartspaceTemplateDataUtils.updateVisibility(mImageView, View.GONE);

            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) mLoadingScreenView.getLayoutParams();
            params.dimensionRatio = dimensionRatio;
            mLoadingScreenView.setBackgroundTintList(
                    ColorStateList.valueOf(
                            getContext().getColor(R.color.smartspace_button_background)));
            BcSmartspaceTemplateDataUtils.updateVisibility(mLoadingScreenView, View.VISIBLE);

            maybeUpdateLayoutWidth(extras, mProgressBar, "progressBarWidth");
            maybeUpdateLayoutHeight(extras, mProgressBar, "progressBarHeight");
            mProgressBar.setIndeterminateTintList(
                    ColorStateList.valueOf(getContext().getColor(R.color.smartspace_button_text)));

            boolean progressBarVisible =
                    state == 1 || (state == 4 && extras.getBoolean("progressBarVisible", true));
            BcSmartspaceTemplateDataUtils.updateVisibility(
                    mProgressBar, progressBarVisible ? View.VISIBLE : View.GONE);

            boolean iconVisible = false;
            if (state == 2) {
                mLoadingIcon.setImageDrawable(getContext().getDrawable(R.drawable.videocam));
                iconVisible = true;
            } else if (state == 3) {
                mLoadingIcon.setImageDrawable(getContext().getDrawable(R.drawable.videocam_off));
                iconVisible = true;
            } else if (state == 4 && extras.containsKey("loadingScreenIcon")) {
                Bitmap iconBitmap = (Bitmap) extras.get("loadingScreenIcon");
                mLoadingIcon.setImageBitmap(iconBitmap);
                if (extras.getBoolean("tintLoadingIcon", false)) {
                    mLoadingIcon.setColorFilter(
                            getContext().getColor(R.color.smartspace_button_text));
                }
                iconVisible = true;
            }

            maybeUpdateLayoutWidth(extras, mLoadingIcon, "loadingIconWidth");
            maybeUpdateLayoutHeight(extras, mLoadingIcon, "loadingIconHeight");
            BcSmartspaceTemplateDataUtils.updateVisibility(
                    mLoadingIcon, iconVisible ? View.VISIBLE : View.GONE);
            return true;
        }
        return false;
    }

    public static class DrawableWithUri extends DrawableWrapper {
        public final Path mClipPath = new Path();
        public final ContentResolver mContentResolver;
        public Drawable mDrawable;
        public final int mHeightInPx;
        public final WeakReference<ImageView> mImageViewWeakReference;
        public final WeakReference<ViewGroup> mLoadingScreenWeakReference;
        public final float mScaledCornerRadius;
        public final RectF mTempRect = new RectF();
        public final Uri mUri;

        public DrawableWithUri(
                Uri uri,
                ContentResolver contentResolver,
                int heightInPx,
                float scaledCornerRadius,
                WeakReference<ImageView> imageViewRef,
                WeakReference<ViewGroup> loadingScreenRef) {
            super(new ColorDrawable(0));
            mUri = uri;
            mContentResolver = contentResolver;
            mHeightInPx = heightInPx;
            mScaledCornerRadius = scaledCornerRadius;
            mImageViewWeakReference = imageViewRef;
            mLoadingScreenWeakReference = loadingScreenRef;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.clipPath(mClipPath);
            super.draw(canvas);
            canvas.restore();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            mTempRect.set(bounds);
            mClipPath.reset();
            mClipPath.addRoundRect(
                    mTempRect, mScaledCornerRadius, mScaledCornerRadius, Path.Direction.CCW);
            super.onBoundsChange(bounds);
        }
    }

    public static class LatencyInstrumentContext {
        public final LatencyTracker mLatencyTracker;
        public final Set<Uri> mUriSet = new HashSet<>();

        public LatencyInstrumentContext(Context context) {
            mLatencyTracker = LatencyTracker.getInstance(context);
        }

        public void cancelInstrument() {
            if (!mUriSet.isEmpty()) {
                mLatencyTracker.onActionCancel(22);
                mUriSet.clear();
            }
        }
    }

    public static class LoadUriTask extends AsyncTask<DrawableWithUri, Void, DrawableWithUri> {
        public final LatencyInstrumentContext mInstrumentContext;

        public LoadUriTask(LatencyInstrumentContext context) {
            mInstrumentContext = context;
        }

        @Override
        protected DrawableWithUri doInBackground(DrawableWithUri... params) {
            if (params.length == 0) return null;
            DrawableWithUri drawable = params[0];
            try (InputStream inputStream =
                    drawable.mContentResolver.openInputStream(drawable.mUri)) {
                android.graphics.ImageDecoder.Source source =
                        android.graphics.ImageDecoder.createSource(null, inputStream);
                drawable.mDrawable =
                        android.graphics.ImageDecoder.decodeDrawable(
                                source,
                                (decoder, info, src) -> {
                                    decoder.setAllocator(
                                            android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE);
                                    int height = drawable.mHeightInPx;
                                    float aspectRatio =
                                            info.getSize().getHeight() != 0
                                                    ? (float) info.getSize().getWidth()
                                                            / info.getSize().getHeight()
                                                    : 0;
                                    int width = (int) (height * aspectRatio);
                                    decoder.setTargetSize(width, height);
                                });
            } catch (IOException e) {
                Log.e(TAG, "Unable to decode stream: " + e);
            } catch (Exception e) {
                Log.w(TAG, "open uri:" + drawable.mUri + " got exception:" + e);
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(DrawableWithUri result) {
            if (result == null) return;
            if (result.mDrawable != null) {
                result.setDrawable(result.mDrawable);
                ImageView imageView = result.mImageViewWeakReference.get();
                if (imageView != null) {
                    int intrinsicWidth = result.mDrawable.getIntrinsicWidth();
                    if (imageView.getLayoutParams().width != intrinsicWidth) {
                        Log.d(TAG, "imageView requestLayout " + result.mUri);
                        imageView.getLayoutParams().width = intrinsicWidth;
                        imageView.requestLayout();
                    }
                }
                if (result.mUri != null
                        && mInstrumentContext.mUriSet.remove(result.mUri)
                        && mInstrumentContext.mUriSet.isEmpty()) {
                    mInstrumentContext.mLatencyTracker.onActionEnd(22);
                } else if (result.mUri == null) {
                    mInstrumentContext.cancelInstrument();
                }
            } else {
                ImageView imageView = result.mImageViewWeakReference.get();
                if (imageView != null) {
                    BcSmartspaceTemplateDataUtils.updateVisibility(imageView, View.GONE);
                }
                mInstrumentContext.cancelInstrument();
            }
            View loadingScreen = result.mLoadingScreenWeakReference.get();
            if (loadingScreen != null) {
                BcSmartspaceTemplateDataUtils.updateVisibility(loadingScreen, View.GONE);
            }
        }

        @Override
        protected void onCancelled() {
            mInstrumentContext.cancelInstrument();
        }
    }
}
