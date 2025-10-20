package com.google.android.systemui.smartspace;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.systemui.smartspace.logging.BcSmartspaceCardLoggingInfo;
import com.google.android.systemui.smartspace.plugin.BcSmartspaceDataPlugin;
import com.saulhdev.smartspace.SmartspaceTarget;

public class BcSmartspaceCardGenericImage extends BcSmartspaceCardSecondary {
    public ImageView mImageView;

    public BcSmartspaceCardGenericImage(Context context) {
        super(context);
    }

    public BcSmartspaceCardGenericImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = findViewById(R.id.image_view);
    }

    public void resetUi() {
        mImageView.setImageBitmap(null);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean setSmartspaceActions(
            SmartspaceTarget target,
            BcSmartspaceDataPlugin.SmartspaceEventNotifier eventNotifier,
            BcSmartspaceCardLoggingInfo loggingInfo) {
        Bundle extras = target.getBaseAction() != null ? target.getBaseAction().getExtras() : null;
        if (extras != null && extras.containsKey("imageBitmap")) {
            if (extras.containsKey("imageScaleType")) {
                String scaleType = extras.getString("imageScaleType");
                try {
                    mImageView.setScaleType(ImageView.ScaleType.valueOf(scaleType));
                } catch (IllegalArgumentException e) {
                    Log.w("SmartspaceGenericImg", "Invalid imageScaleType value: " + scaleType);
                }
            }

            String dimensionRatio = BcSmartSpaceUtil.getDimensionRatio(extras);
            if (dimensionRatio != null) {
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) mImageView.getLayoutParams();
                params.dimensionRatio = dimensionRatio;
            }

            if (extras.containsKey("imageLayoutWidth")) {
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) mImageView.getLayoutParams();
                params.width = extras.getInt("imageLayoutWidth");
            }

            if (extras.containsKey("imageLayoutHeight")) {
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) mImageView.getLayoutParams();
                params.height = extras.getInt("imageLayoutHeight");
            }

            Bitmap bitmap = (Bitmap) extras.get("imageBitmap");
            setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    @Override
    public void setTextColor(int color) {
        // No-op
    }
}
