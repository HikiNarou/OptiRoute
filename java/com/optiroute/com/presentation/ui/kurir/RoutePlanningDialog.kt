package com.optiroute.com.presentation.ui.kurir

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.presentation.viewmodel.RoutePlanningViewModel

@Composable
fun RoutePlanningDialog(
    assignedCustomers: List<Customer>,
    onDismiss: () -> Unit,
    onPlanRoute: (selectedCustomerIds: List<String>, depotId: String, vehicleId: String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    routePlanningViewModel: RoutePlanningViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    var selectedCustomerIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedDepotId by remember { mutableStateOf("") }
    var selectedVehicleId by remember { mutableStateOf("") }

    val depots by routePlanningViewModel.depots.collectAsState()
    val vehicles by routePlanningViewModel.vehicles.collectAsState()
    val viewModelState by routePlanningViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        routePlanningViewModel.loadDepots()
    }

    LaunchedEffect(selectedDepotId) {
        if (selectedDepotId.isNotEmpty()) {
            routePlanningViewModel.loadVehiclesByDepot(selectedDepotId)
        }
    }

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
                // Header with progress
                RoutePlanningHeader(
                    currentStep = currentStep,
                    totalSteps = 4,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Content based on current step
                when (currentStep) {
                    1 -> CustomerSelectionStep(
                        customers = assignedCustomers,
                        selectedCustomerIds = selectedCustomerIds,
                        onSelectionChange = { selectedCustomerIds = it }
                    )
                    2 -> DepotSelectionStep(
                        depots = depots,
                        selectedDepotId = selectedDepotId,
                        onDepotSelected = { selectedDepotId = it },
                        isLoading = viewModelState is RoutePlanningViewModel.State.Loading,
                        error = (viewModelState as? RoutePlanningViewModel.State.Error)?.message
                    )
                    3 -> VehicleSelectionStep(
                        vehicles = vehicles,
                        selectedVehicleId = selectedVehicleId,
                        onVehicleSelected = { selectedVehicleId = it },
                        isLoading = viewModelState is RoutePlanningViewModel.State.Loading,
                        error = (viewModelState as? RoutePlanningViewModel.State.Error)?.message
                    )
                    4 -> RouteConfirmationStep(
                        selectedCustomers = assignedCustomers.filter { it.id in selectedCustomerIds },
                        selectedDepot = depots.find { it.id == selectedDepotId },
                        selectedVehicle = vehicles.find { it.id == selectedVehicleId }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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

                // Navigation buttons
                RoutePlanningNavigationButtons(
                    currentStep = currentStep,
                    canProceed = when (currentStep) {
                        1 -> selectedCustomerIds.isNotEmpty()
                        2 -> selectedDepotId.isNotEmpty()
                        3 -> selectedVehicleId.isNotEmpty()
                        4 -> true
                        else -> false
                    },
                    isLoading = isLoading,
                    onBack = {
                        if (currentStep > 1) {
                            currentStep--
                        } else {
                            onDismiss()
                        }
                    },
                    onNext = {
                        if (currentStep < 4) {
                            currentStep++
                        } else {
                            onPlanRoute(
                                selectedCustomerIds.toList(),
                                selectedDepotId,
                                selectedVehicleId
                            )
                        }
                    },
                    onCancel = onDismiss
                )
            }
        }
    }
}

@Composable
private fun RoutePlanningHeader(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plan Delivery Route",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isActive = stepNumber == currentStep
                val isCompleted = stepNumber < currentStep

                StepIndicator(
                    stepNumber = stepNumber,
                    title = when (stepNumber) {
                        1 -> "Select\nCustomers"
                        2 -> "Choose\nDepot"
                        3 -> "Select\nVehicle"
                        4 -> "Confirm\nRoute"
                        else -> ""
                    },
                    isActive = isActive,
                    isCompleted = isCompleted
                )

                if (index < totalSteps - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (isActive || isCompleted) 1f else 0.5f,
        label = "stepAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = stepNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
    }
}

