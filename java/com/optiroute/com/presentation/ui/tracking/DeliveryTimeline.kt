package com.optiroute.com.presentation.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.optiroute.com.domain.models.DeliveryStatusUpdate
import com.optiroute.com.domain.models.DeliveryTrackingStatus
import com.optiroute.com.utils.toDateString

@Composable
fun DeliveryTimeline(
    statusHistory: List<DeliveryStatusUpdate>,
    currentStatus: DeliveryTrackingStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Delivery Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (statusHistory.isEmpty()) {
                EmptyTimelineState()
            } else {
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(statusHistory.sortedByDescending { it.timestamp }) { index, statusUpdate ->
                        TimelineItem(
                            statusUpdate = statusUpdate,
                            isLatest = index == 0,
                            isLast = index == statusHistory.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTimelineState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Timeline,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No timeline data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimelineItem(
    statusUpdate: DeliveryStatusUpdate,
    isLatest: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isLatest) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            ) {
                Icon(
                    getIconForStatus(statusUpdate.status),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(6.dp),
                    tint = if (isLatest) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (!isLast) {
                Divider(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = getStatusDisplayName(statusUpdate.status),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                color = if (isLatest) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = statusUpdate.timestamp.toDateString("dd MMM yyyy, HH:mm"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (statusUpdate.location.address.isNotEmpty()) {
                Text(
                    text = "📍 ${statusUpdate.location.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (statusUpdate.notes.isNotEmpty()) {
                Text(
                    text = statusUpdate.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getIconForStatus(status: DeliveryTrackingStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        DeliveryTrackingStatus.PREPARING -> Icons.Default.Inventory
        DeliveryTrackingStatus.PICKED_UP -> Icons.Default.CheckCircle
        DeliveryTrackingStatus.IN_TRANSIT -> Icons.Default.DirectionsRun
        DeliveryTrackingStatus.NEAR_DESTINATION -> Icons.Default.NearMe
        DeliveryTrackingStatus.ARRIVED -> Icons.Default.Place
        DeliveryTrackingStatus.DELIVERED -> Icons.Default.CheckCircle
        DeliveryTrackingStatus.FAILED -> Icons.Default.Error
        DeliveryTrackingStatus.RETURNED -> Icons.Default.Undo
    }
}

private fun getStatusDisplayName(status: DeliveryTrackingStatus): String {
    return when (status) {
        DeliveryTrackingStatus.PREPARING -> "Preparing Package"
        DeliveryTrackingStatus.PICKED_UP -> "Package Picked Up"
        DeliveryTrackingStatus.IN_TRANSIT -> "In Transit"
        DeliveryTrackingStatus.NEAR_DESTINATION -> "Near Destination"
        DeliveryTrackingStatus.ARRIVED -> "Arrived at Destination"
        DeliveryTrackingStatus.DELIVERED -> "Package Delivered"
        DeliveryTrackingStatus.FAILED -> "Delivery Failed"
        DeliveryTrackingStatus.RETURNED -> "Package Returned"
    }
}