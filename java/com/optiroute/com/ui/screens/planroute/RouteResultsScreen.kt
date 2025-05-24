package com.optiroute.com.ui.screens.planroute

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border // Impor yang diperlukan
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.optiroute.com.R
import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.domain.vrp.RouteDetail
import com.optiroute.com.domain.vrp.VrpSolution
import com.optiroute.com.ui.screens.utils.bitmapDescriptorFromVector
import com.optiroute.com.ui.screens.utils.toGoogleLatLng
import com.optiroute.com.ui.theme.* // Impor semua dari theme, termasuk MapRouteColorX
import timber.log.Timber

// Daftar warna untuk rute di peta (sudah didefinisikan di ui.theme.Color.kt)
// Variabel ini akan mengambil nilai dari Color.kt karena wildcard import di atas
val routeDisplayColorsList = listOf(
    MapRouteColor1, MapRouteColor2, MapRouteColor3, MapRouteColor4, MapRouteColor5,
    MapRouteColor6, MapRouteColor7, MapRouteColor8, MapRouteColor9, MapRouteColor10
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RouteResultsScreen(
    navController: NavController,
    routePlanId: String?,
    planRouteViewModel: PlanRouteViewModel = hiltViewModel(navController.previousBackStackEntry!!)
) {
    val vrpSolutionState by planRouteViewModel.optimizationResult.collectAsState()
    val depotState by planRouteViewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.map_view) to Icons.Filled.Map,
        stringResource(R.string.list_view) to Icons.Filled.ListAlt
    )

    LaunchedEffect(routePlanId, vrpSolutionState) {
        if (vrpSolutionState != null && vrpSolutionState?.planId != routePlanId) {
            Timber.w("RouteResultsScreen: planId mismatch! Nav arg: $routePlanId, ViewModel has: ${vrpSolutionState?.planId}.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.route_results_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_navigate_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                tabs.forEachIndexed { index, tabInfo ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tabInfo.first) },
                        icon = { Icon(tabInfo.second, contentDescription = tabInfo.first) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val currentVrpSolution = vrpSolutionState
            val currentDepot = (depotState as? PlanRouteUiState.Success)?.depot

            if (currentVrpSolution == null || currentDepot == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val currentUiState = planRouteViewModel.uiState.value
                    if (currentUiState is PlanRouteUiState.Success && currentUiState.isOptimizing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.calculating_routes), modifier = Modifier.padding(top = MaterialTheme.spacing.small))
                        }
                    } else {
                        Text(stringResource(R.string.no_routes_generated), style = MaterialTheme.typography.titleMedium)
                    }
                }
                return@Scaffold
            }

            when (selectedTabIndex) {
                0 -> RouteResultsMapView(
                    vrpSolution = currentVrpSolution,
                    depot = currentDepot
                )
                1 -> RouteResultsListView(
                    vrpSolution = currentVrpSolution,
                    depot = currentDepot
                )
            }
        }
    }
}

