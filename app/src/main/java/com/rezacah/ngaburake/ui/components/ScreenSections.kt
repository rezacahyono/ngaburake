package com.rezacah.ngaburake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rezacah.ngaburake.presentation.UiState
import com.rezacah.ngaburake.report.Severity
import com.rezacah.ngaburake.runtime.ObfuscationResult
import com.rezacah.ngaburake.ui.theme.SeverityTheme

@Composable
fun ScreenHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Runtime Obfuscation Check", style = MaterialTheme.typography.headlineSmall)
        Text(
            "The Gradle plugin verifies at build time in CI; this screen runs the same checks " +
                "at runtime via ObfuscationSDK.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SectionTitle(text: String, topPadding: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = if (topPadding) Modifier.padding(top = 4.dp) else Modifier,
    )
}

@Composable
fun VerificationLoadingIndicator() {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

/** Empty state before the first run — invites the user to verify. */
@Composable
fun EmptyStateHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Run verification to check every configured class against the SDK's checkers.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun ErrorStatus(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SeverityTheme.CRITICAL.background),
    ) {
        Text(
            "Error: $message",
            modifier = Modifier.padding(12.dp),
            color = SeverityTheme.CRITICAL.content,
        )
    }
}

@Composable
fun ResultSummary(result: ObfuscationResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val critical = result.findings.count { it.severity == Severity.CRITICAL }
            val warning = result.findings.count { it.severity == Severity.WARNING }
            val ok = result.findings.count { it.severity == Severity.OK }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("OK $ok") },
                    leadingIcon = {
                        Icon(
                            SeverityTheme.OK.icon,
                            contentDescription = null,
                            tint = SeverityTheme.OK.content,
                        )
                    },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = SeverityTheme.OK.background,
                        labelColor = SeverityTheme.OK.content,
                        leadingIconContentColor = SeverityTheme.OK.content,
                    ),
                )
                AssistChip(
                    onClick = {},
                    label = { Text("WARN $warning") },
                    leadingIcon = {
                        Icon(
                            SeverityTheme.WARNING.icon,
                            contentDescription = null,
                            tint = SeverityTheme.WARNING.content,
                        )
                    },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = SeverityTheme.WARNING.background,
                        labelColor = SeverityTheme.WARNING.content,
                        leadingIconContentColor = SeverityTheme.WARNING.content,
                    ),
                )
                AssistChip(
                    onClick = {},
                    label = { Text("CRIT $critical") },
                    leadingIcon = {
                        Icon(
                            SeverityTheme.CRITICAL.icon,
                            contentDescription = null,
                            tint = SeverityTheme.CRITICAL.content,
                        )
                    },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = SeverityTheme.CRITICAL.background,
                        labelColor = SeverityTheme.CRITICAL.content,
                        leadingIconContentColor = SeverityTheme.CRITICAL.content,
                    ),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (result.isObfuscated) Icons.Filled.VerifiedUser else SeverityTheme.CRITICAL.icon,
                    contentDescription = null,
                    tint = if (result.isObfuscated) MaterialTheme.colorScheme.primary else SeverityTheme.CRITICAL.content,
                )
                Text(
                    if (result.isObfuscated) "All classes verified obfuscated" else "Violations found",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun LoadingRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
        Text("Verifying…", style = MaterialTheme.typography.bodyMedium)
    }
}
