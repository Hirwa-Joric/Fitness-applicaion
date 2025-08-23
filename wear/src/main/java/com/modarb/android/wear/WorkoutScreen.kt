
package com.modarb.android.wear

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.Wearable

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    val context = LocalContext.current
    val exercise = viewModel.exercise.observeAsState("Current Exercise")
    val timer = viewModel.timer.observeAsState("00:00")
    val heartRate = viewModel.heartRate.observeAsState("-- BPM")
    val calories = viewModel.calories.observeAsState("-- kcal")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = exercise.value)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = timer.value)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = heartRate.value)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = calories.value)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = { sendMessage(context, "/pause") }) {
                    Text(text = "Pause")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { sendMessage(context, "/next") }) {
                    Text(text = "Next")
                }
            }
        }
    }
}

private fun sendMessage(context: android.content.Context, path: String) {
    val nodeClient = Wearable.getNodeClient(context)
    nodeClient.connectedNodes.addOnSuccessListener {
        it.forEach { node ->
            Wearable.getMessageClient(context).sendMessage(node.id, path, null)
        }
    }
}
