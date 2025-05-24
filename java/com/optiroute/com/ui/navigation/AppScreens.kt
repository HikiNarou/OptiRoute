package com.optiroute.com.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class yang merepresentasikan semua layar (tujuan navigasi) dalam aplikasi OptiRoute.
 * Setiap objek di dalamnya mewakili satu layar.
 *
 * @property route String unik yang digunakan sebagai rute untuk navigasi.
 * @property titleResId Resource ID untuk judul layar (digunakan di TopAppBar, dll.).
 * @property selectedIcon Ikon yang akan ditampilkan di BottomNavigationBar saat item dipilih.
 * @property unselectedIcon Ikon yang akan ditampilkan di BottomNavigationBar saat item tidak dipilih.
 */
sealed class AppScreens(
    val route: String,
    val titleResId: Int, // Menggunakan Int untuk String resource ID
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // Layar Utama (Bottom Navigation)
    data object Depot : AppScreens(
        route = "depot_screen",
        titleResId = com.optiroute.com.R.string.nav_depot,
        selectedIcon = Icons.Filled.EditLocationAlt,
        unselectedIcon = Icons.Outlined.EditLocationAlt
    )

    data object Vehicles : AppScreens(
        route = "vehicles_screen",
        titleResId = com.optiroute.com.R.string.nav_vehicles,
        selectedIcon = Icons.Filled.LocalShipping,
        unselectedIcon = Icons.Outlined.LocalShipping
    )

    data object Customers : AppScreens(
        route = "customers_screen",
        titleResId = com.optiroute.com.R.string.nav_customers,
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    )

    data object PlanRoute : AppScreens(
        route = "plan_route_screen",
        titleResId = com.optiroute.com.R.string.nav_plan_route,
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map
    )

    data object Settings : AppScreens(
        route = "settings_screen",
        titleResId = com.optiroute.com.R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    // Layar Detail atau Sekunder (tidak di bottom navigation)
    // Contoh: Layar untuk menambah/mengedit kendaraan
    data object AddEditVehicle : AppScreens(
        route = "add_edit_vehicle_screen", // Rute bisa menyertakan argumen: "add_edit_vehicle_screen?vehicleId={vehicleId}"
        titleResId = com.optiroute.com.R.string.add_vehicle_title, // Judul default, bisa diubah di layar
        selectedIcon = Icons.Filled.LocalShipping, // Tidak relevan untuk layar non-bottom nav
        unselectedIcon = Icons.Outlined.LocalShipping // Tidak relevan
    ) {
        // Fungsi untuk membuat rute dengan argumen opsional
        fun routeWithArg(vehicleId: Int? = null): String {
            return if (vehicleId != null) "add_edit_vehicle_screen?vehicleId=$vehicleId" else "add_edit_vehicle_screen"
        }
        const val ARG_VEHICLE_ID = "vehicleId" // Kunci untuk argumen vehicleId
        val routeWithNavArgs = "add_edit_vehicle_screen?$ARG_VEHICLE_ID={$ARG_VEHICLE_ID}"
    }


    data object AddEditCustomer : AppScreens(
        route = "add_edit_customer_screen",
        titleResId = com.optiroute.com.R.string.add_customer_title,
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    ) {
        fun routeWithArg(customerId: Int? = null): String {
            return if (customerId != null) "add_edit_customer_screen?customerId=$customerId" else "add_edit_customer_screen"
        }
        const val ARG_CUSTOMER_ID = "customerId"
        val routeWithNavArgs = "add_edit_customer_screen?$ARG_CUSTOMER_ID={$ARG_CUSTOMER_ID}"
    }

    data object SelectLocationMap : AppScreens(
        route = "select_location_map_screen",
        titleResId = com.optiroute.com.R.string.select_location_on_map,
        selectedIcon = Icons.Filled.Map, // Tidak relevan
        unselectedIcon = Icons.Outlined.Map // Tidak relevan
    ) {
        // Argumen untuk membawa LatLng awal jika ada (misalnya saat mengedit)
        // dan untuk mengembalikan LatLng yang dipilih.
        const val ARG_INITIAL_LAT = "initialLat"
        const val ARG_INITIAL_LNG = "initialLng"
        const val RESULT_LAT = "resultLat"
        const val RESULT_LNG = "resultLng"

        // Rute dengan argumen opsional untuk initial location
        // Contoh: select_location_map_screen?initialLat=xx&initialLng=yy
        val routeWithNavArgs = "select_location_map_screen?$ARG_INITIAL_LAT={$ARG_INITIAL_LAT}&$ARG_INITIAL_LNG={$ARG_INITIAL_LNG}"

        fun createRoute(initialLat: Double? = null, initialLng: Double? = null): String {
            var path = "select_location_map_screen"
            if (initialLat != null && initialLng != null) {
                path += "?$ARG_INITIAL_LAT=$initialLat&$ARG_INITIAL_LNG=$initialLng"
            }
            return path
        }
    }

    data object RouteResultsScreen : AppScreens(
        route = "route_results_screen",
        titleResId = com.optiroute.com.R.string.route_results_title,
        selectedIcon = Icons.Filled.Map, // Tidak relevan
        unselectedIcon = Icons.Outlined.Map // Tidak relevan
    ) {
        // Argumen untuk membawa ID hasil rute atau data rute itu sendiri (jika kecil)
        // Untuk data besar, lebih baik lewatkan ID dan ambil dari ViewModel/Repository
        const val ARG_ROUTE_PLAN_ID = "routePlanId" // Contoh jika kita menyimpan rencana rute
        val routeWithNavArgs = "route_results_screen/{$ARG_ROUTE_PLAN_ID}"

        fun createRoute(planId: String): String { // Menggunakan String untuk ID, bisa juga Long/Int
            return "route_results_screen/$planId"
        }
    }

}

// Daftar layar yang akan muncul di Bottom Navigation Bar
val bottomNavScreens = listOf(
    AppScreens.Depot,
    AppScreens.Vehicles,
    AppScreens.Customers,
    AppScreens.PlanRoute,
    AppScreens.Settings
)
