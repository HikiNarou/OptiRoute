package com.optiroute.com.presentation.ui.kurir

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.CustomerPriority
import com.optiroute.com.domain.models.CustomerStatus
import com.optiroute.com.utils.makePhoneCall
import com.optiroute.com.utils.toDateString
import com.optiroute.com.utils.toFormattedDistance

@Composable
fun KurirCustomerCard(
    customer: Customer,
    isAssigned: Boolean,
    onAcceptTask: ((String) -> Unit)? = null,
    onRejectTask: ((String, String) -> Unit)? = null,
    onViewDetails: ((Customer) -> Unit)? = null,
    onCallCustomer: ((String) -> Unit)? = null,
    distanceFromCurrentLocation: Double? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAssigned) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isAssigned) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isAssigned) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = customer.location.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    TaskStatusChip(
                        status = customer.status,
                        isAssigned = isAssigned
                    )

                    if (distanceFromCurrentLocation != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        DistanceChip(distance = distanceFromCurrentLocation)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Package Information
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PackageInfoItem(
                        icon = Icons.Default.Inventory,
                        label = "Item",
                        value = customer.itemType,
                        modifier = Modifier.weight(1f),
                        isAssigned = isAssigned
                    )

                    PackageInfoItem(
                        icon = Icons.Default.Scale,
                        label = "Weight",
                        value = "${customer.itemWeight} ${customer.weightUnit}",
                        modifier = Modifier.weight(1f),
                        isAssigned = isAssigned
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Priority and Contact Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(priority = customer.priority)

                if (customer.phoneNumber.isNotEmpty()) {
                    ContactButton(
                        phoneNumber = customer.phoneNumber,
                        isAssigned = isAssigned,
                        onCall = {
                            onCallCustomer?.invoke(customer.phoneNumber) ?: run {
                                makePhoneCall(context, customer.phoneNumber)
                            }
                        }
                    )
                }
            }

            // Notes Section
            if (customer.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = customer.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Added: ${customer.createdAt.toDateString("dd MMM")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task Action Buttons
            if (!isAssigned && customer.status == CustomerStatus.PENDING && onAcceptTask != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDetailsDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details")
                    }

                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline")
                    }

                    Button(
                        onClick = { showAcceptDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                }
            } else if (isAssigned) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDetailsDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Details")
                    }

                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plan Route")
                    }
                }
            }
        }
    }

    // Accept Task Confirmation Dialog
    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Accept Delivery Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to accept this delivery task?",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Task Details:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customer: ${customer.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Item: ${customer.itemType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Weight: ${customer.itemWeight} ${customer.weightUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Priority: ${customer.priority.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAcceptTask?.invoke(customer.id)
                        showAcceptDialog = false
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept Task")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAcceptDialog = false },
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Task Dialog
    if (showRejectDialog) {
        var rejectReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Decline Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please provide a reason for declining this task:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason") },
                        placeholder = { Text("e.g., Schedule conflict, Too far from current location") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectTask?.invoke(customer.id, rejectReason)
                        showRejectDialog = false
                    },
                    enabled = rejectReason.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRejectDialog = false },
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Details Dialog
    if (showDetailsDialog) {
        CustomerDetailsDialog(
            customer = customer,
            onDismiss = { showDetailsDialog = false },
            onAccept = if (!isAssigned && customer.status == CustomerStatus.PENDING) {
                { onAcceptTask?.invoke(customer.id) }
            } else null
        )
    }
}

@Composable
private fun TaskStatusChip(
    status: CustomerStatus,
    isAssigned: Boolean
) {
    val (backgroundColor, contentColor, text) = when (status) {
        CustomerStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Pending"
        )
        CustomerStatus.ASSIGNED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            if (isAssigned) "My Task" else "Assigned"
        )
        CustomerStatus.IN_DELIVERY -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "In Delivery"
        )
        CustomerStatus.DELIVERED -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Delivered"
        )
        CustomerStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled"
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
private fun DistanceChip(distance: Double) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Navigation,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = distance.toFormattedDistance(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PackageInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isAssigned: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isAssigned) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isAssigned) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun PriorityChip(priority: CustomerPriority) {
    val (backgroundColor, contentColor, icon) = when (priority) {
        CustomerPriority.LOW -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.KeyboardArrowDown
        )
        CustomerPriority.NORMAL -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Remove
        )
        CustomerPriority.HIGH -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.KeyboardArrowUp
        )
        CustomerPriority.URGENT -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.PriorityHigh
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
                text = priority.name,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ContactButton(
    phoneNumber: String,
    isAssigned: Boolean,
    onCall: () -> Unit
) {
    OutlinedButton(
        onClick = onCall,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = if (isAssigned) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Icon(
            Icons.Default.Phone,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Call",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun CustomerDetailsDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onAccept: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Customer Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                CustomerDetailItem("Name", customer.name)
                CustomerDetailItem("Phone", customer.phoneNumber)
                CustomerDetailItem("Email", customer.email)
                CustomerDetailItem("Address", customer.location.address)
                CustomerDetailItem("Item Type", customer.itemType)
                CustomerDetailItem("Weight", "${customer.itemWeight} ${customer.weightUnit}")
                CustomerDetailItem("Priority", customer.priority.name)
                CustomerDetailItem("Status", customer.status.name.replace("_", " "))
                if (customer.notes.isNotEmpty()) {
                    CustomerDetailItem("Notes", customer.notes)
                }
                CustomerDetailItem("Added", customer.createdAt.toDateString("dd MMM yyyy HH:mm"))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }

                if (onAccept != null) {
                    Button(onClick = {
                        onAccept()
                        onDismiss()
                    }) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept Task")
                    }
                }
            }
        }
    )
}

@Composable
private fun CustomerDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}