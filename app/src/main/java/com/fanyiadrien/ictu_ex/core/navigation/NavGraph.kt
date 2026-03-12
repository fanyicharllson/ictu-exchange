package com.fanyiadrien.ictu_ex.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fanyiadrien.ictu_ex.feature.auth.CheckStatusScreen
import com.fanyiadrien.ictu_ex.feature.auth.SignInScreen
import com.fanyiadrien.ictu_ex.feature.auth.SignUpScreen

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
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(route = Screen.Onboarding.route) {
            // TODO(@teammate): Replace with your OnboardingScreen()
            // When user taps "Get Started":
//               navController.navigate(Screen.CheckStatus.route)
        }

        composable(route = Screen.CheckStatus.route) {
            // TODO(@teammate): Replace with your CheckStatusScreen()
            // When user picks SELLER:
            //   navController.navigate(Screen.SignUp.createRoute("SELLER"))
            // When user picks BUYER:
            //   navController.navigate(Screen.SignUp.createRoute("BUYER"))
            // When user taps "Already have an account?":
            //   navController.navigate(Screen.SignIn.route)
        }

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(
            route     = Screen.SignUp.route,         // "sign_up/{userType}"
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "BUYER"
            // TODO(@teammate): Replace with your SignUpScreen(userType = userType)
            // After successful signup:
            //   navController.navigate(Screen.Home.route) {
            //       popUpTo(Screen.Onboarding.route) { inclusive = true }  ← clears back stack
            //   }
        }

        composable(route = Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }

        // ── Main App ──────────────────────────────────────────────────────────
        composable(route = Screen.Home.route) {
            // TODO: HomeScreen()
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