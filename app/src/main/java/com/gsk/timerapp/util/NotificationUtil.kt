package com.gsk.timerapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.gsk.timerapp.*

class NotificationUtil {
    companion object {
        private const val TIMER_CHANNEL_ID = "timer_channel_id"
        private const val TIMER_CHANNEL_NAME = "Reminder App"

        fun showTimerExpired(context: Context, timer: Int) {
            val res = context.resources
            val nBuilder = getBasicNotificationBuilder(context, TIMER_CHANNEL_ID, true)
            when (timer) {
                HYDRATE_TIMER -> {
                    val picture = BitmapFactory.decodeResource(res, R.drawable.water)
                    nBuilder.setContentTitle("Time To Hydrate!")
                            .setContentText("Grab yourself a glass of water")
                            .setLargeIcon(picture)
                            .setContentIntent(getPendingIntentWithStack(context, ReminderActivity::class.java))
                }
                STRETCH_TIMER -> {
                    val picture = BitmapFactory.decodeResource(res, R.drawable.stretching)
                    nBuilder.setContentTitle("Time To Stretch!")
                            .setContentText("Stand up and stretch your body")
                            .setLargeIcon(picture)
                            .setContentIntent(getPendingIntentWithStack(context, ReminderActivity::class.java))
                }
                WALK_TIMER -> {
                    val picture = BitmapFactory.decodeResource(res, R.drawable.walking)
                    nBuilder.setContentTitle("Time To Walk!")
                            .setContentText("Go for a walk. You deserve it!")
                            .setLargeIcon(picture)
                            .setContentIntent(getPendingIntentWithStack(context, ReminderActivity::class.java))
                }
            }
            val nManager = NotificationManagerCompat.from(context)
            nManager.createNotificationChannel(TIMER_CHANNEL_ID, TIMER_CHANNEL_NAME, true)
            nManager.notify(timer, nBuilder.build())
        }

        fun showTimerRunning(context: Context) {

        }

        fun hideTimerNotification(context: Context, timer: Int) {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(timer)
        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean): NotificationCompat.Builder {
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_baseline_timer_24)
                    .setAutoCancel(true)
                    .setDefaults(0)
            if (playSound) nBuilder.setSound(notificationSound)

            return nBuilder
        }

        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent {
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
                    .addParentStack(javaClass)
                    .addNextIntentWithParentStack(resultIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun NotificationManagerCompat.createNotificationChannel(channelId: String, channelName: String, playSound: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelImportance =
                        if (playSound) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelId, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}