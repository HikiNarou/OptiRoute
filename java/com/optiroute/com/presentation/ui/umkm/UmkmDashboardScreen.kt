package com.optiroute.com.presentation.ui.umkm

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
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.presentation.ui.common.TopAppBarWithLogout
import com.optiroute.com.presentation.viewmodel.AuthViewModel
import com.optiroute.com.presentation.viewmodel.UmkmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmkmDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    umkmViewModel: UmkmViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val customers by umkmViewModel.customers.collectAsState()
    val customerState by umkmViewModel.customerState.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            umkmViewModel.loadCustomers(user.id)
        }
    }

    LaunchedEffect(customerState) {
        when (customerState) {
            is UmkmViewModel.CustomerState.Success -> {
                showAddCustomerDialog = false
                umkmViewModel.clearCustomerState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBarWithLogout(
                title = "UMKM Dashboard",
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
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddCustomerDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Customer")
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
                    text = { Text("Customers") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Deliveries") },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> CustomersTabContent(
                    customers = customers,
                    onRefresh = {
                        currentUser?.let { user ->
                            umkmViewModel.loadCustomers(user.id)
                        }
                    }
                )
                1 -> DeliveriesTabContent(
                    customers = customers,
                    onNavigateToTracking = onNavigateToTracking
                )
                2 -> SettingsTabContent(
                    currentUser = currentUser,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToTracking = onNavigateToTracking,
                    onNavigateToPermissions = onNavigateToPermissions
                )
            }
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = {
                showAddCustomerDialog = false
                umkmViewModel.clearCustomerState()
            },
            onConfirm = { customerData ->
                currentUser?.let { user ->
                    umkmViewModel.createCustomer(
                        name = customerData.name,
                        location = customerData.location,
                        phoneNumber = customerData.phoneNumber,
                        email = customerData.email,
                        itemType = customerData.itemType,
                        itemWeight = customerData.itemWeight,
                        weightUnit = customerData.weightUnit,
                        notes = customerData.notes,
                        priority = customerData.priority,
                        umkmId = user.id
                    )
                }
            },
            isLoading = customerState is UmkmViewModel.CustomerState.Loading,
            error = (customerState as? UmkmViewModel.CustomerState.Error)?.message
        )
    }
}

@Composable
private fun CustomersTabContent(
    customers: List<Customer>,
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
                    text = "My Customers (${customers.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        if (customers.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.People,
                    title = "No customers yet",
                    subtitle = "Add your first customer to get started"
                )
            }
        } else {
            items(customers) { customer ->
                CustomerCard(customer = customer)
            }
        }
    }
}

@Composable
private fun DeliveriesTabContent(
    customers: List<Customer>,
    onNavigateToTracking: () -> Unit
) {
    val deliveredCustomers = customers.filter { it.status.name in listOf("DELIVERED", "IN_DELIVERY") }

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
                    text = "Delivery Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(onClick = onNavigateToTracking) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Track All")
                }
            }
        }

        if (deliveredCustomers.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.LocalShipping,
                    title = "No active deliveries",
                    subtitle = "Your delivery tracking will appear here"
                )
            }
        } else {
            items(deliveredCustomers) { customer ->
                DeliveryCard(customer = customer)
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
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
                text = "Account Settings",
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
                    ProfileInfoRow("User Type", "UMKM")

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
                        text = "App Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ClickableSettingsItem(
                        icon = Icons.Default.LocalShipping,
                        title = "Delivery Tracking",
                        subtitle = "Track your shipments",
                        onClick = onNavigateToTracking
                    )

                    ClickableSettingsItem(
                        icon = Icons.Default.Security,
                        title = "App Permissions",
                        subtitle = "Manage app permissions",
                        onClick = onNavigateToPermissions
                    )

                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Manage delivery notifications"
                    )

                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English"
                    )

                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help & Support",
                        subtitle = "Get help and support"
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