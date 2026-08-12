package com.rezacah.ngaburake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Text field to add a fully qualified class name, with trailing clear and Done-key submit. */
@Composable
fun AddClassForm(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("FQCN to add") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = onAdd,
            enabled = value.isNotBlank(),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add", modifier = Modifier.padding(start = 4.dp))
        }
    }
}
