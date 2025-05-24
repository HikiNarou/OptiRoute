package com.optiroute.com.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.optiroute.com.R
import com.optiroute.com.ui.theme.spacing
import com.optiroute.com.utils.ConfirmationDialog
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current // Digunakan untuk getString di dalam LaunchedEffect
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val uiState by viewModel.uiState.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }

    // Mengambil string resource di lingkup Composable untuk digunakan dalam callback
    val cannotOpenEmailMessage = stringResource(R.string.error_occurred) // Ganti dengan string yang lebih spesifik jika ada, misal "Tidak dapat membuka aplikasi email."
    val cannotOpenUrlMessage = stringResource(R.string.error_occurred)   // Ganti dengan string yang lebih spesifik jika ada, misal "Tidak dapat membuka URL."


    LaunchedEffect(lifecycleOwner.lifecycle) { // Kunci Unit dihapus agar re-subscribe jika perlu
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is SettingsUiEvent.ShowSnackbar -> {
                        // Menggunakan context yang sudah di-capture dari lingkup Composable
                        val message = event.messageText ?: event.messageResId?.let { context.getString(it) } ?: ""
                        if (message.isNotBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearDataDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.settings_clear_data_title),
            message = stringResource(R.string.settings_confirm_clear_data_message),
            onConfirm = {
                viewModel.onClearAllDataConfirmed()
                showClearDataDialog = false
            },
            onDismiss = {
                showClearDataDialog = false
            },
            confirmButtonText = stringResource(R.string.delete),
            dismissButtonText = stringResource(R.string.cancel)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
        // TopAppBar sudah ada di MainActivity, jadi tidak perlu ditambahkan lagi di sini
        // jika mengikuti pola yang sudah ada.
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding dari Scaffold
                .verticalScroll(rememberScrollState())
        ) {
            // Bagian Data Aplikasi
            SettingsSectionTitle(title = "Manajemen Data")
            SettingItem(
                icon = Icons.Filled.DeleteForever,
                title = stringResource(R.string.settings_clear_data_title),
                subtitle = stringResource(R.string.settings_clear_data_summary),
                onClick = { showClearDataDialog = true },
                titleColor = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.error
            )
            if (uiState.isClearingData) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text("Menghapus data...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Divider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium))

            // Bagian Informasi
            SettingsSectionTitle(title = "Tentang Aplikasi")
            SettingItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.app_name) + " Versi",
                subtitle = uiState.appVersion,
                onClick = {} // Tidak ada aksi, hanya info
            )
            SettingItem(
                icon = Icons.Filled.Terminal, // Contoh ikon, bisa diganti
                title = "Dikembangkan Oleh",
                subtitle = "Kelompok 8 - D4 MI POLSRI 2025", // Sesuaikan
                onClick = {}
            )
            SettingItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "Bantuan & Dukungan",
                subtitle = "hubungi.optiroute@example.com", // Ganti dengan email kontak Anda
                onClick = {
                    try {
                        uriHandler.openUri("mailto:hubungi.optiroute@example.com")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to open mail client")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(cannotOpenEmailMessage)
                        }
                    }
                }
            )
            SettingItem(
                icon = Icons.Filled.Policy,
                title = "Kebijakan Privasi",
                subtitle = "Baca kebijakan privasi kami", // Tambahkan subtitle jika ada
                onClick = {
                    try {
                        uriHandler.openUri("https://example.com/privacy") // Ganti dengan URL kebijakan privasi Anda
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to open privacy policy URL")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(cannotOpenUrlMessage)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f)) // Mendorong footer ke bawah

            Text(
                text = "OptiRoute © 2025", // Sesuaikan tahun jika perlu
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.medium,
            end = MaterialTheme.spacing.medium,
            top = MaterialTheme.spacing.large, // Lebih banyak spasi di atas judul bagian
            bottom = MaterialTheme.spacing.small
        )
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = LocalContentColor.current, // Menggunakan LocalContentColor sebagai default
    iconTint: Color = MaterialTheme.colorScheme.secondary, // Default tint untuk ikon
    onClick: () -> Unit
) {
    Surface( // Menggunakan Surface untuk efek klik yang lebih baik dan konsistensi tema
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface // Atau transparent jika ingin menyatu dengan background Column
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large)) // Spasi lebih besar antara ikon dan teks
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor) // Ukuran font disesuaikan
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium, // Ukuran font disesuaikan
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
