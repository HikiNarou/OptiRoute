package com.optiroute.com.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.ui.screens.customer.AddEditCustomerScreen // Akan dibuat
import com.optiroute.com.ui.screens.customer.CustomersScreen // Akan dibuat
import com.optiroute.com.ui.screens.depot.DepotScreen // Akan dibuat
import com.optiroute.com.ui.screens.planroute.PlanRouteScreen // Akan dibuat
import com.optiroute.com.ui.screens.planroute.RouteResultsScreen // Akan dibuat
import com.optiroute.com.ui.screens.settings.SettingsScreen // Akan dibuat
import com.optiroute.com.ui.screens.utils.SelectLocationMapScreen // Akan dibuat
import com.optiroute.com.ui.screens.vehicle.AddEditVehicleScreen // Akan dibuat
import com.optiroute.com.ui.screens.vehicle.VehiclesScreen // Akan dibuat
import timber.log.Timber

/**
 * NavHost utama untuk aplikasi OptiRoute.
 * Mendefinisikan semua rute navigasi dan Composable yang terkait.
 *
 * @param navController Instance dari NavHostController untuk mengelola navigasi.
 * @param modifier Modifier untuk diterapkan pada NavHost.
 * @param onTitleChanged Callback untuk mengubah judul di TopAppBar saat navigasi berubah.
 */
@Composable
fun OptiRouteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onTitleChanged: (Int) -> Unit // Callback untuk mengubah judul AppBar
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Depot.route, // Layar awal aplikasi
        modifier = modifier
    ) {
        // Layar Depot
        composable(AppScreens.Depot.route) {
            onTitleChanged(AppScreens.Depot.titleResId)
            DepotScreen(navController = navController)
        }

        // Layar Kendaraan
        composable(AppScreens.Vehicles.route) {
            onTitleChanged(AppScreens.Vehicles.titleResId)
            VehiclesScreen(
                navController = navController,
                onNavigateToAddEditVehicle = { vehicleId ->
                    navController.navigate(AppScreens.AddEditVehicle.routeWithArg(vehicleId))
                }
            )
        }

        // Layar Tambah/Ubah Kendaraan
        composable(
            route = AppScreens.AddEditVehicle.routeWithNavArgs,
            arguments = listOf(
                navArgument(AppScreens.AddEditVehicle.ARG_VEHICLE_ID) {
                    type = NavType.IntType
                    defaultValue = -1 // -1 menandakan tambah baru
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt(AppScreens.AddEditVehicle.ARG_VEHICLE_ID) ?: -1
            val titleRes = if (vehicleId == -1) AppScreens.AddEditVehicle.titleResId else com.optiroute.com.R.string.edit_vehicle_title
            onTitleChanged(titleRes)
            AddEditVehicleScreen(
                navController = navController,
                vehicleId = if (vehicleId == -1) null else vehicleId
            )
        }

        // Layar Pelanggan
        composable(AppScreens.Customers.route) {
            onTitleChanged(AppScreens.Customers.titleResId)
            CustomersScreen(
                navController = navController,
                onNavigateToAddEditCustomer = { customerId ->
                    navController.navigate(AppScreens.AddEditCustomer.routeWithArg(customerId))
                }
            )
        }

        // Layar Tambah/Ubah Pelanggan
        composable(
            route = AppScreens.AddEditCustomer.routeWithNavArgs,
            arguments = listOf(
                navArgument(AppScreens.AddEditCustomer.ARG_CUSTOMER_ID) {
                    type = NavType.IntType
                    defaultValue = -1 // -1 menandakan tambah baru
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getInt(AppScreens.AddEditCustomer.ARG_CUSTOMER_ID) ?: -1
            val titleRes = if (customerId == -1) AppScreens.AddEditCustomer.titleResId else com.optiroute.com.R.string.edit_customer_title
            onTitleChanged(titleRes)
            AddEditCustomerScreen(
                navController = navController,
                customerId = if (customerId == -1) null else customerId
            )
        }

        // Layar Perencanaan Rute
        composable(AppScreens.PlanRoute.route) {
            onTitleChanged(AppScreens.PlanRoute.titleResId)
            PlanRouteScreen(navController = navController)
        }

        // Layar Hasil Rute
        composable(
            route = AppScreens.RouteResultsScreen.routeWithNavArgs,
            arguments = listOf(navArgument(AppScreens.RouteResultsScreen.ARG_ROUTE_PLAN_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(AppScreens.RouteResultsScreen.ARG_ROUTE_PLAN_ID)
            onTitleChanged(AppScreens.RouteResultsScreen.titleResId)
            // Anda mungkin perlu mengambil data rute berdasarkan planId di sini
            RouteResultsScreen(navController = navController, routePlanId = planId)
        }


        // Layar Pengaturan
        composable(AppScreens.Settings.route) {
            onTitleChanged(AppScreens.Settings.titleResId)
            SettingsScreen(navController = navController)
        }

        // Layar Pilih Lokasi di Peta
        composable(
            route = AppScreens.SelectLocationMap.routeWithNavArgs,
            arguments = listOf(
                navArgument(AppScreens.SelectLocationMap.ARG_INITIAL_LAT) {
                    type = NavType.FloatType // Gunakan FloatType untuk Double di NavArgs
                    defaultValue = -999.0f // Nilai default yang tidak mungkin
                },
                navArgument(AppScreens.SelectLocationMap.ARG_INITIAL_LNG) {
                    type = NavType.FloatType
                    defaultValue = -999.0f
                }
            )
        ) { backStackEntry ->
            onTitleChanged(AppScreens.SelectLocationMap.titleResId)
            val initialLatArg = backStackEntry.arguments?.getFloat(AppScreens.SelectLocationMap.ARG_INITIAL_LAT)
            val initialLngArg = backStackEntry.arguments?.getFloat(AppScreens.SelectLocationMap.ARG_INITIAL_LNG)

            val initialLatLng = if (initialLatArg != -999.0f && initialLngArg != -999.0f && initialLatArg != null && initialLngArg != null) {
                LatLng(initialLatArg.toDouble(), initialLngArg.toDouble())
            } else {
                null
            }

            SelectLocationMapScreen(
                navController = navController,
                initialLatLng = initialLatLng,
                onLocationSelected = { latLng ->
                    // Simpan hasil ke NavController's previousBackStackEntry
                    // agar layar pemanggil bisa mengambilnya.
                    navController.previousBackStackEntry?.savedStateHandle?.set(AppScreens.SelectLocationMap.RESULT_LAT, latLng.latitude)
                    navController.previousBackStackEntry?.savedStateHandle?.set(AppScreens.SelectLocationMap.RESULT_LNG, latLng.longitude)
                    Timber.d("Location selected on map: $latLng, navigating back.")
                    navController.popBackStack()
                }
            )
        }
    }
}
