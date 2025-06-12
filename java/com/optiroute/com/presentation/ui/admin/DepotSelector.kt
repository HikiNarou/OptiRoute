package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.Depot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepotSelector(
    depots: List<Depot>,
    selectedDepotId: String,
    onDepotSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDepot = depots.find { it.id == selectedDepotId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDepot?.name ?: "Select Depot",
            onValueChange = {},
            readOnly = true,
            label = { Text("Depot") },
            leadingIcon = {
                Icon(Icons.Default.Warehouse, contentDescription = null)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            depots.forEach { depot ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(depot.name)
                            Text(
                                text = depot.location.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onDepotSelected(depot.id)
                        expanded = false
                    }
                )
            }
        }
    }
}