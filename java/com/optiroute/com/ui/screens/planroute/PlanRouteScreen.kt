package com.optiroute.com.ui.screens.planroute

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.optiroute.com.R
import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.data.local.entity.DepotEntity
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.ui.navigation.AppScreens
import com.optiroute.com.ui.theme.spacing
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanRouteScreen(
    navController: NavController,
    viewModel: PlanRouteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val selectedCustomerIds by viewModel.selectedCustomerIds.collectAsState()
    val selectedVehicleIds by viewModel.selectedVehicleIds.collectAsState()
    val optimizationResult by viewModel.optimizationResult.collectAsState() // Untuk memantau hasil

    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is PlanRouteUiEvent.ShowSnackbar -> {
                        val message = event.messageText ?: event.messageResId?.let { stringResource(id = it) } ?: ""
                        if (message.isNotBlank()) {
                            snackbarHostState.showSnackbar(message)
                        }
                        Timber.d("PlanRoute Snackbar event: $message")
                    }
                    is PlanRouteUiEvent.NavigateToResults -> {
                        // Navigasi ke layar hasil dengan membawa ID rencana rute
                        // Data VrpSolution akan diambil dari ViewModel berdasarkan ID ini atau di-pass
                        // Untuk saat ini, kita akan mengandalkan ViewModel untuk menyimpan hasil sementara
                        Timber.d("Navigating to results screen with plan ID: ${event.planId}")
                        navController.navigate(AppScreens.RouteResultsScreen.createRoute(event.planId))
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val currentState = uiState
            if (currentState is PlanRouteUiState.Success && !currentState.isOptimizing) {
                Button(
                    onClick = { viewModel.optimizeRoutes() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    enabled = selectedCustomerIds.isNotEmpty() && selectedVehicleIds.isNotEmpty() && currentState.depot != null
                ) {
                    Icon(Icons.Filled.Route, contentDescription = null)
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(stringResource(R.string.optimize_routes_button))
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PlanRouteUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PlanRouteUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(MaterialTheme.spacing.medium),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        Text(
                            text = stringResource(id = state.messageResId),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.messageResId == R.string.no_depot_set_for_planning) {
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                            Button(onClick = { navController.navigate(AppScreens.Depot.route) }) {
                                Text(stringResource(R.string.set_depot_location))
                            }
                        }
                    }
                }
                is PlanRouteUiState.Success -> {
                    if (state.isOptimizing) {
                        OptimizationLoadingView()
                    } else {
                        PlanRouteContentView(
                            depot = state.depot,
                            allCustomers = state.allCustomers,
                            allVehicles = state.allVehicles,
                            selectedCustomerIds = selectedCustomerIds,
                            selectedVehicleIds = selectedVehicleIds,
                            onCustomerToggle = viewModel::toggleCustomerSelection,
                            onVehicleToggle = viewModel::toggleVehicleSelection,
                            onSelectAllCustomers = { viewModel.selectAllCustomers(state.allCustomers) },
                            onDeselectAllCustomers = viewModel::deselectAllCustomers,
                            onSelectAllVehicles = { viewModel.selectAllVehicles(state.allVehicles) },
                            onDeselectAllVehicles = viewModel::deselectAllVehicles
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimizationLoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Text(
            text = stringResource(R.string.calculating_routes),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlanRouteContentView(
    depot: DepotEntity?,
    allCustomers: List<CustomerEntity>,
    allVehicles: List<VehicleEntity>,
    selectedCustomerIds: Set<Int>,
    selectedVehicleIds: Set<Int>,
    onCustomerToggle: (Int) -> Unit,
    onVehicleToggle: (Int) -> Unit,
    onSelectAllCustomers: () -> Unit,
    onDeselectAllCustomers: () -> Unit,
    onSelectAllVehicles: () -> Unit,
    onDeselectAllVehicles: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        // Informasi Depot
        item {
            DepotInfoCard(depot)
        }

        // Pemilihan Pelanggan
        stickyHeader {
            ListSelectionHeader(
                title = stringResource(R.string.select_customers_for_delivery),
                selectedCount = selectedCustomerIds.size,
                totalCount = allCustomers.size,
                onSelectAll = onSelectAllCustomers,
                onDeselectAll = onDeselectAllCustomers,
                icon = Icons.Filled.People
            )
        }
        if (allCustomers.isEmpty()) {
            item { EmptyListItemInfo(message = stringResource(R.string.customer_list_empty)) }
        } else {
            items(allCustomers, key = { "customer-${it.id}" }) { customer ->
                SelectableCustomerItem(
                    customer = customer,
                    isSelected = selectedCustomerIds.contains(customer.id),
                    onToggle = { onCustomerToggle(customer.id) }
                )
            }
        }

        // Pemilihan Kendaraan
        stickyHeader {
            ListSelectionHeader(
                title = stringResource(R.string.select_vehicles_to_use),
                selectedCount = selectedVehicleIds.size,
                totalCount = allVehicles.size,
                onSelectAll = onSelectAllVehicles,
                onDeselectAll = onDeselectAllVehicles,
                icon = Icons.Filled.LocalShipping
            )
        }
        if (allVehicles.isEmpty()) {
            item { EmptyListItemInfo(message = stringResource(R.string.vehicle_list_empty)) } // Atau pesan "tidak ada kendaraan"
        } else {
            items(allVehicles, key = { "vehicle-${it.id}" }) { vehicle ->
                SelectableVehicleItem(
                    vehicle = vehicle,
                    isSelected = selectedVehicleIds.contains(vehicle.id),
                    onToggle = { onVehicleToggle(vehicle.id) }
                )
            }
        }

        // Spacer di akhir untuk memberi ruang bagi BottomBar
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun DepotInfoCard(depot: DepotEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                text = stringResource(R.string.nav_depot),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            if (depot != null) {
                Text("Nama: ${depot.name}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Lokasi: Lat ${"%.4f".format(depot.location.latitude)}, Lng ${"%.4f".format(depot.location.longitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                depot.address?.takeIf { it.isNotBlank() }?.let {
                    Text("Alamat: $it", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text(stringResource(R.string.depot_not_set_message), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ListSelectionHeader(
    title: String,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    icon: ImageVector
) {
    Surface( // Untuk efek sticky header dengan background
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.small, horizontal = MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = "$title ($selectedCount/$totalCount)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (totalCount > 0) {
                TextButton(onClick = if (selectedCount == totalCount) onDeselectAll else onSelectAll) {
                    Text(if (selectedCount == totalCount) stringResource(R.string.clear_selection) else stringResource(R.string.select_all))
                }
            }
        }
    }
}


@Composable
private fun SelectableCustomerItem(
    customer: CustomerEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Permintaan: ${customer.demand}", // Tambahkan satuan jika ada
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                customer.address?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SelectableVehicleItem(
    vehicle: VehicleEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Kapasitas: ${vehicle.capacity} ${vehicle.capacityUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyListItemInfo(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.medium)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp), RoundedCornerShape(8.dp))
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

