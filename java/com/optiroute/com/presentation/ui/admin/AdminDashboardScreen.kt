package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.presentation.ui.common.TopAppBarWithLogout
import com.optiroute.com.presentation.viewmodel.AdminViewModel
import com.optiroute.com.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToUserManagement: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDepotDialog by remember { mutableStateOf(false) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val depots by adminViewModel.depots.collectAsState()
    val vehicles by adminViewModel.vehicles.collectAsState()
    val adminState by adminViewModel.adminState.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let {
            adminViewModel.loadDepots()
        }
    }

    LaunchedEffect(adminState) {
        when (adminState) {
            is AdminViewModel.AdminState.Success -> {
                showAddDepotDialog = false
                showAddVehicleDialog = false
                adminViewModel.clearAdminState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBarWithLogout(
                title = "Admin Dashboard",
                subtitle = "Welcome, ${currentUser?.fullName}",
                onLogout = {
                    authViewModel.logout()
                    onLogout()
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { showAddDepotDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Depot")
                }
                1 -> FloatingActionButton(
                    onClick = { showAddVehicleDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Depots") },
                    icon = { Icon(Icons.Default.Warehouse, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        if (depots.isNotEmpty()) {
                            adminViewModel.loadVehicles(depots.first().id)
                        }
                    },
                    text = { Text("Vehicles") },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Users") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> DepotsTabContent(
                    depots = depots,
                    onRefresh = { adminViewModel.loadDepots() }
                )
                1 -> VehiclesTabContent(
                    vehicles = vehicles,
                    depots = depots,
                    onDepotSelected = { depotId ->
                        adminViewModel.loadVehicles(depotId)
                    }
                )
                2 -> UsersTabContent(
                    onNavigateToUserManagement = onNavigateToUserManagement
                )
                3 -> AdminSettingsTabContent(
                    currentUser = currentUser,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToTracking = onNavigateToTracking,
                    onNavigateToPermissions = onNavigateToPermissions
                )
            }
        }
    }

    if (showAddDepotDialog) {
        AddDepotDialog(
            onDismiss = {
                showAddDepotDialog = false
                adminViewModel.clearAdminState()
            },
            onConfirm = { depotData ->
                adminViewModel.createDepot(
                    name = depotData.name,
                    location = depotData.location,
                    capacity = depotData.capacity,
                    operationalHours = depotData.operationalHours
                )
            },
            isLoading = adminState is AdminViewModel.AdminState.Loading,
            error = (adminState as? AdminViewModel.AdminState.Error)?.message
        )
    }

    if (showAddVehicleDialog) {
        AddVehicleDialog(
            depots = depots,
            onDismiss = {
                showAddVehicleDialog = false
                adminViewModel.clearAdminState()
            },
            onConfirm = { vehicleData ->
                adminViewModel.createVehicle(
                    licensePlate = vehicleData.licensePlate,
                    vehicleType = vehicleData.vehicleType,
                    capacity = vehicleData.capacity,
                    capacityUnit = vehicleData.capacityUnit,
                    fuelConsumption = vehicleData.fuelConsumption,
                    depotId = vehicleData.depotId,
                    kurirId = vehicleData.kurirId
                )
            },
            isLoading = adminState is AdminViewModel.AdminState.Loading,
            error = (adminState as? AdminViewModel.AdminState.Error)?.message
        )
    }
}

@Composable
private fun DepotsTabContent(
    depots: List<Depot>,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Depots (${depots.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        if (depots.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Warehouse,
                    title = "No depots yet",
                    subtitle = "Add your first depot to get started"
                )
            }
        } else {
            items(depots) { depot ->
                DepotCard(depot = depot)
            }
        }
    }
}

@Composable
private fun VehiclesTabContent(
    vehicles: List<Vehicle>,
    depots: List<Depot>,
    onDepotSelected: (String) -> Unit
) {
    var selectedDepotId by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (depots.isNotEmpty()) {
            item {
                DepotSelector(
                    depots = depots,
                    selectedDepotId = selectedDepotId,
                    onDepotSelected = { depotId ->
                        selectedDepotId = depotId
                        onDepotSelected(depotId)
                    }
                )
            }
        }

        if (vehicles.isEmpty() && selectedDepotId.isNotEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.DirectionsCar,
                    title = "No vehicles in this depot",
                    subtitle = "Add vehicles to this depot"
                )
            }
        } else if (vehicles.isEmpty() && depots.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Warehouse,
                    title = "Create a depot first",
                    subtitle = "You need to create a depot before adding vehicles"
                )
            }
        } else {
            items(vehicles) { vehicle ->
                VehicleCard(vehicle = vehicle)
            }
        }
    }
}

@Composable
private fun UsersTabContent(
    onNavigateToUserManagement: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "User Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Manage System Users",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add, edit, and manage all users in the OptiRoute system including UMKM owners, couriers, and other administrators.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToUserManagement,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Users")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsTabContent(
    currentUser: com.optiroute.com.domain.models.User?,
    onNavigateToProfile: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Admin Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Profile Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow("Full Name", currentUser?.fullName ?: "")
                    ProfileInfoRow("Username", currentUser?.username ?: "")
                    ProfileInfoRow("Email", currentUser?.email ?: "")
                    ProfileInfoRow("User Type", "Admin")

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "System Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ClickableSettingsItem(
                        icon = Icons.Default.LocalShipping,
                        title = "Delivery Tracking",
                        subtitle = "Monitor all system deliveries",
                        onClick = onNavigateToTracking
                    )

                    ClickableSettingsItem(
                        icon = Icons.Default.Security,
                        title = "App Permissions",
                        subtitle = "Configure security policies",
                        onClick = onNavigateToPermissions
                    )

                    SettingsItem(
                        icon = Icons.Default.Analytics,
                        title = "System Analytics",
                        subtitle = "View system performance metrics"
                    )

                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "System Notifications",
                        subtitle = "Manage system-wide notifications"
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClickableSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}