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
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import com.optiroute.com.presentation.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val users by viewModel.users.collectAsState()
    val userManagementState by viewModel.userManagementState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    LaunchedEffect(userManagementState) {
        when (userManagementState) {
            is UserManagementViewModel.UserManagementState.Success -> {
                showAddUserDialog = false
                showEditUserDialog = false
                selectedUser = null
                viewModel.clearState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "User Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAllUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true }
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
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
                    text = { Text("All Users") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Kurir") },
                    icon = { Icon(Icons.Default.DeliveryDining, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("UMKM") },
                    icon = { Icon(Icons.Default.Store, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Admins") },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
                )
            }

            val filteredUsers = when (selectedTab) {
                1 -> users.filter { it.userType == UserType.KURIR }
                2 -> users.filter { it.userType == UserType.UMKM }
                3 -> users.filter { it.userType == UserType.ADMIN }
                else -> users
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    UserManagementSummary(
                        totalUsers = users.size,
                        activeUsers = users.count { it.isActive },
                        kurirCount = users.count { it.userType == UserType.KURIR },
                        umkmCount = users.count { it.userType == UserType.UMKM },
                        adminCount = users.count { it.userType == UserType.ADMIN }
                    )
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.People,
                            title = "No users found",
                            subtitle = when (selectedTab) {
                                1 -> "No kurir registered yet"
                                2 -> "No UMKM registered yet"
                                3 -> "No other admins found"
                                else -> "No users in the system"
                            }
                        )
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserManagementCard(
                            user = user,
                            onEdit = {
                                selectedUser = user
                                showEditUserDialog = true
                            },
                            onToggleStatus = {
                                viewModel.toggleUserStatus(user.id, !user.isActive)
                            },
                            onDelete = {
                                viewModel.deleteUser(user.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = {
                showAddUserDialog = false
                viewModel.clearState()
            },
            onConfirm = { userData ->
                viewModel.createUser(
                    username = userData.username,
                    email = userData.email,
                    fullName = userData.fullName,
                    password = userData.password,
                    userType = userData.userType
                )
            },
            isLoading = userManagementState is UserManagementViewModel.UserManagementState.Loading,
            error = (userManagementState as? UserManagementViewModel.UserManagementState.Error)?.message
        )
    }

    if (showEditUserDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            onDismiss = {
                showEditUserDialog = false
                selectedUser = null
                viewModel.clearState()
            },
            onConfirm = { userData ->
                viewModel.updateUser(userData)
            },
            isLoading = userManagementState is UserManagementViewModel.UserManagementState.Loading,
            error = (userManagementState as? UserManagementViewModel.UserManagementState.Error)?.message
        )
    }
}

@Composable
private fun UserManagementSummary(
    totalUsers: Int,
    activeUsers: Int,
    kurirCount: Int,
    umkmCount: Int,
    adminCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "User Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Default.People,
                    label = "Total",
                    value = "$totalUsers",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                SummaryItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Active",
                    value = "$activeUsers",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                SummaryItem(
                    icon = Icons.Default.DeliveryDining,
                    label = "Kurir",
                    value = "$kurirCount",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                SummaryItem(
                    icon = Icons.Default.Store,
                    label = "UMKM",
                    value = "$umkmCount",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                SummaryItem(
                    icon = Icons.Default.AdminPanelSettings,
                    label = "Admin",
                    value = "$adminCount",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}