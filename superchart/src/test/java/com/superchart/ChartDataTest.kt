package com.superchart

import androidx.compose.ui.graphics.Color
import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry
import com.superchart.data.ChartStyle
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChartEntry and ChartDataset data models.
 */
class ChartDataTest {

    @Test
    fun `ChartEntry creation with valid values`() {
        val entry = ChartEntry("Label", 10f, Color.Red)

        assertEquals("Label", entry.label)
        assertEquals(10f, entry.value, 0.01f)
        assertEquals(Color.Red, entry.color)
    }

    @Test
    fun `ChartEntry creation without color uses null`() {
        val entry = ChartEntry("Label", 10f)

        assertEquals("Label", entry.label)
        assertEquals(10f, entry.value, 0.01f)
        assertNull(entry.color)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ChartEntry with NaN value throws exception`() {
        ChartEntry("Label", Float.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ChartEntry with Infinity value throws exception`() {
        ChartEntry("Label", Float.POSITIVE_INFINITY)
    }

    @Test
    fun `ChartDataset creation with entries`() {
        val entries = listOf(
            ChartEntry("A", 10f),
            ChartEntry("B", 20f),
            ChartEntry("C", 30f)
        )
        val dataset = ChartDataset(entries, "Test Dataset", Color.Blue)

        assertEquals(3, dataset.entries.size)
        assertEquals("Test Dataset", dataset.label)
        assertEquals(Color.Blue, dataset.color)
    }

    @Test
    fun `ChartDataset getColorForEntry returns entry color when available`() {
        val entry = ChartEntry("A", 10f, Color.Red)
        val dataset = ChartDataset(listOf(entry), color = Color.Blue)

        assertEquals(Color.Red, dataset.getColorForEntry(entry))
    }

    @Test
    fun `ChartDataset getColorForEntry returns dataset color when entry color is null`() {
        val entry = ChartEntry("A", 10f, null)
        val dataset = ChartDataset(listOf(entry), color = Color.Blue)

        assertEquals(Color.Blue, dataset.getColorForEntry(entry))
    }

    @Test
    fun `ChartDataset getColorAtIndex returns correct color`() {
        val entries = listOf(
            ChartEntry("A", 10f, Color.Red),
            ChartEntry("B", 20f, null),
            ChartEntry("C", 30f, Color.Green)
        )
        val dataset = ChartDataset(entries, color = Color.Blue)

        assertEquals(Color.Red, dataset.getColorAtIndex(0))
        assertEquals(Color.Blue, dataset.getColorAtIndex(1)) // Falls back to dataset color
        assertEquals(Color.Green, dataset.getColorAtIndex(2))
    }

    @Test
    fun `ChartDataset getColorAtIndex returns dataset color for invalid index`() {
        val entries = listOf(ChartEntry("A", 10f))
        val dataset = ChartDataset(entries, color = Color.Blue)

        assertEquals(Color.Blue, dataset.getColorAtIndex(-1))
        assertEquals(Color.Blue, dataset.getColorAtIndex(10))
    }

    @Test
    fun `ChartDataset maxValue returns maximum value`() {
        val entries = listOf(
            ChartEntry("A", 10f),
            ChartEntry("B", 50f),
            ChartEntry("C", 30f)
        )
        val dataset = ChartDataset(entries)

        assertEquals(50f, dataset.maxValue(), 0.01f)
    }

    @Test
    fun `ChartDataset minValue returns minimum value`() {
        val entries = listOf(
            ChartEntry("A", 10f),
            ChartEntry("B", 50f),
            ChartEntry("C", 30f)
        )
        val dataset = ChartDataset(entries)

        assertEquals(10f, dataset.minValue(), 0.01f)
    }

    @Test
    fun `ChartDataset with empty entries returns 0 for max and min`() {
        val dataset = ChartDataset(emptyList())

        assertEquals(0f, dataset.maxValue(), 0.01f)
        assertEquals(0f, dataset.minValue(), 0.01f)
    }

    @Test
    fun `ChartDataset with single entry`() {
        val entries = listOf(ChartEntry("Single", 42f))
        val dataset = ChartDataset(entries)

        assertEquals(1, dataset.entries.size)
        assertEquals(42f, dataset.maxValue(), 0.01f)
        assertEquals(42f, dataset.minValue(), 0.01f)
    }

    @Test
    fun `ChartDataset with negative values`() {
        val entries = listOf(
            ChartEntry("A", -10f),
            ChartEntry("B", 5f),
            ChartEntry("C", -20f)
        )
        val dataset = ChartDataset(entries)

        assertEquals(5f, dataset.maxValue(), 0.01f)
        assertEquals(-20f, dataset.minValue(), 0.01f)
    }

    @Test
    fun `ChartStyle Line default values`() {
        val style = ChartStyle.Line()

        assertEquals(4f, style.width, 0.01f)
        assertFalse(style.dashed)
        assertFalse(style.smooth)
        assertFalse(style.showPoints)
        assertEquals(6f, style.pointRadius, 0.01f)
    }

    @Test
    fun `ChartStyle Bar default values`() {
        val style = ChartStyle.Bar()

        assertEquals(30f, style.width, 0.01f)
        assertEquals(0f, style.cornerRadius, 0.01f)
        assertEquals(8f, style.spacing, 0.01f)
    }

    @Test
    fun `ChartStyle Pie default values`() {
        val style = ChartStyle.Pie()

        assertFalse(style.donutMode)
        assertEquals(0.5f, style.donutHoleRatio, 0.01f)
        assertTrue(style.showLabels)
        assertEquals(2f, style.sliceSpacing, 0.01f)
    }

    @Test
    fun `ChartStyle Line with custom values`() {
        val style = ChartStyle.Line(
            width = 8f,
            dashed = true,
            smooth = true,
            showPoints = true,
            pointRadius = 10f
        )

        assertEquals(8f, style.width, 0.01f)
        assertTrue(style.dashed)
        assertTrue(style.smooth)
        assertTrue(style.showPoints)
        assertEquals(10f, style.pointRadius, 0.01f)
    }

    @Test
    fun `Multiple datasets with different colors`() {
        val dataset1 = ChartDataset(
            listOf(ChartEntry("A", 10f)),
            label = "Dataset 1",
            color = Color.Red
        )
        val dataset2 = ChartDataset(
            listOf(ChartEntry("B", 20f)),
            label = "Dataset 2",
            color = Color.Blue
        )

        assertNotEquals(dataset1.color, dataset2.color)
        assertEquals("Dataset 1", dataset1.label)
        assertEquals("Dataset 2", dataset2.label)
    }
}

