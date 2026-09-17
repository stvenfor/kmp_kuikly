package com.example.kuikly.base

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesignScaleTest {
    @Test
    fun factor_matchesFlutterScreenutilOnWidePhone() {
        // Pixel_7_Pro logical width ≈ 548.57 when density 2.625 / 420dpi class
        val scale = DesignScale(pageWidth = 548.57f, designWidth = 375f)
        assertEquals(548.57f / 375f, scale.factor, absoluteTolerance = 0.001f)
        assertTrue(scale.factor > 1.4f && scale.factor < 1.5f)
        assertEquals(48f * scale.factor, scale.raw(48), absoluteTolerance = 0.01f)
    }

    @Test
    fun identity_onDesignWidth() {
        val scale = DesignScale(pageWidth = 375f)
        assertEquals(1f, scale.factor, absoluteTolerance = 0.0001f)
        assertEquals(16f, scale.raw(16), absoluteTolerance = 0.0001f)
    }
}
