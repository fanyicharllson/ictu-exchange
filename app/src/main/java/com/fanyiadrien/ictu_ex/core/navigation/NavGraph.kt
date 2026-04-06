package com.fanyiadrien.ictu_ex.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fanyiadrien.ictu_ex.core.camera.CameraScreen
import com.fanyiadrien.ictu_ex.feature.activity.MyActivityScreen
import com.fanyiadrien.ictu_ex.feature.auth.CheckStatusScreen
import com.fanyiadrien.ictu_ex.feature.auth.SignInScreen
import com.fanyiadrien.ictu_ex.feature.auth.SignUpScreen
import com.fanyiadrien.ictu_ex.feature.cart.CartScreen
import com.fanyiadrien.ictu_ex.feature.detail.ItemDetailScreen
import com.fanyiadrien.ictu_ex.feature.home.HomeScreen
import com.fanyiadrien.ictu_ex.feature.notifications.NotificationScreen
import com.fanyiadrien.ictu_ex.feature.onboarding.OnboardingScreen
import com.fanyiadrien.ictu_ex.feature.post.PostItemScreen
import com.fanyiadrien.ictu_ex.feature.profile.EditProfileScreen
import com.fanyiadrien.ictu_ex.feature.profile.ProfileScreen
import com.fanyiadrien.ictu_ex.feature.settings.SettingScreen
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route,
    auth: FirebaseAuth,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(Screen.CheckStatus.route) {
            CheckStatusScreen(navController = navController)
        }

        composable(
            route     = Screen.SignUp.route,
            arguments = listOf(navArgument("userType") { type = NavType.StringType })
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "BUYER"
            SignUpScreen(navController = navController, userType = userType)
        }

        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController     = navController,
                themeMode         = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        }

        composable(
            route     = Screen.ItemDetail.route,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) {
            ItemDetailScreen(navController = navController)
        }

        composable(Screen.PostItem.route) {
            val capturedUriString = navController.currentBackStackEntry
                ?.savedStateHandle?.get<String>("captured_image_uri")
            PostItemScreen(
                navController    = navController,
                capturedImageUri = capturedUriString?.let { Uri.parse(it) }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingScreen(
                navController     = navController,
                themeMode         = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(navController = navController)
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(navController = navController)
        }

        composable(Screen.MyActivity.route) {
            MyActivityScreen(navController = navController)
        }
    }
}
