package com.neoapps.neolauncher.folder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.graphics.withClip
import com.android.launcher3.views.ClipPathView

class FolderRootView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ClipPathView {
    private var mClipPath: Path? = null

    override fun onDraw(canvas: Canvas) {
        if (mClipPath == null) {
            super.onDraw(canvas)
        } else {
            canvas.withClip(mClipPath!!) {
                super.onDraw(canvas)
            }
        }
    }

    override fun setClipPath(clipPath: Path?) {
        mClipPath = clipPath
    }
}