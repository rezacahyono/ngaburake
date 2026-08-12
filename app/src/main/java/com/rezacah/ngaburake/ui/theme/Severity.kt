package com.rezacah.ngaburake.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.rezacah.ngaburake.report.Severity

/**
 * Severity palette matching the HtmlReportFormatter's CSS colors — pastel backgrounds with dark
 * text, so the app's finding cards and summary badges look consistent with the generated HTML
 * report. Not theme-switchable by design (literal palette, like the HTML report).
 */
data class SeverityPalette(
    val background: Color,
    val content: Color,
    val icon: ImageVector,
)

object SeverityTheme {
    val OK = SeverityPalette(
        background = Color(0xFFE6FFE6),
        content = Color(0xFF1B3A1B),
        icon = Icons.Filled.CheckCircle,
    )
    val WARNING = SeverityPalette(
        background = Color(0xFFFFF8E1),
        content = Color(0xFF3A2E12),
        icon = Icons.Filled.WarningAmber,
    )
    val CRITICAL = SeverityPalette(
        background = Color(0xFFFFE6E6),
        content = Color(0xFF3A1616),
        icon = Icons.Filled.ErrorOutline,
    )

    fun of(severity: Severity): SeverityPalette = when (severity) {
        Severity.OK -> OK
        Severity.WARNING -> WARNING
        Severity.CRITICAL -> CRITICAL
    }
}
