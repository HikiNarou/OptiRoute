package com.optiroute.com.ui.screens.utils

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.optiroute.com.R
import com.optiroute.com.domain.model.LatLng // Kustom LatLng
import com.optiroute.com.ui.theme.spacing
import com.optiroute.com.utils.hasLocationPermission
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Impor untuk await
import timber.log.Timber

private const val DEFAULT_MAP_ZOOM = 15f
private val FALLBACK_INITIAL_LOCATION = LatLng(-2.976074, 104.775429) // Palembang, Sumatra Selatan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLocationMapScreen(
    navController: NavController,
    initialLatLng: LatLng?,
    onLocationSelected: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State untuk lokasi yang dipilih di peta, diinisialisasi dengan initialLatLng atau fallback
    var currentSelectedOnMap by remember { mutableStateOf(initialLatLng ?: FALLBACK_INITIAL_LOCATION) }

    // State untuk Marker di peta
    val markerState = rememberMarkerState(position = currentSelectedOnMap.toGoogleLatLng())

    // State untuk CameraPosition
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentSelectedOnMap.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
    }

    // Mengupdate currentSelectedOnMap ketika posisi marker berubah karena di-drag
    LaunchedEffect(markerState.position) {
        val newPosition = markerState.position
        currentSelectedOnMap = LatLng(newPosition.latitude, newPosition.longitude)
    }

    // Meminta izin lokasi & update ke lokasi saat ini jika belum ada initialLatLng
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Timber.d("Location permission granted for map screen.")
                coroutineScope.launch {
                    try {
                        val locationResult = LocationServices.getFusedLocationProviderClient(context)
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                        locationResult?.let {
                            val deviceLatLng = LatLng(it.latitude, it.longitude)
                            currentSelectedOnMap = deviceLatLng
                            markerState.position = deviceLatLng.toGoogleLatLng() // Update posisi marker
                            cameraPositionState.animate( // Panggil animate dari coroutine
                                CameraUpdateFactory.newLatLngZoom(deviceLatLng.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
                            )
                        }
                    } catch (e: Exception) { Timber.e(e, "Error getting current location after permission grant") }
                }
            } else {
                Timber.w("Location permission denied for map screen.")
                // Tampilkan pesan atau fallback jika izin ditolak
            }
        }
    )

    // Efek untuk inisialisasi peta:
    // 1. Jika ada initialLatLng, pindah kamera ke sana.
    // 2. Jika tidak ada initialLatLng, coba dapatkan lokasi saat ini.
    // 3. Jika gagal dapatkan lokasi saat ini (atau izin ditolak), gunakan FALLBACK_INITIAL_LOCATION.
    LaunchedEffect(Unit) { // Hanya dijalankan sekali saat komposisi awal
        if (initialLatLng != null) {
            currentSelectedOnMap = initialLatLng
            markerState.position = initialLatLng.toGoogleLatLng()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(initialLatLng.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
            )
        } else {
            if (context.hasLocationPermission()) {
                try {
                    val locationResult = LocationServices.getFusedLocationProviderClient(context)
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                    locationResult?.let {
                        val deviceLatLng = LatLng(it.latitude, it.longitude)
                        currentSelectedOnMap = deviceLatLng
                        markerState.position = deviceLatLng.toGoogleLatLng()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(deviceLatLng.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
                        )
                    } ?: run { // Jika lokasi null, fallback
                        markerState.position = FALLBACK_INITIAL_LOCATION.toGoogleLatLng()
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(FALLBACK_INITIAL_LOCATION.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error getting initial current location.")
                    markerState.position = FALLBACK_INITIAL_LOCATION.toGoogleLatLng()
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(FALLBACK_INITIAL_LOCATION.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
                    )
                }
            } else {
                // Minta izin jika belum ada
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                // Peta akan tetap di fallback location sampai izin diberikan dan lokasi didapat
                markerState.position = FALLBACK_INITIAL_LOCATION.toGoogleLatLng()
                cameraPositionState.position = CameraPosition.fromLatLngZoom(FALLBACK_INITIAL_LOCATION.toGoogleLatLng(), DEFAULT_MAP_ZOOM)

            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.select_location_on_map)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_navigate_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (context.hasLocationPermission()) {
                                try {
                                    val locationResult = LocationServices.getFusedLocationProviderClient(context)
                                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                                    locationResult?.let {
                                        val deviceLatLng = LatLng(it.latitude, it.longitude)
                                        currentSelectedOnMap = deviceLatLng
                                        markerState.position = deviceLatLng.toGoogleLatLng()
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(deviceLatLng.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
                                        )
                                    }
                                } catch (e: Exception) { Timber.e(e, "Error FAB MyLocation") }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.use_current_location))
                }
                FloatingActionButton(
                    onClick = {
                        Timber.i("Location confirmed: $currentSelectedOnMap")
                        onLocationSelected(currentSelectedOnMap) // Kirim lokasi yang terakhir dipilih/di-drag
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.confirm))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false, // Kita buat FAB kustom
                    mapToolbarEnabled = false
                ),
                onMapClick = { gmsLatLng -> // gmsLatLng adalah com.google.android.gms.maps.model.LatLng
                    currentSelectedOnMap = LatLng(gmsLatLng.latitude, gmsLatLng.longitude)
                    markerState.position = gmsLatLng // Update posisi marker
                    Timber.d("Map clicked at: $currentSelectedOnMap")
                }
            ) {
                Marker(
                    state = markerState, // Gunakan markerState yang di-remember
                    title = stringResource(R.string.selected_location),
                    snippet = "Lat: ${"%.4f".format(currentSelectedOnMap.latitude)}, Lng: ${"%.4f".format(currentSelectedOnMap.longitude)}",
                    draggable = true,
                    icon = bitmapDescriptorFromVector(context, R.drawable.ic_map_pin_filled_red)
                    // onMarkerDragEnd tidak lagi digunakan di sini, perubahan posisi markerState sudah dihandle di LaunchedEffect
                )
            }
            // Info box untuk menampilkan koordinat yang dipilih secara realtime
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    text = "Pilihan: Lat ${"%.6f".format(currentSelectedOnMap.latitude)}, Lng ${"%.6f".format(currentSelectedOnMap.longitude)}",
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Fungsi utilitas toGoogleLatLng tetap sama
fun LatLng.toGoogleLatLng(): com.google.android.gms.maps.model.LatLng {
    return com.google.android.gms.maps.model.LatLng(this.latitude, this.longitude)
}

// Fungsi utilitas bitmapDescriptorFromVector tetap sama
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        // DrawableCompat.setTint(this, android.graphics.Color.RED) // Tidak perlu jika drawable sudah berwarna
        draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
