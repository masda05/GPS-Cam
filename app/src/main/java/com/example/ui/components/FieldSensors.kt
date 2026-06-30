package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class GPSData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val speed: Float = 0.0f,
    val bearing: Float = 0.0f, // From GPS
    val heading: Float = 0.0f, // From Compass Sensor
    val address: String = "Acquiring address...",
    val isMock: Boolean = false,
    val signalStrength: Int = 0, // 0 (none), 1 (weak), 2 (medium), 3 (strong)
    val hasLocation: Boolean = false
)

class FieldSensors(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private val _gpsData = MutableStateFlow(GPSData())
    val gpsData: StateFlow<GPSData> = _gpsData.asStateFlow()

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var locationCallback: LocationCallback? = null

    init {
        registerCompass()
    }

    fun registerCompass() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        }
        val magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magnet != null) {
            sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterCompass() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            updateOrientationAngles()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            updateOrientationAngles()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        // Convert radians to degrees, normalized to 0-360
        var headingDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        if (headingDeg < 0) {
            headingDeg += 360f
        }
        _gpsData.update { it.copy(heading = headingDeg) }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // High update frequency for tracking walking/vehicular alignment
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).apply {
            setMinUpdateIntervalMillis(1000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    processLocation(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    processLocation(loc)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        unregisterCompass()
    }

    private fun processLocation(location: Location) {
        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }

        // Level signal: 3 (Strong, acc < 5m), 2 (Med, acc < 15m), 1 (Weak, acc < 50m)
        val signal = when {
            location.accuracy < 5f -> 3
            location.accuracy < 15f -> 2
            location.accuracy < 50f -> 1
            else -> 1
        }

        _gpsData.update {
            it.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                speed = location.speed,
                bearing = location.bearing,
                isMock = isMock,
                signalStrength = signal,
                hasLocation = true
            )
        }

        // Run reverse geocoding asynchronously on IO Thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]
                    val adrText = addressObj.getAddressLine(0) ?: "${addressObj.subLocality ?: ""}, ${addressObj.locality ?: ""}"
                    _gpsData.update { it.copy(address = adrText) }
                } else {
                    _gpsData.update { it.copy(address = "No address resolved") }
                }
            } catch (e: Exception) {
                _gpsData.update { it.copy(address = "No internet (GPS Coordinates Only)") }
            }
        }
    }
}
