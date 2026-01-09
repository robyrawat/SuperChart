package com.superchart.formatter

/**
 * Interface for formatting chart values for display.
 */
interface ValueFormatter {
    fun format(value: Float): String
}

/**
 * Default formatter - shows integer values.
 */
class DefaultFormatter : ValueFormatter {
    override fun format(value: Float): String = value.toInt().toString()
}

/**
 * Percentage formatter - shows values as percentages.
 * Example: 45.5 → "46%"
 */
class PercentFormatter(private val decimals: Int = 0) : ValueFormatter {
    override fun format(value: Float): String {
        return if (decimals == 0) {
            "${value.toInt()}%"
        } else {
            "%.${decimals}f%%".format(value)
        }
    }
}

/**
 * Currency formatter - shows values with currency symbol.
 * Example: 1234.5 → "$1,235"
 */
class CurrencyFormatter(
    private val symbol: String = "$",
    private val showDecimals: Boolean = false,
    private val useCommas: Boolean = true
) : ValueFormatter {
    override fun format(value: Float): String {
        val formatted = if (showDecimals) {
            "%.2f".format(value)
        } else {
            value.toInt().toString()
        }

        val withCommas = if (useCommas) {
            formatted.reversed().chunked(3).joinToString(",").reversed()
        } else {
            formatted
        }

        return "$symbol$withCommas"
    }
}

/**
 * Compact formatter - shows large numbers in K/M/B notation.
 * Example: 1500 → "1.5K", 2000000 → "2M"
 */
class CompactFormatter(private val decimals: Int = 1) : ValueFormatter {
    override fun format(value: Float): String {
        return when {
            value >= 1_000_000_000 -> "%.${decimals}f".format(value / 1_000_000_000) + "B"
            value >= 1_000_000 -> "%.${decimals}f".format(value / 1_000_000) + "M"
            value >= 1_000 -> "%.${decimals}f".format(value / 1_000) + "K"
            else -> value.toInt().toString()
        }
    }
}

/**
 * Decimal formatter - shows values with specified decimal places.
 * Example: 45.6789 → "45.68" (with decimals=2)
 */
class DecimalFormatter(private val decimals: Int = 2) : ValueFormatter {
    override fun format(value: Float): String = "%.${decimals}f".format(value)
}

/**
 * Custom formatter - uses a lambda function.
 */
class CustomFormatter(private val formatFunction: (Float) -> String) : ValueFormatter {
    override fun format(value: Float): String = formatFunction(value)
}

