package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.models.VehicleType
import com.optiroute.com.domain.models.MaintenanceStatus
import com.optiroute.com.utils.toDateString

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onEdit: ((Vehicle) -> Unit)? = null,
    onDelete: ((String) -> Unit)? = null,
    onToggleAvailability: ((String, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !vehicle.isAvailable -> MaterialTheme.colorScheme.errorContainer
                vehicle.maintenanceStatus == MaintenanceStatus.MAINTENANCE -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        getVehicleTypeIcon(vehicle.vehicleType),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicle.licensePlate,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getVehicleTypeDisplayName(vehicle.vehicleType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    VehicleStatusChip(
                        isAvailable = vehicle.isAvailable,
                        maintenanceStatus = vehicle.maintenanceStatus
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Details
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    VehicleDetailRow(
                        icon = Icons.Default.Scale,
                        label = "Capacity",
                        value = "${vehicle.capacity} ${vehicle.capacityUnit}"
                    )

                    VehicleDetailRow(
                        icon = Icons.Default.LocalGasStation,
                        label = "Fuel Consumption",
                        value = "${vehicle.fuelConsumption} L/100km"
                    )

                    VehicleDetailRow(
                        icon = Icons.Default.Warehouse,
                        label = "Depot",
                        value = if (vehicle.depotId.isNotEmpty()) vehicle.depotId else "Unassigned"
                    )

                    if (vehicle.kurirId.isNotEmpty()) {
                        VehicleDetailRow(
                            icon = Icons.Default.Person,
                            label = "Assigned to",
                            value = vehicle.kurirId
                        )
                    }

                    VehicleDetailRow(
                        icon = Icons.Default.Build,
                        label = "Maintenance",
                        value = vehicle.maintenanceStatus.name.replace("_", " ")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle Statistics (if available)
            if (vehicle.currentMileage > 0 || vehicle.lastServiceDate > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Vehicle Statistics",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (vehicle.currentMileage > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current Mileage:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${vehicle.currentMileage.toInt()} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        if (vehicle.lastServiceDate > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Last Service:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = vehicle.lastServiceDate.toDateString("dd MMM yyyy"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        if (vehicle.nextServiceDate > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Next Service:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = vehicle.nextServiceDate.toDateString("dd MMM yyyy"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle Availability
                onToggleAvailability?.let {
                    OutlinedButton(
                        onClick = { it(vehicle.id, !vehicle.isAvailable) },
                        modifier = Modifier.weight(1f),
                        colors = if (vehicle.isAvailable) {
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Icon(
                            if (vehicle.isAvailable) Icons.Default.DoNotDisturb else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (vehicle.isAvailable) "Disable" else "Enable")
                    }
                }

                // Edit Button
                onEdit?.let {
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                }

                // Delete Button
                onDelete?.let {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }

            // Created Date
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Added: ${vehicle.createdAt.toDateString("dd MMM yyyy")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Delete Vehicle")
            },
            text = {
                Text("Are you sure you want to delete vehicle ${vehicle.licensePlate}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete?.invoke(vehicle.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VehicleStatusChip(
    isAvailable: Boolean,
    maintenanceStatus: MaintenanceStatus
) {
    val (backgroundColor, contentColor, text) = when {
        !isAvailable -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Unavailable"
        )
        maintenanceStatus == MaintenanceStatus.MAINTENANCE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Maintenance"
        )
        maintenanceStatus == MaintenanceStatus.REPAIR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Repair"
        )
        maintenanceStatus == MaintenanceStatus.OUT_OF_SERVICE -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Out of Service"
        )
        else -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Available"
        )
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun VehicleDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
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