@Composable
private fun CustomerSelectionStep(
    customers: List<Customer>,
    selectedCustomerIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    Column {
        Text(
            text = "Select Customers for Route",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Choose which customers to include in this delivery route",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedCustomerIds.size} of ${customers.size} selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row {
                TextButton(
                    onClick = { onSelectionChange(emptySet()) }
                ) {
                    Text("Clear All")
                }

                TextButton(
                    onClick = { onSelectionChange(customers.map { it.id }.toSet()) }
                ) {
                    Text("Select All")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(customers) { customer ->
                CustomerSelectionItem(
                    customer = customer,
                    isSelected = customer.id in selectedCustomerIds,
                    onSelectionToggle = {
                        val newSelection = if (customer.id in selectedCustomerIds) {
                            selectedCustomerIds - customer.id
                        } else {
                            selectedCustomerIds + customer.id
                        }
                        onSelectionChange(newSelection)
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomerSelectionItem(
    customer: Customer,
    isSelected: Boolean,
    onSelectionToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelectionToggle
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${customer.itemType} - ${customer.itemWeight} ${customer.weightUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = when (customer.priority.name) {
                    "URGENT" -> MaterialTheme.colorScheme.error
                    "HIGH" -> MaterialTheme.colorScheme.tertiary
                    "NORMAL" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = customer.priority.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DepotSelectionStep(
    depots: List<Depot>,
    selectedDepotId: String,
    onDepotSelected: (String) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column {
        Text(
            text = "Choose Starting Depot",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Select the depot where this route will start",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading depots...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else if (depots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warehouse,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No depots available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Contact your admin to create depots",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(depots) { depot ->
                    DepotSelectionItem(
                        depot = depot,
                        isSelected = depot.id == selectedDepotId,
                        onSelected = { onDepotSelected(depot.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DepotSelectionItem(
    depot: Depot,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelected
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = depot.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = depot.location.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Capacity: ${depot.capacity} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VehicleSelectionStep(
    vehicles: List<Vehicle>,
    selectedVehicleId: String,
    onVehicleSelected: (String) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column {
        Text(
            text = "Select Vehicle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Choose the vehicle for this delivery route",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading vehicles...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else if (vehicles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No vehicles available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Contact your admin to add vehicles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vehicles) { vehicle ->
                    VehicleSelectionItem(
                        vehicle = vehicle,
                        isSelected = vehicle.id == selectedVehicleId,
                        onSelected = { onVehicleSelected(vehicle.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleSelectionItem(
    vehicle: Vehicle,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelected
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.licensePlate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${vehicle.vehicleType.name} - ${vehicle.capacity} ${vehicle.capacityUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fuel: ${vehicle.fuelConsumption} L/100km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (vehicle.isAvailable) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "In Use",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteConfirmationStep(
    selectedCustomers: List<Customer>,
    selectedDepot: Depot?,
    selectedVehicle: Vehicle?
) {
    Column {
        Text(
            text = "Confirm Route",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Review your route configuration before optimization",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                RouteConfirmationSection(
                    title = "Selected Customers (${selectedCustomers.size})",
                    icon = Icons.Default.People
                ) {
                    selectedCustomers.forEach { customer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${customer.itemWeight} ${customer.weightUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                selectedDepot?.let { depot ->
                    RouteConfirmationSection(
                        title = "Starting Depot",
                        icon = Icons.Default.Warehouse
                    ) {
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
                }
            }

            item {
                selectedVehicle?.let { vehicle ->
                    RouteConfirmationSection(
                        title = "Selected Vehicle",
                        icon = Icons.Default.DirectionsCar
                    ) {
                        Text(
                            text = vehicle.licensePlate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${vehicle.vehicleType.name} - ${vehicle.capacity} ${vehicle.capacityUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                val totalWeight = selectedCustomers.sumOf { it.itemWeight }
                val vehicleCapacity = selectedVehicle?.capacity ?: 0.0
                val utilizationPercent = if (vehicleCapacity > 0) {
                    (totalWeight / vehicleCapacity * 100).toInt()
                } else 0

                RouteConfirmationSection(
                    title = "Route Summary",
                    icon = Icons.Default.Analytics
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Weight:")
                        Text("${totalWeight} kg")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vehicle Utilization:")
                        Text("$utilizationPercent%")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Stops:")
                        Text("${selectedCustomers.size}")
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteConfirmationSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun RoutePlanningNavigationButtons(
    currentStep: Int,
    canProceed: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }

        if (currentStep > 1) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back")
            }
        }

        Button(
            onClick = onNext,
            enabled = canProceed && !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (currentStep == 4) "Optimize Route" else "Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (currentStep == 4) Icons.Default.Route else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}