@Composable
fun RouteResultsMapView(
    vrpSolution: VrpSolution,
    depot: com.optiroute.com.data.local.entity.DepotEntity
) {
    val context = LocalContext.current
    val depotLatLngGms = depot.location.toGoogleLatLng()

    val allPointsForBounds = remember(vrpSolution, depot) {
        mutableListOf(depotLatLngGms).apply {
            vrpSolution.routes.forEach { routeDetail ->
                routeDetail.stops.forEach { customer ->
                    add(customer.location.toGoogleLatLng())
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(depotLatLngGms, 12f)
    }

    LaunchedEffect(allPointsForBounds) {
        if (allPointsForBounds.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            allPointsForBounds.forEach { boundsBuilder.include(it) }
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100),
                    durationMs = 1000
                )
            } catch (e: IllegalStateException) {
                Timber.e(e, "Error animating camera to bounds.")
                if (allPointsForBounds.size == 1) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPointsForBounds.first(), 15f))
                }
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true), // Membutuhkan izin lokasi
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            mapToolbarEnabled = true,
            myLocationButtonEnabled = true
        )
    ) {
        Marker(
            state = MarkerState(position = depotLatLngGms),
            title = depot.name,
            snippet = stringResource(R.string.nav_depot),
            icon = bitmapDescriptorFromVector(context, R.drawable.ic_depot_pin)
        )

        vrpSolution.routes.forEachIndexed { routeIndex, routeDetail ->
            val routeColor = routeDisplayColorsList[routeIndex % routeDisplayColorsList.size]
            val polylinePoints = remember(routeDetail, depotLatLngGms) {
                mutableListOf<com.google.android.gms.maps.model.LatLng>().apply {
                    add(depotLatLngGms)
                    routeDetail.stops.forEach { customer -> add(customer.location.toGoogleLatLng()) }
                    add(depotLatLngGms)
                }
            }

            Polyline(points = polylinePoints, color = routeColor, width = 10f, zIndex = routeIndex.toFloat())

            routeDetail.stops.forEachIndexed { stopIndex, customer ->
                Marker(
                    state = MarkerState(position = customer.location.toGoogleLatLng()),
                    title = customer.name,
                    snippet = "Rute ${routeIndex + 1}, Stop ${stopIndex + 1} (Kendaraan: ${routeDetail.vehicle.name})",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE + (routeIndex * 25) % 360) // Variasi warna marker
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteResultsListView(
    vrpSolution: VrpSolution,
    depot: com.optiroute.com.data.local.entity.DepotEntity
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large) // Beri jarak lebih antar bagian
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(stringResource(R.string.route_results_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Divider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))
                    Text(
                        stringResource(R.string.total_distance_label) + " ${"%.2f".format(vrpSolution.totalOverallDistance)} km",
                        style = MaterialTheme.typography.titleMedium
                    )
                    vrpSolution.calculationTimeMillis?.let {
                        Text("Waktu Kalkulasi: ${it / 1000.0} detik", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Total Rute Dibuat: ${vrpSolution.routes.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (vrpSolution.unassignedCustomers.isNotEmpty()) {
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = stringResource(R.string.unassigned_customers) + " (${vrpSolution.unassignedCustomers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium)
                    )
                }
            }
            itemsIndexed(vrpSolution.unassignedCustomers, key = { _, cust -> "unassigned-${cust.id}"}) { _, customer ->
                UnassignedCustomerItem(customer, modifier = Modifier.padding(top = MaterialTheme.spacing.small))
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                ) {
                    Text(
                        stringResource(R.string.no_unassigned_customers),
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        vrpSolution.routes.forEachIndexed { index, routeDetail ->
            stickyHeader {
                Surface(
                    color = routeDisplayColorsList[index % routeDisplayColorsList.size].copy(alpha = 0.3f), // Menggunakan routeDisplayColorsList
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = stringResource(R.string.route_for_vehicle, routeDetail.vehicle.name) + " (Rute ${index + 1})",
                        style = MaterialTheme.typography.titleLarge, // Lebih besar
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface, // Warna teks yang kontras
                        modifier = Modifier.padding(MaterialTheme.spacing.medium)
                    )
                }
            }
            item {
                RouteDetailCard(routeDetail = routeDetail, depotName = depot.name, routeIndex = index)
                if (index < vrpSolution.routes.size - 1) { // Divider antar rute
                    Divider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))
                }
            }
        }

        if (vrpSolution.routes.isEmpty() && vrpSolution.unassignedCustomers.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_routes_generated), // Pesan jika tidak ada rute sama sekali
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UnassignedCustomerItem(customer: CustomerEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.WarningAmber, contentDescription = "Unassigned Customer", tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Column {
                Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Permintaan: ${customer.demand}", style = MaterialTheme.typography.bodyMedium)
                customer.address?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun RouteDetailCard(routeDetail: RouteDetail, depotName: String, routeIndex: Int) {
    ElevatedCard( // Menggunakan ElevatedCard untuk penekanan visual
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                "Kendaraan: ${routeDetail.vehicle.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Kapasitas Digunakan: ${"%.2f".format(routeDetail.totalDemand)} / ${routeDetail.vehicle.capacity} ${routeDetail.vehicle.capacityUnit}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.total_distance_label) + " ${"%.2f".format(routeDetail.totalDistance)} km",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Divider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))
            Text(stringResource(R.string.stops_label).uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            StopItemView(
                stopName = "$depotName (Depot - Mulai)",
                index = 0,
                isDepot = true,
                routeColor = routeDisplayColorsList[routeIndex % routeDisplayColorsList.size], // Menggunakan routeDisplayColorsList
                isFirst = true
            )

            routeDetail.stops.forEachIndexed { index, customer ->
                StopItemView(
                    stopName = customer.name,
                    demand = customer.demand,
                    address = customer.address,
                    index = index + 1,
                    routeColor = routeDisplayColorsList[routeIndex % routeDisplayColorsList.size] // Menggunakan routeDisplayColorsList
                )
            }
            StopItemView(
                stopName = "$depotName (Depot - Selesai)",
                index = routeDetail.stops.size + 1,
                isDepot = true,
                routeColor = routeDisplayColorsList[routeIndex % routeDisplayColorsList.size], // Menggunakan routeDisplayColorsList
                isLast = true
            )
        }
    }
}

@Composable
fun StopItemView(
    stopName: String,
    demand: Double? = null,
    address: String? = null,
    index: Int,
    isDepot: Boolean = false,
    routeColor: Color,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.extraSmall),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDepot) MaterialTheme.colorScheme.tertiary else routeColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape), // Perbaikan: Menambahkan impor border
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDepot) "D" else (index).toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDepot) MaterialTheme.colorScheme.onTertiary else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .height(if (address != null && demand != null) 48.dp else 24.dp)
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stopName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            address?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            demand?.let {
                Text(
                    text = "Permintaan: ${"%.1f".format(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
