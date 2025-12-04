package com.example.mybloom.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.mybloom.ui.screens.JournalListScreen
import com.example.mybloom.repository.DiscoveryRepository
import com.example.mybloom.ui.screens.CaptureScreen
import com.example.mybloom.ui.screens.DetailScreen
import com.example.mybloom.ui.screens.SignInScreen
import com.example.mybloom.ui.screens.SignUpScreen
import com.example.mybloom.viewmodel.AuthViewModel
import com.example.mybloom.viewmodel.JournalViewModel
import com.example.mybloom.viewmodel.DiscoveryViewModel
import com.example.mybloom.viewmodel.DetailViewModel
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object JournalList : Screen("journal_list")
    object Capture : Screen("capture")
    object Detail : Screen("detail/{discoveryId}") {
        fun createRoute(discoveryId: Int) = "detail/$discoveryId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: DiscoveryRepository,
    authViewModel: AuthViewModel,
    onThemeChange: (String) -> Unit = {} // Ajout du paramètre
) {
    val context = LocalContext.current

    // Vérifier si l'utilisateur est déjà connecté
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) {
        Screen.JournalList.route
    } else {
        Screen.SignIn.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== AUTHENTICATION SCREENS ==========

        // Sign In Screen
        composable(Screen.SignIn.route) {
            val loading by authViewModel.loading.collectAsState()
            val error by authViewModel.error.collectAsState()

            SignInScreen(
                loading = loading,
                error = error,
                onSignInClick = { email, password ->
                    authViewModel.signIn(email, password) {
                        navController.navigate(Screen.JournalList.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    }
                },
                onGoogleClick = {
                    authViewModel.signInWithGoogle(context = context) {
                        navController.navigate(Screen.JournalList.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    }
                },
                onGoToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onErrorDismiss = { authViewModel.clearError() }
            )
        }

        // Sign Up Screen
        composable(Screen.SignUp.route) {
            val loading by authViewModel.loading.collectAsState()
            val error by authViewModel.error.collectAsState()

            SignUpScreen(
                loading = loading,
                error = error,
                onSignUpClick = { email, password ->
                    authViewModel.signUp(email, password) {
                        navController.navigate(Screen.JournalList.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                },
                onGoogleClick = {
                    authViewModel.signInWithGoogle(context = context) {
                        navController.navigate(Screen.JournalList.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                },
                onGoToSignIn = {
                    navController.popBackStack()
                },
                onErrorDismiss = { authViewModel.clearError() }
            )
        }

        // ========== MAIN APP SCREENS ==========

        // Journal List Screen
        composable(Screen.JournalList.route) {
            val journalViewModel = remember { JournalViewModel(repository) }

            JournalListScreen(
                viewModel = journalViewModel,
                onAddClick = {
                    navController.navigate(Screen.Capture.route)
                },
                onCardClick = { discoveryId ->
                    navController.navigate(Screen.Detail.createRoute(discoveryId))
                },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onThemeChange = onThemeChange // Passe le callback ici
            )
        }

        // Capture Screen (avec Gemini AI)
        composable(Screen.Capture.route) {
            val discoveryViewModel = remember { DiscoveryViewModel(repository) }

            CaptureScreen(
                viewModel = discoveryViewModel,
                onNavigateToDetail = { discoveryId ->
                    navController.navigate(Screen.Detail.createRoute(discoveryId)) {
                        popUpTo(Screen.Capture.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Detail Screen
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("discoveryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val discoveryId = backStackEntry.arguments?.getInt("discoveryId") ?: return@composable
            val detailViewModel = remember { DetailViewModel(repository) }

            DetailScreen(
                viewModel = detailViewModel,
                discoveryId = discoveryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
