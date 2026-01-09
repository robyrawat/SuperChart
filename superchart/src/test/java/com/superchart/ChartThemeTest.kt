package com.superchart

import androidx.compose.ui.graphics.Color
import com.superchart.theme.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChartTheme configuration.
 */
class ChartThemeTest {

    @Test
    fun `ChartTheme default colors are initialized`() {
        assertNotNull(ChartTheme.colors)
        assertNotNull(ChartTheme.colors.primary)
        assertNotNull(ChartTheme.colors.background)
    }

    @Test
    fun `ChartTheme default palette has 10 colors`() {
        assertEquals(10, ChartTheme.defaultPalette.size)
    }

    @Test
    fun `ChartTheme getColorForIndex wraps around`() {
        val color0 = ChartTheme.getColorForIndex(0)
        val color10 = ChartTheme.getColorForIndex(10)
        val color20 = ChartTheme.getColorForIndex(20)

        assertEquals(color0, color10)
        assertEquals(color0, color20)
    }

    @Test
    fun `ChartTheme getColorForIndex works with negative indices`() {
        // Should not crash with negative indices
        val color = ChartTheme.getColorForIndex(-1)
        assertNotNull(color)
    }

    @Test
    fun `ChartColors default values are set`() {
        val colors = ChartColors()

        assertNotNull(colors.primary)
        assertNotNull(colors.secondary)
        assertNotNull(colors.tertiary)
        assertNotNull(colors.background)
        assertNotNull(colors.grid)
        assertNotNull(colors.text)
        assertNotNull(colors.axis)
        assertNotNull(colors.tooltip)
    }

    @Test
    fun `ChartColors custom values can be set`() {
        val customColors = ChartColors(
            primary = Color.Red,
            background = Color.Black
        )

        assertEquals(Color.Red, customColors.primary)
        assertEquals(Color.Black, customColors.background)
    }

    @Test
    fun `Light and Dark color schemes are different`() {
        val light = ChartTheme.lightColors
        val dark = ChartTheme.darkColors

        assertNotEquals(light.background, dark.background)
        assertNotEquals(light.text, dark.text)
    }

    @Test
    fun `AxisConfig default values`() {
        val config = AxisConfig()

        assertTrue(config.showXAxis)
        assertTrue(config.showYAxis)
        assertTrue(config.showGridLines)
        assertTrue(config.showXAxisLabels)
        assertTrue(config.showYAxisLabels)
        assertEquals(5, config.yAxisSteps)
        assertTrue(config.xAxisLabels.isEmpty())
    }

    @Test
    fun `AxisConfig custom values`() {
        val config = AxisConfig(
            showXAxis = false,
            showYAxis = false,
            yAxisSteps = 10,
            xAxisLabels = listOf("A", "B", "C")
        )

        assertFalse(config.showXAxis)
        assertFalse(config.showYAxis)
        assertEquals(10, config.yAxisSteps)
        assertEquals(3, config.xAxisLabels.size)
    }

    @Test
    fun `AnimationConfig default values`() {
        val config = AnimationConfig()

        assertTrue(config.enabled)
        assertEquals(600, config.durationMs)
        assertEquals(0, config.delayMs)
        assertEquals(50, config.staggerDelayMs)
    }

    @Test
    fun `AnimationConfig custom values`() {
        val config = AnimationConfig(
            enabled = false,
            durationMs = 1000,
            delayMs = 200,
            staggerDelayMs = 100
        )

        assertFalse(config.enabled)
        assertEquals(1000, config.durationMs)
        assertEquals(200, config.delayMs)
        assertEquals(100, config.staggerDelayMs)
    }

    @Test
    fun `ChartDimensions default values`() {
        val dimensions = ChartDimensions()

        assertEquals(4f, dimensions.defaultLineWidth, 0.01f)
        assertEquals(30f, dimensions.defaultBarWidth, 0.01f)
        assertEquals(6f, dimensions.defaultPointRadius, 0.01f)
        assertEquals(1f, dimensions.gridLineWidth, 0.01f)
        assertEquals(2f, dimensions.axisLineWidth, 0.01f)
    }

    @Test
    fun `LegendOrientation enum values`() {
        val horizontal = LegendOrientation.HORIZONTAL
        val vertical = LegendOrientation.VERTICAL

        assertNotEquals(horizontal, vertical)
    }

    @Test
    fun `LegendPosition enum has all values`() {
        val positions = LegendPosition.values()

        assertTrue(positions.contains(LegendPosition.TOP))
        assertTrue(positions.contains(LegendPosition.BOTTOM))
        assertTrue(positions.contains(LegendPosition.LEFT))
        assertTrue(positions.contains(LegendPosition.RIGHT))
        assertTrue(positions.contains(LegendPosition.NONE))
        assertEquals(5, positions.size)
    }

    @Test
    fun `ChartTheme typography is initialized`() {
        assertNotNull(ChartTheme.typography)
        assertNotNull(ChartTheme.typography.labelTextSize)
        assertNotNull(ChartTheme.typography.titleTextSize)
    }

    @Test
    fun `ChartTheme dimensions is initialized`() {
        assertNotNull(ChartTheme.dimensions)
        assertTrue(ChartTheme.dimensions.defaultLineWidth > 0)
        assertTrue(ChartTheme.dimensions.defaultBarWidth > 0)
    }

    @Test
    fun `ChartTheme axisConfig is initialized`() {
        assertNotNull(ChartTheme.axisConfig)
        assertTrue(ChartTheme.axisConfig.yAxisSteps > 0)
    }

    @Test
    fun `ChartTheme animationConfig is initialized`() {
        assertNotNull(ChartTheme.animationConfig)
        assertTrue(ChartTheme.animationConfig.durationMs > 0)
    }
}

