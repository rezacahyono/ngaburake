package com.rezacah.ngaburake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Toggle options (mapping cross-check + custom keywords) presented as labelled switches in a
 * single card — clearer on/off semantics than chips.
 */
@Composable
fun VerificationOptions(
    useMappingFile: Boolean,
    onToggleMappingFile: (Boolean) -> Unit,
    customKeywords: Boolean,
    onToggleCustomKeywords: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OptionSwitchRow(
                title = "Mapping cross-check",
                subtitle = "Compare against the bundled mapping.txt",
                icon = Icons.Filled.VerifiedUser,
                checked = useMappingFile,
                onCheckedChange = onToggleMappingFile,
            )
            OptionSwitchRow(
                title = "Custom keywords",
                subtitle = "Use keyword list: \"legacy\"",
                icon = Icons.Filled.Security,
                checked = customKeywords,
                onCheckedChange = onToggleCustomKeywords,
            )
        }
    }
}

@Composable
private fun OptionSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
