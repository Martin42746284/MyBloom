package com.example.mybloom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.mybloom.config.AppDatabase
import com.example.mybloom.navigation.NavGraph
import com.example.mybloom.repository.AuthRepository
import com.example.mybloom.repository.DiscoveryRepository
import com.example.mybloom.ui.theme.PlantdiscoveryTheme
import com.example.mybloom.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activer Edge-to-Edge pour un UI moderne
        enableEdgeToEdge()

        // Initialiser Room Database
        val database = AppDatabase.getDatabase(applicationContext)
        val discoveryDao = database.discoveryDao()

        // Initialiser DiscoveryRepository avec DAO et Context
        val discoveryRepository = DiscoveryRepository(
            discoveryDao = discoveryDao,
            context = applicationContext
        )

        // Initialiser AuthRepository et AuthViewModel
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        setContent {
            PlantdiscoveryTheme {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    repository = discoveryRepository,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
