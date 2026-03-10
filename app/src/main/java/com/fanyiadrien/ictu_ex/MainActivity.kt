package com.fanyiadrien.ictu_ex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.core.navigation.NavGraph
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.ui.theme.IctuExTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // darkTheme state — LightSensorManager will update this value
            val darkTheme by remember { mutableStateOf(false) }
            IctuExTheme {
                val navController = rememberNavController()

                // Change startDestination to Screen.Home.route once user is already logged in
                // (check SharedPreferences or Firebase auth state here later)
                NavGraph(
                    navController = navController,
                    startDestination = Screen.Onboarding.route
                )

            }
        }
    }
}


