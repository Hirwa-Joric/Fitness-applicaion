
package com.modarb.android.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val viewModel: WorkoutViewModel by viewModels()
    private lateinit var healthConnectClient: HealthConnectClient
    private val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectClient = HealthConnectClient.getOrCreate(this)
        requestPermissions()

        setContent {
            WorkoutScreen(viewModel = viewModel)
        }
        Wearable.getDataClient(this).addListener(this)
    }

    private fun requestPermissions() {
        val requestPermissionActivityContract = healthConnectClient.permissionController.createRequestPermissionActivityContract()
        val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(permissions)) {
                readHealthData()
            }
        }
        requestPermissions.launch(permissions)
    }

    private fun readHealthData() {
        lifecycleScope.launch {
            readHeartRate()
            readCalories()
        }
    }

    private suspend fun readHeartRate() {
        val now = Instant.now()
        val startOfDay = now.truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        val request = androidx.health.connect.client.request.ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(startOfDay, now)
        )
        val response = healthConnectClient.readRecords(request)
        response.records.lastOrNull()?.let {
            viewModel.updateHeartRate("${it.samples.lastOrNull()?.beatsPerMinute ?: "--"} BPM")
        }
    }

    private suspend fun readCalories() {
        val now = Instant.now()
        val startOfDay = now.truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        val request = androidx.health.connect.client.request.ReadRecordsRequest(
            recordType = TotalCaloriesBurnedRecord::class,
            timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(startOfDay, now)
        )
        val response = healthConnectClient.readRecords(request)
        response.records.lastOrNull()?.let {
            viewModel.updateCalories("${it.energy.inKilocalories.toInt()} kcal")
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val dataMap = dataMapItem.dataMap
                viewModel.updateExercise(dataMap.getString("exercise", ""))
                viewModel.updateTimer(dataMap.getString("timer", ""))
                viewModel.updateHeartRate(dataMap.getString("heart_rate", ""))
                viewModel.updateCalories(dataMap.getString("calories", ""))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        readHealthData()
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }
}
