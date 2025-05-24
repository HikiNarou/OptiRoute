package com.optiroute.com.ui.screens.depot

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
// import com.google.android.gms.tasks.CancellationTokenSource // Tidak selalu diperlukan jika menggunakan await
import com.optiroute.com.R
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.ui.navigation.AppScreens
import com.optiroute.com.ui.theme.spacing
import com.optiroute.com.utils.hasLocationPermission
import com.optiroute.com.utils.openAppSettings
import com.optiroute.com.utils.PermissionRationaleDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Composable
fun DepotScreen(
    navController: NavController,
    viewModel: DepotViewModel = hiltViewModel()
) {
    val context = LocalContext.current // Digunakan untuk getString di dalam LaunchedEffect
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val depotUiState by viewModel.depotState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    // Mengambil string resource di lingkup Composable untuk digunakan dalam callback
    val permissionDeniedMessage = stringResource(id = R.string.location_permission_denied_message)
    val permissionGrantedMessage = stringResource(id = R.string.success) + ": " + stringResource(id = R.string.location_permission_rationale_title) // Sesuaikan jika ini judul, atau buat string baru untuk "izin diberikan"


    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Timber.d("Location permission granted.")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(permissionGrantedMessage)
                }
                getCurrentLocation(context) { latLng -> // Pastikan getCurrentLocation aman dari thread
                    viewModel.onLocationSelected(latLng)
                }
            } else {
                Timber.w("Location permission denied.")
                showPermissionRationaleDialog = true
            }
        }
    )

    LaunchedEffect(navController, lifecycleOwner) {
        navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Double>(AppScreens.SelectLocationMap.RESULT_LAT)
                .observe(lifecycleOwner) { lat ->
                    savedStateHandle.getLiveData<Double>(AppScreens.SelectLocationMap.RESULT_LNG)
                        .observe(lifecycleOwner) { lng ->
                            if (lat != null && lng != null) {
                                Timber.d("Received location from map: Lat=$lat, Lng=$lng")
                                viewModel.onLocationSelected(LatLng(lat, lng))
                                savedStateHandle.remove<Double>(AppScreens.SelectLocationMap.RESULT_LAT)
                                savedStateHandle.remove<Double>(AppScreens.SelectLocationMap.RESULT_LNG)
                            }
                        }
                }
        }
    }

    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is DepotUiEvent.ShowSnackbar -> {
                        // Menggunakan context yang sudah di-capture dari lingkup Composable
                        val message = event.messageText ?: event.messageResId?.let { context.getString(it) } ?: ""
                        if (message.isNotBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    }
                    is DepotUiEvent.RequestLocationPermission -> {
                        if (!context.hasLocationPermission()) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            Timber.d("Location permission already granted when requested by VM.")
                            getCurrentLocation(context) { latLng ->
                                viewModel.onLocationSelected(latLng)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPermissionRationaleDialog) {
        PermissionRationaleDialog(
            title = stringResource(id = R.string.location_permission_rationale_title),
            message = stringResource(id = R.string.location_permission_rationale_message),
            onConfirm = {
                showPermissionRationaleDialog = false
                context.openAppSettings()
            },
            onDismiss = {
                showPermissionRationaleDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(permissionDeniedMessage)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = depotUiState) {
                is DepotUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is DepotUiState.Error -> {
                    Text(
                        stringResource(id = state.messageResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium)
                    )
                }
                is DepotUiState.Success, DepotUiState.Empty -> {
                    DepotInputForm(
                        formState = formState,
                        onNameChange = viewModel::onNameChange,
                        onAddressChange = viewModel::onAddressChange,
                        onNotesChange = viewModel::onNotesChange,
                        onSaveClick = viewModel::saveDepot,
                        onSelectOnMapClick = {
                            val currentLoc = formState.selectedLocation
                            navController.navigate(
                                AppScreens.SelectLocationMap.createRoute(currentLoc?.latitude, currentLoc?.longitude)
                            )
                        },
                        onUseCurrentLocationClick = {
                            if (context.hasLocationPermission()) {
                                getCurrentLocation(context) { latLng ->
                                    viewModel.onLocationSelected(latLng)
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepotInputForm(
    formState: DepotFormState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onSelectOnMapClick: () -> Unit,
    onUseCurrentLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        if (formState.isSaving) {
            CircularProgressIndicator()
        }

        OutlinedTextField(
            value = formState.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.depot_name_label)) },
            placeholder = { Text(stringResource(R.string.depot_name_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { errorResId -> { Text(stringResource(errorResId)) } }
        )

        Text(
            text = stringResource(R.string.depot_location_label),
            style = MaterialTheme.typography.titleMedium
        )
        formState.selectedLocation?.let {
            Text(
                text = "Lat: ${"%.6f".format(it.latitude)}, Lng: ${"%.6f".format(it.longitude)}",
                style = MaterialTheme.typography.bodyMedium
            )
        } ?: Text(
            text = stringResource(R.string.not_set_yet),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val locationErrorResource = formState.locationError // Variabel lokal untuk smart cast
        if (locationErrorResource != null) {
            Text(
                text = stringResource(id = locationErrorResource),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Button(
                onClick = onSelectOnMapClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = stringResource(R.string.select_location_on_map))
                Spacer(Modifier.width(MaterialTheme.spacing.small))
                Text(stringResource(R.string.select_location_on_map))
            }
            Button(
                onClick = onUseCurrentLocationClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.use_current_location))
                Spacer(Modifier.width(MaterialTheme.spacing.small))
                Text(stringResource(R.string.use_current_location))
            }
        }

        OutlinedTextField(
            value = formState.address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.address)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
        )

        OutlinedTextField(
            value = formState.notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.notes)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Button(
            onClick = onSaveClick,
            enabled = !formState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

private fun getCurrentLocation(context: android.content.Context, onLocationFetched: (LatLng) -> Unit) {
    if (!context.hasLocationPermission()) {
        Timber.w("Attempted to get current location without permission.")
        return
    }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null // CancellationToken bisa null jika tidak ada operasi pembatalan spesifik
        ).addOnSuccessListener { location ->
            if (location != null) {
                Timber.d("Current location fetched: Lat=${location.latitude}, Lng=${location.longitude}")
                onLocationFetched(LatLng(location.latitude, location.longitude))
            } else {
                Timber.w("Failed to get current location, location is null.")
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error getting current location.")
        }
    } catch (e: SecurityException) {
        Timber.e(e, "SecurityException in getCurrentLocation (DepotScreen)")
    }
}
