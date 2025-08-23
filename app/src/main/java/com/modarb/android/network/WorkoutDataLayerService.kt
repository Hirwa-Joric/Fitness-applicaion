
package com.modarb.android.network

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WorkoutDataLayerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/pause" -> {
                val intent = Intent("workout-control")
                intent.putExtra("command", "pause")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            "/next" -> {
                val intent = Intent("workout-control")
                intent.putExtra("command", "next")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }
}
