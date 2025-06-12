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
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.utils.toDateString

@Composable
fun DepotCard(
    depot: Depot,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Text(
                        text = depot.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = depot.location.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusChip(
                    text = if (depot.isActive) "Active" else "Inactive",
                    isActive = depot.isActive
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Depot Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DepotInfoItem(
                    icon = Icons.Default.Scale,
                    label = "Capacity",
                    value = "${depot.capacity} tons",
                    modifier = Modifier.weight(1f)
                )

                DepotInfoItem(
                    icon = Icons.Default.AccessTime,
                    label = "Hours",
                    value = depot.operationalHours,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinates
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Latitude",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.6f".format(depot.location.latitude),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "Longitude",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.6f".format(depot.location.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Created: ${depot.createdAt.toDateString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Depot",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Depot",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DepotInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
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
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusChip(
    text: String,
    isActive: Boolean
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}