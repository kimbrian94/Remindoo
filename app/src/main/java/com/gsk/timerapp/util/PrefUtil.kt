package com.gsk.timerapp.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.gsk.timerapp.*

class PrefUtil {
    companion object {
        fun setDayOnGoing(context: Context, dayStarted: Boolean) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putBoolean(DAY_ONGOING_ID, dayStarted)
            editor.apply()
        }

        fun getDayOnGoing(context: Context): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(DAY_ONGOING_ID, false)
        }

        fun getTimerLength(context: Context, timer: Int): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var minutes = 0
            when (timer) {
                HYDRATE_TIMER -> {
                    minutes = preferences.getInt(HYDRATE_TIMER_LENGTH_ID, 30)
                }
                STRETCH_TIMER -> {
                    minutes = preferences.getInt(STRETCH_TIMER_LENGTH_ID, 30)
                }
                WALK_TIMER -> {
                    minutes = preferences.getInt(WALK_TIMER_LENGTH_ID, 30)
                }
            }
            return minutes
        }

        fun setPreviousTimerLengthSeconds(context: Context, timer: Int, seconds: Long) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            when (timer) {
                HYDRATE_TIMER -> {
                    editor.putLong(HYDRATE_PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
                }
                STRETCH_TIMER -> {
                    editor.putLong(STRETCH_PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
                }
                WALK_TIMER -> {
                    editor.putLong(WALK_PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
                }
            }
            editor.apply()
        }

        fun getPreviousTimerLengthSeconds(context: Context, timer: Int): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var seconds = 0L
            when (timer) {
                HYDRATE_TIMER -> {
                    seconds = preferences.getLong(HYDRATE_PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
                }
                STRETCH_TIMER -> {
                    seconds = preferences.getLong(STRETCH_PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
                }
                WALK_TIMER -> {
                    seconds = preferences.getLong(WALK_PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
                }
            }
            return seconds
        }

        fun setSecondsRemaining (context: Context, timer: Int, seconds: Long) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            when (timer) {
                HYDRATE_TIMER -> {
                    editor.putLong(HYDRATE_SECONDS_REMAINING_ID, seconds)
                }
                STRETCH_TIMER -> {
                    editor.putLong(STRETCH_SECONDS_REMAINING_ID, seconds)
                }
                WALK_TIMER -> {
                    editor.putLong(WALK_SECONDS_REMAINING_ID, seconds)
                }
            }
            editor.apply()
        }

        fun getSecondsRemaining (context: Context, timer: Int): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var seconds = 0L
            when (timer) {
                HYDRATE_TIMER -> {
                    seconds = preferences.getLong(HYDRATE_SECONDS_REMAINING_ID, 0)
                }
                STRETCH_TIMER -> {
                    seconds = preferences.getLong(STRETCH_SECONDS_REMAINING_ID, 0)
                }
                WALK_TIMER -> {
                    seconds = preferences.getLong(WALK_SECONDS_REMAINING_ID, 0)
                }
            }
            return seconds
        }

        fun setTimerState (context: Context, timer: Int, state: ReminderActivity.TimerState) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            when (timer) {
                HYDRATE_TIMER -> {
                    editor.putInt(HYDRATE_TIMER_STATE_ID, ordinal)
                }
                STRETCH_TIMER -> {
                    editor.putInt(STRETCH_TIMER_STATE_ID, ordinal)
                }
                WALK_TIMER -> {
                    editor.putInt(WALK_TIMER_STATE_ID, ordinal)
                }
            }
            editor.apply()
        }

        fun getTimerState (context: Context, timer: Int): ReminderActivity.TimerState {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var ordinal = 0
            when (timer) {
                HYDRATE_TIMER -> {
                    ordinal = preferences.getInt(HYDRATE_TIMER_STATE_ID, 0)
                }
                STRETCH_TIMER -> {
                    ordinal = preferences.getInt(STRETCH_TIMER_STATE_ID, 0)
                }
                WALK_TIMER -> {
                    ordinal = preferences.getInt(WALK_TIMER_STATE_ID, 0)
                }
            }
            return ReminderActivity.TimerState.values()[ordinal]
        }

        fun setAlarmSetTime (context: Context, timer: Int, time: Long) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            when (timer) {
                HYDRATE_TIMER -> {
                    editor.putLong(HYDRATE_ALARM_SET_TIME_ID, time)
                }
                STRETCH_TIMER -> {
                    editor.putLong(STRETCH_ALARM_SET_TIME_ID, time)
                }
                WALK_TIMER -> {
                    editor.putLong(WALK_ALARM_SET_TIME_ID, time)
                }
            }
            editor.apply()
        }

        fun getAlarmSetTime (context: Context, timer: Int): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var alarmSetSeconds = 0L
            when (timer) {
                HYDRATE_TIMER -> {
                    alarmSetSeconds = preferences.getLong(HYDRATE_ALARM_SET_TIME_ID, 0)
                }
                STRETCH_TIMER -> {
                    alarmSetSeconds = preferences.getLong(STRETCH_ALARM_SET_TIME_ID, 0)
                }
                WALK_TIMER -> {
                    alarmSetSeconds = preferences.getLong(WALK_ALARM_SET_TIME_ID, 0)
                }
            }
            return alarmSetSeconds
        }

        fun setTimerStreak(context: Context, timer: Int, todayStreak: Int) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            when (timer) {
                HYDRATE_TIMER -> {
                    editor.putInt(HYDRATE_TIMER_STREAK_ID, todayStreak)
                }
                STRETCH_TIMER -> {
                    editor.putInt(STRETCH_TIMER_STREAK_ID, todayStreak)
                }
                WALK_TIMER -> {
                    editor.putInt(WALK_TIMER_STREAK_ID, todayStreak)
                }
            }
            editor.apply()
        }

        fun getTimerStreak(context: Context, timer: Int) : Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var streaks = 0
            when (timer) {
                HYDRATE_TIMER -> {
                    streaks = preferences.getInt(HYDRATE_TIMER_STREAK_ID, 0)
                }
                STRETCH_TIMER -> {
                    streaks = preferences.getInt(STRETCH_TIMER_STREAK_ID, 0)
                }
                WALK_TIMER -> {
                    streaks = preferences.getInt(WALK_TIMER_STREAK_ID, 0)
                }
            }
            return streaks
        }

        fun setFirstRunning(context: Context, firstRunning: Boolean) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putBoolean(FIRST_RUNNING, firstRunning)
            editor.apply()
        }

        fun getFirstRunning(context: Context) : Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val firstRunning = preferences.getBoolean(FIRST_RUNNING , true)
            return firstRunning
        }
    }
}