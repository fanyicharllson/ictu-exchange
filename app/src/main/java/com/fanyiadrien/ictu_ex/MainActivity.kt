package com.fanyiadrien.ictu_ex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.core.navigation.NavGraph
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.sensors.LightSensorManager
import com.fanyiadrien.ictu_ex.ui.theme.IctuExTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    // Hilt delivers this automatically — no manual creation needed
    @Inject
    lateinit var lightSensorManager: LightSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Observes the sensor flow — updates theme instantly when lux changes
            val isDark by lightSensorManager.isDark.collectAsState()

            IctuExTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = Screen.Onboarding.route,
                    auth = auth
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensorManager.register()    // start sensor when app is visible
    }

    override fun onPause() {
        super.onPause()
        lightSensorManager.unregister()  // stop sensor when app goes background
    }
}