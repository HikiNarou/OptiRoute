package com.optiroute.com.presentation.ui.kurir

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.DeliveryTask
import com.optiroute.com.domain.models.DeliveryStatus
import com.optiroute.com.utils.toDateString

@Composable
fun DeliveryTaskCard(
    task: DeliveryTask,
    onCompleteDelivery: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showFailDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                DeliveryStatus.ASSIGNED -> MaterialTheme.colorScheme.secondaryContainer
                DeliveryStatus.PICKED_UP -> MaterialTheme.colorScheme.tertiaryContainer
                DeliveryStatus.IN_TRANSIT -> MaterialTheme.colorScheme.primaryContainer
                DeliveryStatus.DELIVERED -> MaterialTheme.colorScheme.primary
                DeliveryStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                DeliveryStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Task #${task.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Customer: ${task.customerId.take(8)}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (task.estimatedDeliveryTime > 0) {
                        Text(
                            text = "ETA: ${task.estimatedDeliveryTime.toDateString("HH:mm")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DeliveryStatusChip(status = task.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Information
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    DeliveryInfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Assigned",
                        value = task.assignedAt.toDateString("dd MMM HH:mm")
                    )

                    if (task.pickedUpAt > 0) {
                        DeliveryInfoRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Picked Up",
                            value = task.pickedUpAt.toDateString("dd MMM HH:mm")
                        )
                    }

                    if (task.actualDeliveryTime > 0) {
                        DeliveryInfoRow(
                            icon = Icons.Default.LocalShipping,
                            label = "Delivered",
                            value = task.actualDeliveryTime.toDateString("dd MMM HH:mm")
                        )
                    }
                }
            }

            // Notes Section
            if (task.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            when (task.status) {
                DeliveryStatus.ASSIGNED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFailDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cannot Deliver")
                        }

                        Button(
                            onClick = { /* Mark as picked up */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick Up")
                        }
                    }
                }

                DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFailDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Failed")
                        }

                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete")
                        }
                    }
                }

                DeliveryStatus.DELIVERED -> {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delivery Completed")
                    }
                }

                DeliveryStatus.FAILED, DeliveryStatus.CANCELLED -> {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (task.status == DeliveryStatus.FAILED) "Delivery Failed" else "Delivery Cancelled")
                    }
                }
            }
        }
    }

    // Complete Delivery Dialog
    if (showCompleteDialog) {
        var completionNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Complete Delivery",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Confirm that this delivery has been successfully completed.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = completionNotes,
                        onValueChange = { completionNotes = it },
                        label = { Text("Completion Notes (Optional)") },
                        placeholder = { Text("e.g., Delivered to customer directly, Left at front door") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCompleteDelivery(task.id, completionNotes)
                        showCompleteDialog = false
                    }
                ) {
                    Icon(
                        Icons.Default.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Complete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCompleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Fail Delivery Dialog
    if (showFailDialog) {
        var failureReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFailDialog = false },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Mark Delivery as Failed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please provide a reason for delivery failure:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = failureReason,
                        onValueChange = { failureReason = it },
                        label = { Text("Failure Reason") },
                        placeholder = { Text("e.g., Customer not available, Address incorrect") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle delivery failure
                        showFailDialog = false
                    },
                    enabled = failureReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Failed")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFailDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DeliveryStatusChip(status: DeliveryStatus) {
    val (backgroundColor, contentColor, text, icon) = when (status) {
        DeliveryStatus.ASSIGNED -> Quadruple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Assigned",
            Icons.Default.Assignment
        )
        DeliveryStatus.PICKED_UP -> Quadruple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Picked Up",
            Icons.Default.CheckCircle
        )
        DeliveryStatus.IN_TRANSIT -> Quadruple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "In Transit",
            Icons.Default.LocalShipping
        )
        DeliveryStatus.DELIVERED -> Quadruple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Delivered",
            Icons.Default.TaskAlt
        )
        DeliveryStatus.FAILED -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Failed",
            Icons.Default.Error
        )
        DeliveryStatus.CANCELLED -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled",
            Icons.Default.Cancel
        )
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DeliveryInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            tint = MaterialTheme.colorScheme.primary
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

// Helper data class for Quadruple
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)