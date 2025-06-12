package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.VehicleType
import com.optiroute.com.presentation.viewmodel.AdminViewModel

data class VehicleData(
    val licensePlate: String,
    val vehicleType: VehicleType,
    val capacity: Double,
    val capacityUnit: String,
    val fuelConsumption: Double,
    val depotId: String,
    val kurirId: String
)

@Composable
fun AddVehicleDialog(
    depots: List<Depot>,
    onDismiss: () -> Unit,
    onConfirm: (VehicleData) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    var licensePlate by remember { mutableStateOf("") }
    var selectedVehicleType by remember { mutableStateOf(VehicleType.VAN) }
    var capacity by remember { mutableStateOf("") }
    var capacityUnit by remember { mutableStateOf("kg") }
    var fuelConsumption by remember { mutableStateOf("") }
    var selectedDepotId by remember { mutableStateOf("") }
    var kurirId by remember { mutableStateOf("") }

    val isValid = licensePlate.isNotBlank() &&
            capacity.isNotBlank() &&
            fuelConsumption.isNotBlank() &&
            selectedDepotId.isNotBlank() &&
            capacity.toDoubleOrNull() != null &&
            fuelConsumption.toDoubleOrNull() != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Vehicle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // License Plate
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    label = { Text("License Plate") },
                    placeholder = { Text("e.g., B 1234 ABC") },
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle Type Selection
                Text(
                    text = "Vehicle Type",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    VehicleType.values().forEach { vehicleType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedVehicleType == vehicleType,
                                    enabled = !isLoading,
                                    onClick = { selectedVehicleType = vehicleType }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedVehicleType == vehicleType,
                                onClick = { selectedVehicleType = vehicleType },
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                getVehicleTypeIcon(vehicleType),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getVehicleTypeDisplayName(vehicleType),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Capacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f),
                        enabled = !isLoading,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = capacityUnit,
                        onValueChange = { capacityUnit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fuel Consumption
                OutlinedTextField(
                    value = fuelConsumption,
                    onValueChange = { fuelConsumption = it },
                    label = { Text("Fuel Consumption (L/100km)") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Depot Selection
                DepotDropdown(
                    depots = depots,
                    selectedDepotId = selectedDepotId,
                    onDepotSelected = { selectedDepotId = it },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kurir Assignment (Optional)
                OutlinedTextField(
                    value = kurirId,
                    onValueChange = { kurirId = it },
                    label = { Text("Assign to Kurir (Optional)") },
                    placeholder = { Text("Leave empty for unassigned") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error display
                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val vehicleData = VehicleData(
                                licensePlate = licensePlate,
                                vehicleType = selectedVehicleType,
                                capacity = capacity.toDouble(),
                                capacityUnit = capacityUnit,
                                fuelConsumption = fuelConsumption.toDouble(),
                                depotId = selectedDepotId,
                                kurirId = kurirId.ifBlank { "" }
                            )
                            onConfirm(vehicleData)
                        },
                        enabled = isValid && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Add Vehicle")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepotDropdown(
    depots: List<Depot>,
    selectedDepotId: String,
    onDepotSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDepot = depots.find { it.id == selectedDepotId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled }
    ) {
        OutlinedTextField(
            value = selectedDepot?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text("Select Depot") },
            placeholder = { Text("Choose depot for this vehicle") },
            leadingIcon = {
                Icon(Icons.Default.Warehouse, contentDescription = null)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (depots.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No depots available") },
                    onClick = { },
                    enabled = false
                )
            } else {
                depots.forEach { depot ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = depot.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
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
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Warehouse,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun getVehicleTypeIcon(vehicleType: VehicleType): ImageVector {
    return when (vehicleType) {
        VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
        VehicleType.CAR -> Icons.Default.DirectionsCar
        VehicleType.VAN -> Icons.Default.LocalAirport
        VehicleType.TRUCK -> Icons.Default.LocalShipping
        VehicleType.PICKUP -> Icons.Default.DirectionsCar
    }
}

private fun getVehicleTypeDisplayName(vehicleType: VehicleType): String {
    return when (vehicleType) {
        VehicleType.MOTORCYCLE -> "Motorcycle"
        VehicleType.CAR -> "Car"
        VehicleType.VAN -> "Van"
        VehicleType.TRUCK -> "Truck"
        VehicleType.PICKUP -> "Pickup"
    }
}