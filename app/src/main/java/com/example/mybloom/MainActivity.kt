package com.example.mybloom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.mybloom.config.AppDatabase
import com.example.mybloom.config.ThemeManager
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
            val context = LocalContext.current

            // Charger le thème sauvegardé
            val savedTheme = remember { ThemeManager.getTheme(context) }
            var currentTheme by remember { mutableStateOf(savedTheme) }

            // Déterminer si on utilise le mode sombre
            val darkTheme = when (currentTheme) {
                ThemeManager.THEME_LIGHT -> false
                ThemeManager.THEME_DARK -> true
                else -> isSystemInDarkTheme() // THEME_SYSTEM (par défaut)
            }

            PlantdiscoveryTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    repository = discoveryRepository,
                    authViewModel = authViewModel,
                    onThemeChange = { themeCode ->
                        currentTheme = themeCode
                    }
                )
            }
        }
    }
}
