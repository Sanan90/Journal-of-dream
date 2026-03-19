package com.dreamjournal.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dreamjournal.journalofdream.util.LocaleHelper
import com.dreamjournal.journalofdream.util.NOTIFICATION_ID_REALITY
import com.dreamjournal.journalofdream.util.scheduleRealityCheck
import com.dreamjournal.journalofdream.util.showNotification

class RealityCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("reality_check_enabled", false)
        if (!enabled) return

        val localizedContext = LocaleHelper.applyLanguage(context)
        showNotification(
            context,
            title = localizedContext.getString(R.string.notif_reality_title),
            message = localizedContext.getString(R.string.notif_reality_message),
            notificationId = NOTIFICATION_ID_REALITY
        )

        scheduleRealityCheck(context)
        Log.d("RealityCheckReceiver", "Reality check notification sent and next one scheduled")
    }
}
