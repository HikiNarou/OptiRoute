package com.optiroute.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.optiroute.com.ui.navigation.AppScreens
import com.optiroute.com.ui.navigation.OptiRouteNavHost
import com.optiroute.com.ui.navigation.bottomNavScreens
import com.optiroute.com.ui.theme.OptiRouteTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity onCreate called")
        enableEdgeToEdge()
        setContent {
            OptiRouteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OptiRouteApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptiRouteApp() {
    val navController = rememberNavController()
    // State untuk menyimpan judul TopAppBar saat ini
    var currentScreenTitleResId by remember { mutableStateOf(AppScreens.Depot.titleResId) }

    Scaffold(
        topBar = {
            // Hanya tampilkan TopAppBar jika bukan layar peta fullscreen atau layar hasil
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showTopBar = currentRoute !in listOf(
                AppScreens.SelectLocationMap.routeWithNavArgs, // Membandingkan dengan template rute
                // Tambahkan rute lain yang tidak memerlukan TopAppBar
            ) && !currentRoute.orEmpty().startsWith(AppScreens.RouteResultsScreen.route.substringBefore("/{"))


            if (showTopBar) {
                TopAppBar(
                    title = { Text(stringResource(id = currentScreenTitleResId)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    // Anda bisa menambahkan navigationIcon jika diperlukan (misalnya, tombol kembali untuk layar detail)
                )
            }
        },
        bottomBar = {
            // Hanya tampilkan BottomNavigationBar jika layar saat ini adalah salah satu dari bottomNavScreens
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavScreens.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface, // Atau surfaceVariant
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = stringResource(id = screen.titleResId)
                                )
                            },
                            label = { Text(stringResource(id = screen.titleResId)) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up ke start destination dari graph untuk menghindari tumpukan back stack yang besar
                                    // saat memilih item yang sama berulang kali atau beralih antar item.
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Hindari membuat salinan ganda dari tujuan yang sama di atas tumpukan.
                                    launchSingleTop = true
                                    // Kembalikan state saat memilih kembali item yang sebelumnya dipilih.
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        OptiRouteNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onTitleChanged = { titleResId ->
                currentScreenTitleResId = titleResId
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OptiRouteTheme {
        OptiRouteApp()
    }
}

