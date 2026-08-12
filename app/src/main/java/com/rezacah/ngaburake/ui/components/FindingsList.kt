package com.rezacah.ngaburake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity
import com.rezacah.ngaburake.runtime.ObfuscationResult
import com.rezacah.ngaburake.ui.theme.SeverityTheme

private fun FindingType.label(): String = when (this) {
    FindingType.CLASS_NAME -> "Class"
    FindingType.FIELD_NAME -> "Field"
    FindingType.METHOD_NAME -> "Method"
}

/** A color-coded finding card with a severity-leading icon. */
@Composable
fun FindingCard(finding: Finding) {
    val palette = SeverityTheme.of(finding.severity)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = palette.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                palette.icon,
                contentDescription = null,
                tint = palette.content,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${finding.severity} · ${finding.type.label()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.content,
                )
                Text(finding.target, style = MaterialTheme.typography.bodyMedium, color = palette.content)
                Text(finding.detail, style = MaterialTheme.typography.bodySmall, color = palette.content)
            }
        }
    }
}

/** Grouped list of all findings — used as direct LazyColumn children. */
@Composable
fun FindingsList(result: ObfuscationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        result.findings.forEach { finding ->
            FindingCard(finding)
        }
    }
}
