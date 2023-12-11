package com.android.app.animation

import android.annotation.InterpolatorRes
import android.content.Context
import android.view.animation.AnimationUtils
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@SmallTest
@RunWith(JUnit4::class)
class InterpolatorResourcesTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testResourceInterpolatorsMatchCodeInterpolators() {
        var progress = 0f
        while (progress < +1f) {
            assertEquals(
                InterpolatorsAndroidX.EMPHASIZED.getInterpolation(progress),
                loadInterpolator(R.interpolator.emphasized_interpolator).getInterpolation(progress)
            )
            assertEquals(
                InterpolatorsAndroidX.EMPHASIZED_ACCELERATE.getInterpolation(progress),
                loadInterpolator(R.interpolator.emphasized_accelerate_interpolator)
                    .getInterpolation(progress)
            )
            assertEquals(
                InterpolatorsAndroidX.EMPHASIZED_DECELERATE.getInterpolation(progress),
                loadInterpolator(R.interpolator.emphasized_decelerate_interpolator)
                    .getInterpolation(progress)
            )
            assertEquals(
                InterpolatorsAndroidX.STANDARD.getInterpolation(progress),
                loadInterpolator(R.interpolator.standard_interpolator).getInterpolation(progress)
            )
            assertEquals(
                InterpolatorsAndroidX.STANDARD_ACCELERATE.getInterpolation(progress),
                loadInterpolator(R.interpolator.standard_accelerate_interpolator)
                    .getInterpolation(progress)
            )
            assertEquals(
                InterpolatorsAndroidX.STANDARD_DECELERATE.getInterpolation(progress),
                loadInterpolator(R.interpolator.standard_decelerate_interpolator)
                    .getInterpolation(progress)
            )
            progress += 0.1f
        }
    }

    private fun loadInterpolator(@InterpolatorRes resourceInt: Int) =
        AnimationUtils.loadInterpolator(context, resourceInt)

}
