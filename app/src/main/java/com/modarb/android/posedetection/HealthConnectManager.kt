package com.modarb.android.posedetection

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        val providerPackageName = "com.google.android.apps.healthdata"
        val status = HealthConnectClient.getSdkStatus(context, providerPackageName)
        if (status == HealthConnectClient.SDK_UNAVAILABLE) {
            return@lazy null
        }
        HealthConnectClient.getOrCreate(context)
    }

    fun isHealthConnectAvailable(): Boolean {
        return healthConnectClient != null
    }

    suspend fun hasPermissions(): Boolean {
        val granted = healthConnectClient?.permissionController?.getGrantedPermissions()
        return granted?.containsAll(PERMISSIONS) == true
    }

    fun requestPermissions(activity: ActivityResultRegistryOwner): ActivityResultLauncher<Array<String>> {
        return activity.activityResultRegistry.register(
            "health_connect_permissions",
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle permission result
        }
    }

    fun readSteps(startTime: Instant, endTime: Instant, callback: (Long?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                val response = healthConnectClient?.readRecords(request)
                val totalSteps = response?.records?.sumOf { it.count } ?: 0L
                callback(totalSteps)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    companion object {
        val PERMISSIONS =
            setOf(
                HealthPermission.getReadPermission(StepsRecord::class)
            )
    }
}
