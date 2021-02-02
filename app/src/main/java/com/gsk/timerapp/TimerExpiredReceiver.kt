package com.gsk.timerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gsk.timerapp.util.NotificationUtil
import com.gsk.timerapp.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timer = intent.getIntExtra(TIMER_TYPE, 0)
        NotificationUtil.showTimerExpired(context, timer)

        PrefUtil.setTimerState(context, timer, ReminderActivity.TimerState.Stopped)
    }
}