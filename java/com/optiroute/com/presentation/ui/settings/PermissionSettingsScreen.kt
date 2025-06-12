package com.optiroute.com.presentation.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.*
import com.optiroute.com.presentation.viewmodel.PermissionSettingsViewModel
import com.optiroute.com.utils.PermissionManager

data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isRequired: Boolean = true
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PermissionSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissionStatus by viewModel.permissionStatus.collectAsState()

    val permissionItems = remember {
        listOf(
            PermissionItem(
                permission = PermissionManager.LOCATION_PERMISSION,
                title = "Location Access",
                description = "Required for navigation and route optimization",
                icon = Icons.Default.LocationOn
            ),
            PermissionItem(
                permission = PermissionManager.CAMERA_PERMISSION,
                title = "Camera Access",
                description = "For taking delivery proof photos",
                icon = Icons.Default.CameraAlt
            ),
            PermissionItem(
                permission = PermissionManager.PHONE_PERMISSION,
                title = "Phone Access",
                description = "To call customers directly from the app",
                icon = Icons.Default.Phone
            ),
            PermissionItem(
                permission = PermissionManager.NOTIFICATION_PERMISSION,
                title = "Notifications",
                description = "Receive delivery updates and notifications",
                icon = Icons.Default.Notifications
            ),
            PermissionItem(
                permission = PermissionManager.READ_EXTERNAL_STORAGE,
                title = "Storage Access",
                description = "Access files and save delivery documents",
                icon = Icons.Default.Storage,
                isRequired = false
            )
        )
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = permissionItems.map { it.permission }
    )

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        viewModel.checkPermissions(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "App Permissions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage app permissions and access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "App Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PermissionSummaryCard(
                    permissionStatus = permissionStatus,
                    onRequestAllPermissions = {
                        if (!multiplePermissionsState.allPermissionsGranted) {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                        }
                    }
                )
            }

            items(permissionItems) { permissionItem ->
                val permissionState = rememberPermissionState(permissionItem.permission)

                PermissionCard(
                    permissionItem = permissionItem,
                    isGranted = permissionState.status.isGranted,
                    shouldShowRationale = permissionState.status.shouldShowRationale,
                    onRequestPermission = {
                        if (!permissionState.status.isGranted) {
                            permissionState.launchPermissionRequest()
                        }
                    },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            item {
                PermissionTipsCard()
            }
        }
    }
}

@Composable
private fun PermissionSummaryCard(
    permissionStatus: PermissionManager.PermissionStatus,
    onRequestAllPermissions: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (permissionStatus.allRequiredPermissionsGranted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (permissionStatus.allRequiredPermissionsGranted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = if (permissionStatus.allRequiredPermissionsGranted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (permissionStatus.allRequiredPermissionsGranted) {
                            "All Permissions Granted"
                        } else {
                            "Permissions Required"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (permissionStatus.allRequiredPermissionsGranted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = if (permissionStatus.allRequiredPermissionsGranted) {
                            "OptiRoute has all necessary permissions to function properly"
                        } else {
                            "Some features may not work without required permissions"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (permissionStatus.allRequiredPermissionsGranted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            if (!permissionStatus.allRequiredPermissionsGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestAllPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant All Permissions")
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    permissionItem: PermissionItem,
    isGranted: Boolean,
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        permissionItem.icon,
                        contentDescription = null,
                        tint = if (isGranted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = permissionItem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (permissionItem.isRequired) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Required",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = permissionItem.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PermissionStatusChip(isGranted = isGranted)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isGranted) {
                    if (shouldShowRationale) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Settings")
                        }
                    }

                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Grant Access")
                    }
                } else {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Permission Granted")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusChip(isGranted: Boolean) {
    Surface(
        color = if (isGranted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isGranted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "Granted" else "Denied",
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}

@Composable
private fun PermissionTipsCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permission Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Location permission is essential for navigation and route optimization",
                "Camera access allows you to take proof of delivery photos",
                "Phone permission enables direct calling to customers",
                "Notification permission keeps you updated on delivery status",
                "You can change permissions anytime in your device settings"
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}