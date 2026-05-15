package com.example.jewcalendar.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationProvider {

    suspend fun getCurrentLocation(context: Context): UserLocation? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return null

        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)

            val last = client.lastLocation.await()
            if (last != null) {
                return UserLocation(last.latitude, last.longitude)
            }

            val cts = CancellationTokenSource()
            val fresh = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
            if (fresh != null) UserLocation(fresh.latitude, fresh.longitude) else null
        } catch (e: Exception) {
            null
        }
    }
}