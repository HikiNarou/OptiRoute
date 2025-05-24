package com.optiroute.com.ui.screens.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.optiroute.com.R
import com.optiroute.com.ui.theme.spacing
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleScreen(
    navController: NavController,
    viewModel: VehicleViewModel = hiltViewModel(),
    vehicleId: Int? // Null jika mode tambah, non-null jika mode edit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val formState by viewModel.vehicleFormState.collectAsState()

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            Timber.d("AddEditVehicleScreen: Edit mode for vehicle ID $vehicleId")
            viewModel.loadVehicleForEditing(vehicleId)
        } else {
            Timber.d("AddEditVehicleScreen: Add mode")
            viewModel.prepareNewVehicleForm()
        }
    }

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
                    }
                    is VehicleUiEvent.NavigateBack -> {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            id = if (formState.isEditMode) R.string.edit_vehicle_title else R.string.add_vehicle_title
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
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Sedikit elevasi
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
                label = { Text(stringResource(R.string.vehicle_name_label)) },
                placeholder = { Text(stringResource(R.string.vehicle_name_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                isError = formState.nameError != null,
                supportingText = formState.nameError?.let { { Text(stringResource(it)) } }
            )

            OutlinedTextField(
                value = formState.capacity,
                onValueChange = viewModel::onCapacityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vehicle_capacity_label)) },
                placeholder = { Text(stringResource(R.string.vehicle_capacity_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberDecimal,
                    imeAction = ImeAction.Next
                ),
                isError = formState.capacityError != null,
                supportingText = formState.capacityError?.let { { Text(stringResource(it)) } }
            )

            OutlinedTextField(
                value = formState.capacityUnit,
                onValueChange = viewModel::onCapacityUnitChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vehicle_capacity_unit_label)) },
                placeholder = { Text(stringResource(R.string.vehicle_capacity_unit_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words, // Atau None jika ingin bebas
                    imeAction = ImeAction.Next
                ),
                isError = formState.capacityUnitError != null,
                supportingText = formState.capacityUnitError?.let { { Text(stringResource(it)) } }
            )

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
                onClick = viewModel::saveVehicle,
                enabled = !formState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
