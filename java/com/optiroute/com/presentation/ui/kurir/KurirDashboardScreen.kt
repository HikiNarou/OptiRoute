package com.optiroute.com.presentation.ui.kurir

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
import com.optiroute.com.domain.models.*
import com.optiroute.com.presentation.ui.common.TopAppBarWithLogout
import com.optiroute.com.presentation.viewmodel.AuthViewModel
import com.optiroute.com.presentation.viewmodel.KurirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KurirDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    kurirViewModel: KurirViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showRoutePlanningDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val currentUser by authViewModel.currentUser.collectAsState()
    val pendingCustomers by kurirViewModel.pendingCustomers.collectAsState()
    val assignedCustomers by kurirViewModel.assignedCustomers.collectAsState()
    val deliveryTasks by kurirViewModel.deliveryTasks.collectAsState()
    val optimizedRoute by kurirViewModel.optimizedRoute.collectAsState()
    val kurirState by kurirViewModel.kurirState.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            kurirViewModel.loadPendingCustomers()
            kurirViewModel.loadAssignedCustomers(user.id)
            kurirViewModel.loadDeliveryTasks()
        }
    }

    LaunchedEffect(kurirState) {
        when (kurirState) {
            is KurirViewModel.KurirState.RouteOptimized -> {
                showRoutePlanningDialog = false
                snackbarMessage = (kurirState as KurirViewModel.KurirState.RouteOptimized).message
                showSuccessSnackbar = true
                kurirViewModel.clearKurirState()
            }
            is KurirViewModel.KurirState.Success -> {
                snackbarMessage = (kurirState as KurirViewModel.KurirState.Success).message
                showSuccessSnackbar = true
                kurirViewModel.clearKurirState()
                // Refresh data after successful operation
                currentUser?.let { user ->
                    kurirViewModel.refreshAllData(user.id)
                }
            }
            is KurirViewModel.KurirState.Error -> {
                snackbarMessage = (kurirState as KurirViewModel.KurirState.Error).message
                showSuccessSnackbar = true
                kurirViewModel.clearKurirState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBarWithLogout(
                title = "Kurir Dashboard",
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
            if (selectedTab == 0 && assignedCustomers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showRoutePlanningDialog = true }
                ) {
                    Icon(Icons.Default.Route, contentDescription = "Plan Route")
                }
            }
        },
        snackbarHost = {
            if (showSuccessSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSuccessSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(snackbarMessage)
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
                    text = { Text("Tasks") },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Routes") },
                    icon = { Icon(Icons.Default.Route, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Deliveries") },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> TasksTabContent(
                    pendingCustomers = pendingCustomers,
                    assignedCustomers = assignedCustomers,
                    currentUser = currentUser,
                    isLoading = kurirState is KurirViewModel.KurirState.Loading,
                    onRefresh = {
                        currentUser?.let { user ->
                            kurirViewModel.refreshAllData(user.id)
                        }
                    },
                    onAcceptTask = { customerId ->
                        currentUser?.let { user ->
                            kurirViewModel.acceptTask(customerId, user.id)
                        }
                    },
                    onRejectTask = { customerId, reason ->
                        kurirViewModel.rejectTask(customerId, reason)
                    }
                )
                1 -> RoutesTabContent(
                    optimizedRoute = optimizedRoute,
                    onStartRoute = { routeId ->
                        kurirViewModel.startRoute(routeId)
                    },
                    onNavigateToMap = onNavigateToMap
                )
                2 -> DeliveriesTabContent(
                    deliveryTasks = deliveryTasks,
                    onCompleteDelivery = { taskId, notes ->
                        kurirViewModel.completeDelivery(taskId, notes)
                    },
                    onRefresh = {
                        kurirViewModel.loadDeliveryTasks()
                    },
                    onNavigateToTracking = onNavigateToTracking
                )
                3 -> KurirSettingsTabContent(
                    currentUser = currentUser,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToTracking = onNavigateToTracking,
                    onNavigateToPermissions = onNavigateToPermissions
                )
            }
        }
    }

    if (showRoutePlanningDialog) {
        RoutePlanningDialog(
            assignedCustomers = assignedCustomers,
            onDismiss = {
                showRoutePlanningDialog = false
                kurirViewModel.clearKurirState()
            },
            onPlanRoute = { selectedCustomerIds, depotId, vehicleId ->
                kurirViewModel.selectCustomersForRoute(selectedCustomerIds)
                kurirViewModel.selectDepot(depotId)
                kurirViewModel.selectVehicle(vehicleId)
                kurirViewModel.optimizeRoute()
            },
            isLoading = kurirState is KurirViewModel.KurirState.Loading,
            error = (kurirState as? KurirViewModel.KurirState.Error)?.message
        )
    }
}

