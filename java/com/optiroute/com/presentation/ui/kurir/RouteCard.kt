package com.optiroute.com.presentation.ui.kurir

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.Route
import com.optiroute.com.domain.models.RouteStatus
import com.optiroute.com.utils.toDateString
import com.optiroute.com.utils.toFormattedDistance
import com.optiroute.com.utils.toFormattedDuration

data class RouteStatusInfo(
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val text: String,
    val icon: ImageVector
)

@Composable
fun RouteCard(
    route: Route,
    onStartRoute: (String) -> Unit,
    onViewOnMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (route.status) {
                RouteStatus.PLANNED -> MaterialTheme.colorScheme.primaryContainer
                RouteStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
                RouteStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
                RouteStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
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
                        text = "Route #${route.id.take(8)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (route.status) {
                            RouteStatus.PLANNED -> MaterialTheme.colorScheme.onPrimaryContainer
                            RouteStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onTertiaryContainer
                            RouteStatus.COMPLETED -> MaterialTheme.colorScheme.onSecondaryContainer
                            RouteStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )

                    Text(
                        text = "Created: ${route.createdAt.toDateString("dd MMM yyyy HH:mm")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RouteStatusChip(status = route.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route Statistics
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RouteStatItem(
                        icon = Icons.Default.People,
                        label = "Stops",
                        value = "${route.customerIds.size}",
                        color = MaterialTheme.colorScheme.primary
                    )

                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    RouteStatItem(
                        icon = Icons.Default.Route,
                        label = "Distance",
                        value = route.totalDistance.toFormattedDistance(),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    RouteStatItem(
                        icon = Icons.Default.AccessTime,
                        label = "Duration",
                        value = route.estimatedDuration.toFormattedDuration(),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    RouteStatItem(
                        icon = Icons.Default.Scale,
                        label = "Weight",
                        value = "${route.totalWeight} kg",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route Progress (if in progress)
            if (route.status == RouteStatus.IN_PROGRESS && route.startedAt > 0) {
                RouteProgressSection(route = route)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewOnMap,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "View Map")
                }

                when (route.status) {
                    RouteStatus.PLANNED -> {
                        Button(
                            onClick = { showStartConfirmDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Start Route")
                        }
                    }
                    RouteStatus.IN_PROGRESS -> {
                        Button(
                            onClick = onViewOnMap,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Navigate")
                        }
                    }
                    RouteStatus.COMPLETED -> {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Completed")
                        }
                    }
                    RouteStatus.CANCELLED -> {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Cancelled")
                        }
                    }
                }
            }
        }
    }

    // Start Route Confirmation Dialog
    if (showStartConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showStartConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Start Delivery Route",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you ready to start this delivery route?",
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
                                text = "Route Details:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• ${route.customerIds.size} delivery stops",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "• ${route.totalDistance.toFormattedDistance()} total distance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "• ${route.estimatedDuration.toFormattedDuration()} estimated time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "• ${route.totalWeight} kg total weight",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Make sure your vehicle is ready and you have all necessary equipment for deliveries.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStartRoute(route.id)
                        showStartConfirmDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Start Route")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showStartConfirmDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun RouteStatusChip(status: RouteStatus) {
    val statusInfo = when (status) {
        RouteStatus.PLANNED -> RouteStatusInfo(
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            text = "Planned",
            icon = Icons.Default.Schedule
        )
        RouteStatus.IN_PROGRESS -> RouteStatusInfo(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            text = "In Progress",
            icon = Icons.Default.DirectionsRun
        )
        RouteStatus.COMPLETED -> RouteStatusInfo(
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            text = "Completed",
            icon = Icons.Default.CheckCircle
        )
        RouteStatus.CANCELLED -> RouteStatusInfo(
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            text = "Cancelled",
            icon = Icons.Default.Cancel
        )
    }

    Surface(
        color = statusInfo.backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusInfo.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = statusInfo.contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = statusInfo.text,
                style = MaterialTheme.typography.labelSmall,
                color = statusInfo.contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RouteStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RouteProgressSection(route: Route) {
    val currentTime = System.currentTimeMillis()
    val elapsedTime = currentTime - route.startedAt
    val progress = if (route.estimatedDuration > 0) {
        (elapsedTime.toFloat() / route.estimatedDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Route Progress",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Started: ${route.startedAt.toDateString("HH:mm")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = "Elapsed: ${elapsedTime.toFormattedDuration()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}