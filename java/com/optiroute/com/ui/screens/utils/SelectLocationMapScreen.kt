package com.optiroute.com.ui.screens.utils

import android.content.Context
import android.graphics.Bitmap
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.optiroute.com.R
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.ui.theme.spacing
import com.optiroute.com.utils.hasLocationPermission
import timber.log.Timber

// Default zoom level untuk peta
private const val DEFAULT_MAP_ZOOM = 15f
// Lokasi default jika tidak ada initial LatLng dan tidak bisa mendapatkan lokasi saat ini (misalnya, Palembang)
private val FALLBACK_INITIAL_LOCATION = LatLng(-2.976074, 104.775429)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLocationMapScreen(
    navController: NavController,
    initialLatLng: LatLng?, // Lokasi awal untuk ditampilkan (misalnya saat mengedit)
    onLocationSelected: (LatLng) -> Unit
) {
    val context = LocalContext.current
    var selectedMapLocation by remember { mutableStateOf(initialLatLng ?: FALLBACK_INITIAL_LOCATION) }
    var currentDeviceLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedMapLocation, DEFAULT_MAP_ZOOM)
    }

    // State untuk mengontrol apakah marker pengguna sudah dipindahkan
    var userHasMovedMarker by remember { mutableStateOf(initialLatLng != null) }

    // Efek untuk memperbarui selectedMapLocation saat kamera berhenti bergerak,
    // HANYA jika pengguna belum secara eksplisit memindahkan marker.
    // Ini memungkinkan peta untuk "mengikuti" saat pengguna menggeser,
    // tetapi berhenti mengikuti setelah marker ditempatkan atau dipindahkan.
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && !userHasMovedMarker) {
            val newPos = cameraPositionState.position.target
            selectedMapLocation = LatLng(newPos.latitude, newPos.longitude)
            Timber.v("Map idle, new target: $selectedMapLocation")
        }
    }

    // Efek untuk mendapatkan lokasi perangkat saat ini saat layar pertama kali dimuat,
    // jika tidak ada initialLatLng yang diberikan.
    LaunchedEffect(Unit) {
        if (initialLatLng == null && context.hasLocationPermission()) {
            try {
                val locationResult = com.google.android.gms.location.LocationServices
                    .getFusedLocationProviderClient(context)
                    .getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                    .await() // Menggunakan await dari kotlinx-coroutines-play-services
                if (locationResult != null) {
                    currentDeviceLocation = LatLng(locationResult.latitude, locationResult.longitude)
                    if (!userHasMovedMarker) { // Hanya pindah jika pengguna belum berinteraksi
                        selectedMapLocation = currentDeviceLocation!!
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                selectedMapLocation.toGoogleLatLng(), DEFAULT_MAP_ZOOM
                            )
                        )
                        Timber.d("Fetched current device location for map: $currentDeviceLocation")
                    }
                } else {
                    Timber.w("Device location is null, using fallback.")
                }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException while getting current location for map.")
                // Izin mungkin telah dicabut
            } catch (e: Exception) {
                Timber.e(e, "Exception while getting current location for map.")
            }
        } else if (initialLatLng != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(initialLatLng.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
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
                        currentDeviceLocation?.let {
                            selectedMapLocation = it
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(it.toGoogleLatLng(), DEFAULT_MAP_ZOOM)
                            )
                            userHasMovedMarker = true // Anggap pengguna telah memilih lokasi ini
                        } ?: run {
                            // Handle jika currentDeviceLocation masih null (misalnya, izin belum ada atau gagal fetch)
                            Timber.w("FAB MyLocation clicked, but currentDeviceLocation is null.")
                            // Mungkin minta izin lagi atau tampilkan pesan
                        }
                    },
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.use_current_location))
                }
                FloatingActionButton(
                    onClick = {
                        Timber.i("Location confirmed: $selectedMapLocation")
                        onLocationSelected(selectedMapLocation)
                        // Navigasi kembali akan ditangani oleh pemanggil setelah menerima hasil
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
                onMapClick = { gmsLatLng ->
                    selectedMapLocation = LatLng(gmsLatLng.latitude, gmsLatLng.longitude)
                    userHasMovedMarker = true // Pengguna secara eksplisit memilih lokasi
                    Timber.d("Map clicked at: $selectedMapLocation")
                },
                // Jika ingin peta mengikuti pergerakan kamera secara terus menerus (sebelum marker dipindah)
                // onPOIClick = { poi ->
                //     selectedMapLocation = LatLng(poi.latLng.latitude, poi.latLng.longitude)
                //     userHasMovedMarker = true
                //     Timber.d("POI clicked: ${poi.name} at $selectedMapLocation")
                // }
            ) {
                // Marker yang menunjukkan lokasi yang dipilih
                Marker(
                    state = MarkerState(position = selectedMapLocation.toGoogleLatLng()),
                    title = stringResource(R.string.selected_location),
                    snippet = "Lat: ${"%.4f".format(selectedMapLocation.latitude)}, Lng: ${"%.4f".format(selectedMapLocation.longitude)}",
                    draggable = true, // Izinkan marker di-drag
                    onInfoWindowClick = { /* Bisa tambahkan aksi saat info window diklik */ },
                    icon = bitmapDescriptorFromVector(context, R.drawable.ic_map_pin_filled_red), // Ikon kustom
                    onDragEnd = { marker ->
                        selectedMapLocation = LatLng(marker.position.latitude, marker.position.longitude)
                        userHasMovedMarker = true // Pengguna telah memindahkan marker
                        Timber.d("Marker dragged to: $selectedMapLocation")
                    }
                )
            }

            // Anda bisa menambahkan crosshair di tengah layar jika ingin metode pemilihan yang berbeda
            // Icon(
            //     imageVector = Icons.Filled.Add, // Ganti dengan ikon crosshair
            //     contentDescription = "Map Center",
            //     modifier = Modifier.align(Alignment.Center).size(48.dp),
            //     tint = MaterialTheme.colorScheme.primary
            // )
        }
    }
}

// Fungsi utilitas untuk mengubah LatLng kustom ke com.google.android.gms.maps.model.LatLng
fun LatLng.toGoogleLatLng(): com.google.android.gms.maps.model.LatLng {
    return com.google.android.gms.maps.model.LatLng(this.latitude, this.longitude)
}

// Fungsi utilitas untuk membuat BitmapDescriptor dari vector drawable
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        DrawableCompat.setTint(this, android.graphics.Color.RED) // Atur warna pin jika perlu
        draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

