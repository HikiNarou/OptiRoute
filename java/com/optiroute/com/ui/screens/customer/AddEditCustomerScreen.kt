package com.optiroute.com.ui.screens.customer

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType // Pastikan impor ini ada
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
// import com.google.android.gms.tasks.CancellationTokenSource // Tidak digunakan secara eksplisit di versi ini, await menangani pembatalan
import com.optiroute.com.R
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.ui.navigation.AppScreens
import com.optiroute.com.ui.theme.spacing
import com.optiroute.com.utils.PermissionRationaleDialog
import com.optiroute.com.utils.hasLocationPermission
import com.optiroute.com.utils.openAppSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    navController: NavController,
    viewModel: CustomerViewModel = hiltViewModel(),
    customerId: Int?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val formState by viewModel.customerFormState.collectAsState()
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val permissionDeniedMessage = stringResource(id = R.string.location_permission_denied_message)
    val permissionGrantedMessage = stringResource(id = R.string.success) + ": Izin lokasi diberikan."


    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Timber.d("Location permission granted for customer screen.")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(permissionGrantedMessage)
                }
                getCurrentCustomerLocation(context) { latLng ->
                    viewModel.onLocationSelected(latLng)
                }
            } else {
                Timber.w("Location permission denied for customer screen.")
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
                                Timber.d("Received location from map for customer: Lat=$lat, Lng=$lng")
                                viewModel.onLocationSelected(LatLng(lat, lng))
                                savedStateHandle.remove<Double>(AppScreens.SelectLocationMap.RESULT_LAT)
                                savedStateHandle.remove<Double>(AppScreens.SelectLocationMap.RESULT_LNG)
                            }
                        }
                }
        }
    }


    LaunchedEffect(customerId) {
        if (customerId != null) {
            Timber.d("AddEditCustomerScreen: Edit mode for customer ID $customerId")
            viewModel.loadCustomerForEditing(customerId)
        } else {
            Timber.d("AddEditCustomerScreen: Add mode")
            viewModel.prepareNewCustomerForm()
        }
    }

    LaunchedEffect(lifecycleOwner.lifecycle) { // Hapus Unit agar re-subscribe jika lifecycle berubah signifikan
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is CustomerUiEvent.ShowSnackbar -> {
                        val message = event.messageText ?: event.messageResId?.let { context.getString(it) } ?: ""
                        if (message.isNotBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    }
                    is CustomerUiEvent.NavigateBack -> {
                        navController.popBackStack()
                    }
                    is CustomerUiEvent.RequestLocationPermission -> {
                        if (!context.hasLocationPermission()) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            Timber.d("Location permission already granted when requested by CustomerVM.")
                            getCurrentCustomerLocation(context) { latLng ->
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            id = if (formState.isEditMode) R.string.edit_customer_title else R.string.add_customer_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            if (formState.isSaving) {
                CircularProgressIndicator()
            }

            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.customer_name_label)) },
                placeholder = { Text(stringResource(R.string.customer_name_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                isError = formState.nameError != null,
                supportingText = formState.nameError?.let { errorResId -> { Text(stringResource(errorResId)) } }
            )

            OutlinedTextField(
                value = formState.address,
                onValueChange = viewModel::onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.address)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
            )

            OutlinedTextField(
                value = formState.demand,
                onValueChange = viewModel::onDemandChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.customer_demand_label)) },
                placeholder = { Text(stringResource(R.string.customer_demand_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, // PERBAIKAN
                    imeAction = ImeAction.Next
                ),
                isError = formState.demandError != null,
                supportingText = {
                    Column {
                        formState.demandError?.let { errorResId -> Text(stringResource(errorResId), color = MaterialTheme.colorScheme.error) }
                        Text(stringResource(R.string.customer_demand_unit_explanation), style = MaterialTheme.typography.bodySmall)
                    }
                }
            )

            Text(
                text = stringResource(R.string.customer_location_label),
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
            // PERBAIKAN untuk smart cast
            val locationErrorRes = formState.locationError
            if (locationErrorRes != null) {
                Text(
                    text = stringResource(id = locationErrorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Button(
                    onClick = {
                        val currentLoc = formState.selectedLocation
                        navController.navigate(
                            AppScreens.SelectLocationMap.createRoute(currentLoc?.latitude, currentLoc?.longitude)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = stringResource(R.string.select_location_on_map))
                    Spacer(Modifier.width(MaterialTheme.spacing.small))
                    Text(stringResource(R.string.select_location_on_map))
                }
                Button(
                    onClick = {
                        if (context.hasLocationPermission()) {
                            getCurrentCustomerLocation(context) { latLng ->
                                viewModel.onLocationSelected(latLng)
                            }
                        } else {
                            viewModel.requestLocationPermission()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.use_current_location))
                    Spacer(Modifier.width(MaterialTheme.spacing.small))
                    Text(stringResource(R.string.use_current_location))
                }
            }


            OutlinedTextField(
                value = formState.notes,
                onValueChange = viewModel::onNotesChange,
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
                onClick = viewModel::saveCustomer,
                enabled = !formState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

private fun getCurrentCustomerLocation(context: android.content.Context, onLocationFetched: (LatLng) -> Unit) {
    if (!context.hasLocationPermission()) {
        Timber.w("Attempted to get current location for customer without permission.")
        return
    }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    // val cancellationTokenSource = CancellationTokenSource() // Tidak selalu diperlukan jika menggunakan await

    try {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null // CancellationToken bisa null jika tidak ada operasi pembatalan spesifik
        ).addOnSuccessListener { location ->
            if (location != null) {
                Timber.d("Current location fetched for customer: Lat=${location.latitude}, Lng=${location.longitude}")
                onLocationFetched(LatLng(location.latitude, location.longitude))
            } else {
                Timber.w("Failed to get current location for customer, location is null.")
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error getting current location for customer.")
        }
    } catch (e: SecurityException) {
        Timber.e(e, "SecurityException in getCurrentCustomerLocation")
    }
}