@Composable
private fun TasksTabContent(
    pendingCustomers: List<Customer>,
    assignedCustomers: List<Customer>,
    currentUser: User?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAcceptTask: (String) -> Unit,
    onRejectTask: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        // Task Statistics Card
        item {
            TaskStatisticsCard(
                assignedCount = assignedCustomers.size,
                availableCount = pendingCustomers.size
            )
        }

        // Assigned Customers Section
        if (assignedCustomers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assigned to Me (${assignedCustomers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Ready to Route",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(assignedCustomers, key = { it.id }) { customer ->
                KurirCustomerCard(
                    customer = customer,
                    isAssigned = true,
                    isLoading = isLoading
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Available Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Tasks (${pendingCustomers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (pendingCustomers.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "New Tasks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (pendingCustomers.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Assignment,
                    title = "No pending tasks",
                    subtitle = "All available tasks have been assigned. Check back later for new deliveries."
                )
            }
        } else {
            items(pendingCustomers, key = { it.id }) { customer ->
                KurirCustomerCard(
                    customer = customer,
                    isAssigned = false,
                    isLoading = isLoading,
                    onAcceptTask = onAcceptTask,
                    onRejectTask = onRejectTask
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TaskStatisticsCard(
    assignedCount: Int,
    availableCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                icon = Icons.Default.AssignmentTurnedIn,
                label = "My Tasks",
                value = assignedCount.toString(),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
            )

            StatisticItem(
                icon = Icons.Default.Assignment,
                label = "Available",
                value = availableCount.toString(),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
            )

            StatisticItem(
                icon = Icons.Default.TrendingUp,
                label = "Efficiency",
                value = "${if (assignedCount > 0) ((assignedCount.toFloat() / (assignedCount + availableCount)) * 100).toInt() else 0}%",
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun RoutesTabContent(
    optimizedRoute: Route?,
    onStartRoute: (String) -> Unit,
    onNavigateToMap: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Optimized Routes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (optimizedRoute != null) {
                    OutlinedButton(onClick = onNavigateToMap) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Map")
                    }
                }
            }
        }

        if (optimizedRoute != null) {
            item {
                RouteCard(
                    route = optimizedRoute,
                    onStartRoute = onStartRoute,
                    onViewOnMap = { onNavigateToMap() }
                )
            }
        } else {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Route,
                    title = "No routes planned",
                    subtitle = "Accept and assign tasks, then plan an optimized route for efficient delivery."
                )
            }
        }
    }
}

@Composable
private fun DeliveriesTabContent(
    deliveryTasks: List<DeliveryTask>,
    onCompleteDelivery: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToTracking: () -> Unit
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
                    text = "My Deliveries (${deliveryTasks.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNavigateToTracking) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Track All")
                    }

                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        }

        if (deliveryTasks.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.LocalShipping,
                    title = "No deliveries yet",
                    subtitle = "Start a route to begin deliveries and track your progress."
                )
            }
        } else {
            items(deliveryTasks, key = { it.id }) { task ->
                DeliveryTaskCard(
                    task = task,
                    onCompleteDelivery = onCompleteDelivery
                )
            }
        }
    }
}

@Composable
private fun KurirSettingsTabContent(
    currentUser: User?,
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
                text = "Kurir Settings",
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
                    ProfileInfoRow("User Type", "Kurir")

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
                        text = "Delivery Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ClickableSettingsItem(
                        icon = Icons.Default.LocalShipping,
                        title = "Delivery Tracking",
                        subtitle = "Track your active deliveries",
                        onClick = onNavigateToTracking
                    )

                    ClickableSettingsItem(
                        icon = Icons.Default.Security,
                        title = "App Permissions",
                        subtitle = "Manage app permissions and access",
                        onClick = onNavigateToPermissions
                    )

                    SettingsItem(
                        icon = Icons.Default.Navigation,
                        title = "GPS Navigation",
                        subtitle = "Enable turn-by-turn navigation"
                    )

                    SettingsItem(
                        icon = Icons.Default.CameraAlt,
                        title = "Delivery Photos",
                        subtitle = "Take photos as proof of delivery"
                    )

                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Delivery and route notifications"
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
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