package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.optiroute.com.domain.models.Location

data class DepotFormData(
    val name: String,
    val location: Location,
    val capacity: Double,
    val operationalHours: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepotDialog(
    onDismiss: () -> Unit,
    onConfirm: (DepotFormData) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var operationalHours by remember { mutableStateOf("08:00 - 17:00") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add New Depot",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Depot Information Section
                    Text(
                        text = "Depot Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Depot Name") },
                        leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity (tons)") },
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = operationalHours,
                        onValueChange = { operationalHours = it },
                        label = { Text("Operational Hours") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g., 08:00 - 17:00") }
                    )

                    Divider()

                    // Location Section
                    Text(
                        text = "Location Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitude") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("e.g., -6.175392") }
                        )

                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitude") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("e.g., 106.827153") }
                        )
                    }

                    // Location Help Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You can get coordinates from Google Maps or GPS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val lat = latitude.toDoubleOrNull() ?: 0.0
                            val lng = longitude.toDoubleOrNull() ?: 0.0
                            val cap = capacity.toDoubleOrNull() ?: 0.0

                            val depotData = DepotFormData(
                                name = name,
                                location = Location(lat, lng, address),
                                capacity = cap,
                                operationalHours = operationalHours
                            )
                            onConfirm(depotData)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading &&
                                name.isNotBlank() &&
                                address.isNotBlank() &&
                                capacity.isNotBlank() &&
                                latitude.isNotBlank() &&
                                longitude.isNotBlank() &&
                                operationalHours.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Add Depot")
                        }
                    }
                }
            }
        }
    }
}