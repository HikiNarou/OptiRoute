package com.optiroute.com.presentation.ui.maps

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.PatternItem
import com.google.maps.android.compose.*
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Route
import com.optiroute.com.domain.models.RouteStatus
import com.optiroute.com.presentation.viewmodel.MapViewModel
import com.optiroute.com.utils.LocationUtils

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    route: Route?,
    onNavigateBack: () -> Unit,
    onStartNavigation: (() -> Unit)? = null,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val mapState by mapViewModel.mapState.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()

    // Default location (Jakarta)
    val defaultLocation = LatLng(-6.175392, 106.827153)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // State for map controls
    var showMapType by remember { mutableStateOf(false) }
    var currentMapType by remember { mutableStateOf(MapType.NORMAL) }
    var showTrafficLayer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            mapViewModel.getCurrentLocation(context)
        }
        route?.let { mapViewModel.loadRouteData(it) }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            val newPosition = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15f
            )
            cameraPositionState.position = newPosition
        }
    }

    LaunchedEffect(route) {
        route?.let { routeData ->
            if (routeData.optimizedPath.isNotEmpty()) {
                val bounds = calculateBounds(routeData.optimizedPath)
                bounds?.let {
                    val centerLat = (bounds.first.latitude + bounds.second.latitude) / 2
                    val centerLng = (bounds.first.longitude + bounds.second.longitude) / 2
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(centerLat, centerLng),
                        12f
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Route Navigation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        route?.let {
                            Text(
                                text = "${it.customerIds.size} stops • ${LocationUtils.formatDistance(it.totalDistance)}",
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
                    IconButton(onClick = { showMapType = !showMapType }) {
                        Icon(Icons.Default.Layers, contentDescription = "Map Type")
                    }

                    IconButton(
                        onClick = { showTrafficLayer = !showTrafficLayer }
                    ) {
                        Icon(
                            Icons.Default.Traffic,
                            contentDescription = "Traffic",
                            tint = if (showTrafficLayer) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    if (onStartNavigation != null && route != null && route.status == RouteStatus.PLANNED) {
                        IconButton(onClick = onStartNavigation) {
                            Icon(Icons.Default.Navigation, contentDescription = "Start Navigation")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!locationPermissionState.status.isGranted) {
                LocationPermissionScreen(
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                )
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = currentMapType,
                        isTrafficEnabled = showTrafficLayer
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = true,
                        compassEnabled = true,
                        rotationGesturesEnabled = true,
                        scrollGesturesEnabled = true,
                        tiltGesturesEnabled = true,
                        zoomGesturesEnabled = true
                    )
                ) {
                    currentLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                            title = "Your Location",
                            snippet = "Current position",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }

                    route?.let { routeData ->
                        RouteMapContent(
                            route = routeData,
                            currentLocation = currentLocation
                        )
                    }
                }

                if (showMapType) {
                    MapTypeSelector(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .zIndex(1f),
                        currentMapType = currentMapType,
                        onMapTypeSelected = {
                            currentMapType = it
                            showMapType = false
                        },
                        onDismiss = { showMapType = false }
                    )
                }

                MapControlsFAB(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onMyLocationClicked = {
                        if (locationPermissionState.status.isGranted) {
                            mapViewModel.getCurrentLocation(context)
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    },
                    onZoomToRoute = {
                        route?.optimizedPath?.let { path ->
                            if (path.isNotEmpty()) {
                                val bounds = calculateBounds(path)
                                bounds?.let {
                                    val centerLat = (it.first.latitude + it.second.latitude) / 2
                                    val centerLng = (it.first.longitude + it.second.longitude) / 2
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        LatLng(centerLat, centerLng),
                                        12f
                                    )
                                }
                            }
                        }
                    }
                )

                route?.let { routeData ->
                    RouteInfoBottomSheet(
                        modifier = Modifier.align(Alignment.BottomStart),
                        route = routeData,
                        onNavigateToStop = { location ->
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(location.latitude, location.longitude),
                                17f
                            )
                        }
                    )
                }

                if (mapState is MapViewModel.MapState.Loading) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Loading map data...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPermissionScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To show your location on the map and provide navigation, we need access to your location.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grant Location Permission")
        }
    }
}

@Composable
private fun RouteMapContent(
    route: Route,
    currentLocation: Location?
) {
    val optimizedPath = route.optimizedPath

    if (optimizedPath.isNotEmpty()) {
        val polylinePoints = optimizedPath.map { LatLng(it.latitude, it.longitude) }

        // Create pattern for dashed line
        val pattern: List<PatternItem>? = if (route.status == RouteStatus.PLANNED) {
            listOf(Dash(20f), Gap(10f))
        } else {
            null
        }

        Polyline(
            points = polylinePoints,
            color = when (route.status) {
                RouteStatus.PLANNED -> androidx.compose.ui.graphics.Color.Blue
                RouteStatus.IN_PROGRESS -> androidx.compose.ui.graphics.Color.Green
                RouteStatus.COMPLETED -> androidx.compose.ui.graphics.Color.Gray
                RouteStatus.CANCELLED -> androidx.compose.ui.graphics.Color.Red
            },
            width = 8f,
            pattern = pattern
        )

        optimizedPath.forEachIndexed { index, location ->
            val isDepot = index == 0 || index == optimizedPath.size - 1
            val isCurrentLocation = currentLocation?.let { current ->
                LocationUtils.calculateDistance(
                    current.latitude, current.longitude,
                    location.latitude, location.longitude
                ) < 0.1
            } ?: false

            Marker(
                state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                title = when {
                    isDepot && index == 0 -> "Start: ${location.address}"
                    isDepot && index == optimizedPath.size - 1 -> "End: ${location.address}"
                    else -> "Stop ${index}: ${location.address}"
                },
                snippet = when {
                    isCurrentLocation -> "Current location"
                    isDepot -> "Depot location"
                    else -> "Delivery stop"
                },
                icon = BitmapDescriptorFactory.defaultMarker(
                    when {
                        isCurrentLocation -> BitmapDescriptorFactory.HUE_GREEN
                        isDepot -> BitmapDescriptorFactory.HUE_RED
                        else -> BitmapDescriptorFactory.HUE_BLUE
                    }
                )
            )
        }
    }
}

@Composable
private fun MapTypeSelector(
    modifier: Modifier = Modifier,
    currentMapType: MapType,
    onMapTypeSelected: (MapType) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Map Type",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            val mapTypes = listOf(
                Pair(MapType.NORMAL, "Normal"),
                Pair(MapType.SATELLITE, "Satellite"),
                Pair(MapType.TERRAIN, "Terrain"),
                Pair(MapType.HYBRID, "Hybrid")
            )

            mapTypes.forEach { (type, name) ->
                TextButton(
                    onClick = { onMapTypeSelected(type) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (type == currentMapType) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name)
                        if (type == currentMapType) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapControlsFAB(
    modifier: Modifier = Modifier,
    onMyLocationClicked: () -> Unit,
    onZoomToRoute: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallFloatingActionButton(
            onClick = onZoomToRoute,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                Icons.Default.CenterFocusStrong,
                contentDescription = "Zoom to Route"
            )
        }

        SmallFloatingActionButton(
            onClick = onMyLocationClicked,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "My Location"
            )
        }
    }
}

@Composable
private fun RouteInfoBottomSheet(
    modifier: Modifier = Modifier,
    route: Route,
    onNavigateToStop: (Location) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, bottom = 16.dp, end = 80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Route Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = when (route.status) {
                        RouteStatus.PLANNED -> MaterialTheme.colorScheme.secondaryContainer
                        RouteStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                        RouteStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        RouteStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = route.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when (route.status) {
                            RouteStatus.PLANNED -> MaterialTheme.colorScheme.onSecondaryContainer
                            RouteStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                            RouteStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimary
                            RouteStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RouteInfoItem(
                    icon = Icons.Default.Route,
                    label = "Distance",
                    value = LocationUtils.formatDistance(route.totalDistance)
                )

                RouteInfoItem(
                    icon = Icons.Default.AccessTime,
                    label = "Duration",
                    value = LocationUtils.formatDuration(route.estimatedDuration)
                )

                RouteInfoItem(
                    icon = Icons.Default.People,
                    label = "Stops",
                    value = "${route.customerIds.size}"
                )

                RouteInfoItem(
                    icon = Icons.Default.Scale,
                    label = "Weight",
                    value = "${route.totalWeight} kg"
                )
            }

            if (route.optimizedPath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            route.optimizedPath.firstOrNull()?.let { firstLocation ->
                                onNavigateToStop(firstLocation)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }

                    OutlinedButton(
                        onClick = {
                            route.optimizedPath.lastOrNull()?.let { lastLocation ->
                                onNavigateToStop(lastLocation)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FlagCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("End")
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
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

// Helper function to calculate bounds
private fun calculateBounds(locations: List<Location>): Pair<Location, Location>? {
    if (locations.isEmpty()) return null

    var minLat = locations.first().latitude
    var maxLat = locations.first().latitude
    var minLng = locations.first().longitude
    var maxLng = locations.first().longitude

    locations.forEach { location ->
        minLat = minOf(minLat, location.latitude)
        maxLat = maxOf(maxLat, location.latitude)
        minLng = minOf(minLng, location.longitude)
        maxLng = maxOf(maxLng, location.longitude)
    }

    return Pair(
        Location(minLat, minLng, "Southwest"),
        Location(maxLat, maxLng, "Northeast")
    )
}