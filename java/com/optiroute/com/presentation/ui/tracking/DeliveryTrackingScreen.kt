package com.optiroute.com.presentation.ui.tracking

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
import com.optiroute.com.domain.models.DeliveryTracking
import com.optiroute.com.domain.models.DeliveryTrackingStatus
import com.optiroute.com.domain.models.UserType
import com.optiroute.com.presentation.viewmodel.AuthViewModel
import com.optiroute.com.presentation.viewmodel.DeliveryTrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTrackingScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    trackingViewModel: DeliveryTrackingViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val deliveryTrackings by trackingViewModel.deliveryTrackings.collectAsState()
    val trackingState by trackingViewModel.trackingState.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            when (user.userType) {
                UserType.UMKM -> trackingViewModel.loadTrackingForUmkm(user.id)
                UserType.KURIR -> trackingViewModel.loadTrackingForKurir(user.id)
                UserType.ADMIN -> trackingViewModel.loadAllActiveDeliveries()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Delivery Tracking",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        currentUser?.let { user ->
                            Text(
                                text = when (user.userType) {
                                    UserType.UMKM -> "My Shipments"
                                    UserType.KURIR -> "My Deliveries"
                                    UserType.ADMIN -> "All Active Deliveries"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            currentUser?.let { user ->
                                when (user.userType) {
                                    UserType.UMKM -> trackingViewModel.loadTrackingForUmkm(user.id)
                                    UserType.KURIR -> trackingViewModel.loadTrackingForKurir(user.id)
                                    UserType.ADMIN -> trackingViewModel.loadAllActiveDeliveries()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            if (deliveryTrackings.isEmpty()) {
                item {
                    EmptyTrackingState(
                        userType = currentUser?.userType ?: UserType.UMKM
                    )
                }
            } else {
                items(deliveryTrackings) { tracking ->
                    DeliveryTrackingCard(
                        tracking = tracking,
                        userType = currentUser?.userType ?: UserType.UMKM,
                        onUpdateStatus = { trackingId, status ->
                            trackingViewModel.updateDeliveryStatus(trackingId, status)
                        },
                        onViewDetails = { trackingId ->
                            // Navigate to detailed tracking view
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTrackingState(userType: UserType) {
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
                Icons.Default.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (userType) {
                    UserType.UMKM -> "No shipments to track"
                    UserType.KURIR -> "No active deliveries"
                    UserType.ADMIN -> "No active deliveries in system"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (userType) {
                    UserType.UMKM -> "Your delivery tracking will appear here"
                    UserType.KURIR -> "Accept tasks to start tracking deliveries"
                    UserType.ADMIN -> "All active deliveries will be shown here"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeliveryTrackingCard(
    tracking: DeliveryTracking,
    userType: UserType,
    onUpdateStatus: (String, DeliveryTrackingStatus) -> Unit,
    onViewDetails: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Delivery #${tracking.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Customer: ${tracking.customerId.take(8)}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (userType == UserType.ADMIN) {
                        Text(
                            text = "Kurir: ${tracking.kurirId.take(8)}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DeliveryStatusChip(status = tracking.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Location
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current Location",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = tracking.currentLocation.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Timeline
            DeliveryTimeline(
                statusHistory = tracking.statusHistory,
                currentStatus = tracking.status
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onViewDetails(tracking.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details")
                }

                if (userType == UserType.KURIR && tracking.status in listOf(
                        DeliveryTrackingStatus.PICKED_UP,
                        DeliveryTrackingStatus.IN_TRANSIT,
                        DeliveryTrackingStatus.NEAR_DESTINATION
                    )) {
                    Button(
                        onClick = {
                            val nextStatus = when (tracking.status) {
                                DeliveryTrackingStatus.PICKED_UP -> DeliveryTrackingStatus.IN_TRANSIT
                                DeliveryTrackingStatus.IN_TRANSIT -> DeliveryTrackingStatus.NEAR_DESTINATION
                                DeliveryTrackingStatus.NEAR_DESTINATION -> DeliveryTrackingStatus.ARRIVED
                                else -> tracking.status
                            }
                            onUpdateStatus(tracking.id, nextStatus)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatusChip(status: DeliveryTrackingStatus) {
    val (backgroundColor, contentColor, text, icon) = when (status) {
        DeliveryTrackingStatus.PREPARING -> Quadruple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Preparing",
            Icons.Default.Inventory
        )
        DeliveryTrackingStatus.PICKED_UP -> Quadruple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Picked Up",
            Icons.Default.CheckCircle
        )
        DeliveryTrackingStatus.IN_TRANSIT -> Quadruple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "In Transit",
            Icons.Default.DirectionsRun
        )
        DeliveryTrackingStatus.NEAR_DESTINATION -> Quadruple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Near Destination",
            Icons.Default.NearMe
        )
        DeliveryTrackingStatus.ARRIVED -> Quadruple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Arrived",
            Icons.Default.Place
        )
        DeliveryTrackingStatus.DELIVERED -> Quadruple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            "Delivered",
            Icons.Default.CheckCircle
        )
        DeliveryTrackingStatus.FAILED -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Failed",
            Icons.Default.Error
        )
        DeliveryTrackingStatus.RETURNED -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Returned",
            Icons.Default.Undo
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
                color = contentColor
            )
        }
    }
}

// Helper data class for Quadruple
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)