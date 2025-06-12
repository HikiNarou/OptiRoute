package com.optiroute.com.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.optiroute.com.presentation.navigation.OptiRouteNavigation
import com.optiroute.com.presentation.ui.theme.OptiRouteTheme
import com.optiroute.com.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OptiRouteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by authViewModel.authState.collectAsState()
                    val currentUser by authViewModel.currentUser.collectAsState()

                    OptiRouteNavigation(
                        authState = authState,
                        currentUser = currentUser
                    )
                }
            }
        }
    }
}