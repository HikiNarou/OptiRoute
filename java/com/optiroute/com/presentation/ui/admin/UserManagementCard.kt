package com.optiroute.com.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import com.optiroute.com.utils.toDateString

@Composable
fun UserManagementCard(
    user: User,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserTypeIcon(userType = user.userType)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    UserStatusChip(
                        userType = user.userType,
                        isActive = user.isActive
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Details
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    UserDetailRow("User ID", user.id.take(8) + "...")
                    UserDetailRow("User Type", user.userType.name)
                    UserDetailRow("Status", if (user.isActive) "Active" else "Inactive")
                    UserDetailRow("Created", user.createdAt.toDateString("dd MMM yyyy"))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
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

                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (user.isActive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Icon(
                        if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (user.isActive) "Disable" else "Enable")
                }

                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

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
                Text("Delete User")
            },
            text = {
                Text("Are you sure you want to delete ${user.fullName}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
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
private fun UserTypeIcon(userType: UserType) {
    val icon = when (userType) {
        UserType.UMKM -> Icons.Default.Store
        UserType.ADMIN -> Icons.Default.AdminPanelSettings
        UserType.KURIR -> Icons.Default.DeliveryDining
    }

    Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun UserStatusChip(
    userType: UserType,
    isActive: Boolean
) {
    Surface(
        color = when {
            !isActive -> MaterialTheme.colorScheme.errorContainer
            userType == UserType.ADMIN -> MaterialTheme.colorScheme.primaryContainer
            userType == UserType.KURIR -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = userType.name,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    !isActive -> MaterialTheme.colorScheme.onErrorContainer
                    userType == UserType.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                    userType == UserType.KURIR -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
            if (!isActive) {
                Text(
                    text = "INACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun UserDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}