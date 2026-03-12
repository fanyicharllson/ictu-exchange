package com.fanyiadrien.ictu_ex.core.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fanyiadrien.ictu_ex.feature.auth.CheckStatusScreen
import com.fanyiadrien.ictu_ex.feature.auth.SignInScreen
import com.fanyiadrien.ictu_ex.feature.auth.SignUpScreen
import com.fanyiadrien.ictu_ex.feature.home.HomeScreen
import com.fanyiadrien.ictu_ex.feature.onboarding.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "ICTU_NavGraph"

/**
 * Central NavGraph for ICTU-Ex.
 *
 * All screens are registered here. Each feature team member only needs to:
 *  1. Import their screen composable
 *  2. Call navController.navigate(Screen.X.route) to go somewhere
 *
 * Start destination is [Screen.Onboarding].
 * After login/signup, navigate to [Screen.Home] and clear the back stack
 * so the user cannot press Back to return to auth screens.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route,
    auth: FirebaseAuth
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(route = Screen.Onboarding.route) {
            Log.d(TAG, "Entering route: ${Screen.Onboarding.route}")
            OnboardingScreen(navController = navController)
        }

        composable(route = Screen.CheckStatus.route) {
            Log.d(TAG, "Entering route: ${Screen.CheckStatus.route}")
            CheckStatusScreen(navController = navController)
        }

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(
            route     = Screen.SignUp.route,         // "sign_up/{userType}"
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "BUYER"
            Log.d(TAG, "Entering route: sign_up with userType=$userType")
            SignUpScreen(navController = navController, userType = userType)
        }

        composable(route = Screen.SignIn.route) {
            Log.d(TAG, "Entering route: ${Screen.SignIn.route}")
            SignInScreen(navController = navController)
        }

        // ── Main App ──────────────────────────────────────────────────────────
        composable(route = Screen.Home.route) {
            Log.d(TAG, "Entering route: ${Screen.Home.route}")
            HomeScreen(auth = auth)
        }

        composable(
            route     = Screen.ItemDetail.route,     // "item_detail/{listingId}"
            arguments = listOf(
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            // TODO: ItemDetailScreen(listingId = listingId)
        }

        composable(route = Screen.PostItem.route) {
            // TODO: PostItemScreen()
        }

        composable(route = Screen.Profile.route) {
            // TODO: ProfileScreen()
        }
        composable(route = Screen.Settings.route) {
        //TODO SettingScreen()
        }
    }
}