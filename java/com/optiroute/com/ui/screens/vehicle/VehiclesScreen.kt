package com.optiroute.com.ui.screens.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.ui.theme.spacing
import com.optiroute.com.utils.ConfirmationDialog
import timber.log.Timber

@Composable
fun VehiclesScreen(
    navController: NavController,
    viewModel: VehicleViewModel = hiltViewModel(),
    onNavigateToAddEditVehicle: (Int?) -> Unit // Int? untuk vehicleId, null jika tambah baru
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val vehiclesListState by viewModel.vehiclesListState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf<VehicleEntity?>(null) }

    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is VehicleUiEvent.ShowSnackbar -> {
                        val message = event.messageText ?: event.messageResId?.let {
                            if (event.args != null) stringResource(id = it, formatArgs = event.args)
                            else stringResource(id = it)
                        } ?: ""
                        if (message.isNotBlank()) {
                            snackbarHostState.showSnackbar(message)
                        }
                        Timber.d("Snackbar event: $message")
                    }
                    is VehicleUiEvent.NavigateBack -> {
                        // Tidak relevan untuk layar daftar, lebih untuk form
                    }
                }
            }
        }
    }

    if (showDeleteConfirmationDialog != null) {
        ConfirmationDialog(
            title = stringResource(id = R.string.delete_confirmation_title),
            message = stringResource(id = R.string.confirm_delete_vehicle_message, showDeleteConfirmationDialog!!.name),
            onConfirm = {
                viewModel.deleteVehicle(showDeleteConfirmationDialog!!)
                showDeleteConfirmationDialog = null
            },
            onDismiss = {
                showDeleteConfirmationDialog = null
            },
            confirmButtonText = stringResource(id = R.string.delete),
            dismissButtonText = stringResource(id = R.string.cancel)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.prepareNewVehicleForm() // Pastikan form direset sebelum navigasi
                    onNavigateToAddEditVehicle(null) // null untuk mode tambah
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_vehicle_title))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = vehiclesListState) {
                is VehicleListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is VehicleListUiState.Error -> {
                    Text(
                        text = stringResource(id = state.messageResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(MaterialTheme.spacing.medium),
                        textAlign = TextAlign.Center
                    )
                }
                is VehicleListUiState.Empty -> {
                    Text(
                        text = stringResource(id = R.string.vehicle_list_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(MaterialTheme.spacing.medium),
                        textAlign = TextAlign.Center
                    )
                }
                is VehicleListUiState.Success -> {
                    if (state.vehicles.isEmpty()) { // Dobel cek, seharusnya sudah ditangani oleh state Empty
                        Text(
                            text = stringResource(id = R.string.vehicle_list_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(MaterialTheme.spacing.medium),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(MaterialTheme.spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            items(state.vehicles, key = { it.id }) { vehicle ->
                                VehicleItem(
                                    vehicle = vehicle,
                                    onEditClick = {
                                        onNavigateToAddEditVehicle(vehicle.id)
                                    },
                                    onDeleteClick = {
                                        showDeleteConfirmationDialog = vehicle
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleItem(
    vehicle: VehicleEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }, // Klik item untuk mengedit
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Filled.LocalShipping,
                    contentDescription = stringResource(R.string.content_description_vehicle_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                Column {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.vehicle_capacity_label
                        ) + ": ${vehicle.capacity} ${vehicle.capacityUnit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!vehicle.notes.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.notes) + ": ${vehicle.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

