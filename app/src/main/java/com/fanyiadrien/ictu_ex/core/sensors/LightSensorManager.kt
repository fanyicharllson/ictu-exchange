package com.fanyiadrien.ictu_ex.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the device's physical ambient light sensor (lux).
 * Emits [isDark] = true when light drops below the threshold.
 *
 * MainActivity observes [isDark] and passes it to IctuExTheme(darkTheme = ...)
 */
@Singleton
class LightSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    // ── Threshold: below 10 lux = dark room (dim lamp, night study) ──────────
    private val DARK_THRESHOLD = 10f

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // ── This is what MainActivity observes ───────────────────────────────────
    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark

    /** Call in MainActivity onResume — starts listening to the sensor */
    fun register() {
        lightSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /** Call in MainActivity onPause — stops listening to save battery */
    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            _isDark.value = lux < DARK_THRESHOLD
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}