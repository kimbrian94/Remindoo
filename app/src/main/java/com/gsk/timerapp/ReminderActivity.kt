package com.gsk.timerapp

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import com.gsk.timerapp.databinding.ActivityMainBinding
import com.gsk.timerapp.util.NotificationUtil
import com.gsk.timerapp.util.PrefUtil
import org.w3c.dom.Text
import java.util.*
import java.util.concurrent.Executors

class ReminderActivity : AppCompatActivity() {
    companion object {
        fun setAlarm(context: Context, timer: Int, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            intent.putExtra(TIMER_TYPE, timer)
            val pendingIntent = PendingIntent.getBroadcast(context, timer, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(context, timer, nowSeconds)
            return wakeUpTime
        }

        fun removeAlarm(context: Context, timer: Int) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
                    .putExtra(TIMER_TYPE, timer)
            val pendingIntent = PendingIntent.getBroadcast(context, timer, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(context, timer, 0)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var hydrateTimer: CountDownTimer
    private lateinit var stretchTimer: CountDownTimer
    private lateinit var walkTimer: CountDownTimer
    private lateinit var summaryDialog: Dialog

    private var hydrateLengthSeconds = 0L
    private var hydrateSecondsRemaining = 0L
    private var hydrateTimerState = TimerState.Stopped
    private var hydrateTimerStreak = 0
    private var stretchLengthSeconds = 0L
    private var stretchSecondsRemaining = 0L
    private var stretchTimerState = TimerState.Stopped
    private var stretchTimerStreak = 0
    private var walkLengthSeconds = 0L
    private var walkSecondsRemaining = 0L
    private var walkTimerState = TimerState.Stopped
    private var walkTimerStreak = 0
    private var dayOnGoing = false
    private var hydrateBackFront = false
    private var stretchBackFront = false
    private var walkBackFront = false

    private val tag = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val background = resources.getDrawable(R.drawable.gradient_background, theme)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.transparent, theme)
            window.setBackgroundDrawable(background)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hydrateTimer = object : CountDownTimer(hydrateLengthSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() { }
        }
        stretchTimer = object : CountDownTimer(stretchLengthSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() { }
        }
        walkTimer = object : CountDownTimer(walkLengthSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() { }
        }

        summaryDialog = Dialog(this)

        binding.content.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.content.mainButton.setOnClickListener {
            if (!dayOnGoing) {
                dayOnGoing = true
                startTimer(HYDRATE_TIMER)
                startTimer(STRETCH_TIMER)
                startTimer(WALK_TIMER)
                binding.content.mainButton.text = "End My Day"
            } else {
                dayOnGoing = false
                hydrateTimer.cancel()
                stretchTimer.cancel()
                walkTimer.cancel()
                onTimerFinished(HYDRATE_TIMER)
                onTimerFinished(STRETCH_TIMER)
                onTimerFinished(WALK_TIMER)
                displaySummary()
                hydrateTimerStreak = 0; stretchTimerStreak = 0; walkTimerStreak = 0
                binding.content.mainButton.text = "Start My Day!"
                if (hydrateBackFront && stretchBackFront && walkBackFront) {
                    animateCardOut(HYDRATE_TIMER)
                    animateCardOut(STRETCH_TIMER)
                    animateCardOut(WALK_TIMER)
                }
                if (hydrateBackFront) animateCardOut(HYDRATE_TIMER)
                if (stretchBackFront) animateCardOut(STRETCH_TIMER)
                if (walkBackFront) animateCardOut(WALK_TIMER)
                updateButtons()
                updateCountDownUI()
                updateAdditionalInfo()
            }
        }

        initAnimators()
        restoreCardUI()

        Log.i(tag, "onCreate")
    }

    private fun restoreCardUI() {
        dayOnGoing = PrefUtil.getDayOnGoing(this)
        hydrateTimerState = PrefUtil.getTimerState(this, HYDRATE_TIMER)
        stretchTimerState = PrefUtil.getTimerState(this, STRETCH_TIMER)
        walkTimerState = PrefUtil.getTimerState(this, WALK_TIMER)
        if (dayOnGoing && hydrateTimerState == TimerState.Stopped) {
            binding.content.hydrateCardViewFront.alpha = 0F
            binding.content.hydrateCardViewBack.alpha = 1F
            hydrateBackFront = true
            binding.content.hydrateCardViewBack.setOnClickListener {
                animateCardOut(HYDRATE_TIMER)
                startTimer(HYDRATE_TIMER)
            }
        }
        if (dayOnGoing && stretchTimerState == TimerState.Stopped) {
            binding.content.stretchCardViewFront.alpha = 0F
            binding.content.stretchCardViewBack.alpha = 1F
            stretchBackFront = true
            binding.content.stretchCardViewBack.setOnClickListener {
                animateCardOut(STRETCH_TIMER)
                startTimer(STRETCH_TIMER)
            }
        }
        if (dayOnGoing && walkTimerState == TimerState.Stopped) {
            binding.content.walkCardViewFront.alpha = 0F
            binding.content.walkCardViewBack.alpha = 1F
            walkBackFront = true
            binding.content.walkCardViewBack.setOnClickListener {
                animateCardOut(WALK_TIMER)
                startTimer(WALK_TIMER)
            }
        }
    }

    private fun initAnimators() {
        val scale: Float = applicationContext.resources.displayMetrics.density
        binding.content.hydrateCardViewFront.cameraDistance = 8000 * scale
        binding.content.hydrateCardViewBack.cameraDistance = 8000 * scale
        binding.content.stretchCardViewFront.cameraDistance = 8000 * scale
        binding.content.stretchCardViewBack.cameraDistance = 8000 * scale
        binding.content.walkCardViewFront.cameraDistance = 8000 * scale
        binding.content.walkCardViewBack.cameraDistance = 8000 * scale
    }

    override fun onPause() {
        super.onPause()

        if (hydrateTimerState == TimerState.Running) {
            hydrateTimer.cancel()
            val wakeUpTime = setAlarm(this, HYDRATE_TIMER, nowSeconds, hydrateSecondsRemaining)
        }
        if (stretchTimerState == TimerState.Running) {
            stretchTimer.cancel()
            val wakeUpTime = setAlarm(this, STRETCH_TIMER, nowSeconds, stretchSecondsRemaining)
        }
        if (walkTimerState == TimerState.Running) {
            walkTimer.cancel()
            val wakeUpTime = setAlarm(this, WALK_TIMER, nowSeconds, walkSecondsRemaining)
        }
        PrefUtil.setDayOnGoing(this, dayOnGoing)

        PrefUtil.setPreviousTimerLengthSeconds(this, HYDRATE_TIMER, hydrateLengthSeconds)
        PrefUtil.setSecondsRemaining(this, HYDRATE_TIMER, hydrateSecondsRemaining)
        PrefUtil.setTimerState(this, HYDRATE_TIMER, hydrateTimerState)
        PrefUtil.setTimerStreak(this, HYDRATE_TIMER, hydrateTimerStreak)

        PrefUtil.setPreviousTimerLengthSeconds(this, STRETCH_TIMER, stretchLengthSeconds)
        PrefUtil.setSecondsRemaining(this, STRETCH_TIMER, stretchSecondsRemaining)
        PrefUtil.setTimerState(this, STRETCH_TIMER, stretchTimerState)
        PrefUtil.setTimerStreak(this, STRETCH_TIMER, stretchTimerStreak)

        PrefUtil.setPreviousTimerLengthSeconds(this, WALK_TIMER, walkLengthSeconds)
        PrefUtil.setSecondsRemaining(this, WALK_TIMER, walkSecondsRemaining)
        PrefUtil.setTimerState(this, WALK_TIMER, walkTimerState)
        PrefUtil.setTimerStreak(this, WALK_TIMER, walkTimerStreak)
    }

    override fun onResume() {
        super.onResume()

        initTimers()
        NotificationUtil.hideTimerNotification(this, HYDRATE_TIMER)
        NotificationUtil.hideTimerNotification(this, STRETCH_TIMER)
        NotificationUtil.hideTimerNotification(this, WALK_TIMER)
    }

    private fun initTimers() {
        dayOnGoing = PrefUtil.getDayOnGoing(this)
        hydrateTimerState = PrefUtil.getTimerState(this, HYDRATE_TIMER)
        stretchTimerState = PrefUtil.getTimerState(this, STRETCH_TIMER)
        walkTimerState = PrefUtil.getTimerState(this, WALK_TIMER)

        hydrateTimerStreak = PrefUtil.getTimerStreak(this, HYDRATE_TIMER)
        stretchTimerStreak = PrefUtil.getTimerStreak(this, STRETCH_TIMER)
        walkTimerStreak = PrefUtil.getTimerStreak(this, WALK_TIMER)

        if (hydrateTimerState == TimerState.Stopped) setNewTimerLength(HYDRATE_TIMER) else setPreviousTimerLength(HYDRATE_TIMER)
        if (stretchTimerState == TimerState.Stopped) setNewTimerLength(STRETCH_TIMER) else setPreviousTimerLength(STRETCH_TIMER)
        if (walkTimerState == TimerState.Stopped) setNewTimerLength(WALK_TIMER) else setPreviousTimerLength(WALK_TIMER)

        hydrateSecondsRemaining = if (hydrateTimerState == TimerState.Running || hydrateTimerState == TimerState.Paused || (hydrateTimerState == TimerState.Stopped && dayOnGoing))
            PrefUtil.getSecondsRemaining(this, HYDRATE_TIMER)
        else hydrateLengthSeconds

        stretchSecondsRemaining = if (stretchTimerState == TimerState.Running || stretchTimerState == TimerState.Paused || (stretchTimerState == TimerState.Stopped && dayOnGoing))
            PrefUtil.getSecondsRemaining(this, STRETCH_TIMER)
        else stretchLengthSeconds

        walkSecondsRemaining = if (walkTimerState == TimerState.Running || walkTimerState == TimerState.Paused || (walkTimerState == TimerState.Stopped && dayOnGoing))
            PrefUtil.getSecondsRemaining(this, WALK_TIMER)
        else walkLengthSeconds

        val hydrateAlarmSetTime = PrefUtil.getAlarmSetTime(this, HYDRATE_TIMER)
        val stretchAlarmSetTime = PrefUtil.getAlarmSetTime(this, STRETCH_TIMER)
        val walkAlarmSetTime = PrefUtil.getAlarmSetTime(this, WALK_TIMER)
        //change secondsRemaining according to the where the background timer stopped
        if (hydrateAlarmSetTime > 0) hydrateSecondsRemaining -= nowSeconds - hydrateAlarmSetTime //nowSeconds - alarmSetTime is the amount of time background timer spent
        if (stretchAlarmSetTime > 0) stretchSecondsRemaining -= nowSeconds - stretchAlarmSetTime
        if (walkAlarmSetTime > 0) walkSecondsRemaining -= nowSeconds - walkAlarmSetTime

        removeAlarm(this, HYDRATE_TIMER)
        removeAlarm(this, STRETCH_TIMER)
        removeAlarm(this, WALK_TIMER)

        if (hydrateSecondsRemaining <= 0)
            onTimerFinished(HYDRATE_TIMER)
        else if (hydrateTimerState == TimerState.Running)
            startTimer(HYDRATE_TIMER)
        if (stretchSecondsRemaining <= 0)
            onTimerFinished(STRETCH_TIMER)
        else if (stretchTimerState == TimerState.Running)
            startTimer(STRETCH_TIMER)
        if (walkSecondsRemaining <= 0)
            onTimerFinished(WALK_TIMER)
        else if (walkTimerState == TimerState.Running)
            startTimer(WALK_TIMER)

        updateAdditionalInfo()
        updateButtons()
        updateCountDownUI()
    }

    private fun setNewTimerLength(timer: Int) {
        val lengthInMinutes = PrefUtil.getTimerLength(this, timer)
        val lengthInSeconds = lengthInMinutes * 60L
        when (timer) {
            HYDRATE_TIMER -> {
                hydrateLengthSeconds = lengthInSeconds
                binding.content.hydrateProgressbar.max = hydrateLengthSeconds.toInt()
            }
            STRETCH_TIMER -> {
                stretchLengthSeconds = lengthInSeconds
                binding.content.stretchProgressbar.max = stretchLengthSeconds.toInt()
            }
            WALK_TIMER -> {
                walkLengthSeconds = lengthInSeconds
                binding.content.walkProgressbar.max = walkLengthSeconds.toInt()
            }
        }
    }

    private fun setPreviousTimerLength(timer: Int) {
        when (timer) {
            HYDRATE_TIMER -> {
                hydrateLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this, timer)
                binding.content.hydrateProgressbar.max = hydrateLengthSeconds.toInt()
            }
            STRETCH_TIMER -> {
                stretchLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this, timer)
                binding.content.stretchProgressbar.max = stretchLengthSeconds.toInt()
            }
            WALK_TIMER -> {
                walkLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this, timer)
                binding.content.walkProgressbar.max = walkLengthSeconds.toInt()
            }
        }
    }
    private fun startTimer(timer: Int) {
        when (timer) {
            HYDRATE_TIMER -> {
                hydrateTimerState = TimerState.Running
                hydrateTimer = object : CountDownTimer(hydrateSecondsRemaining * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        hydrateSecondsRemaining = millisUntilFinished / 1000
                        updateCountDownUI()
                        updateButtons()
                    }
                    override fun onFinish() = onTimerFinished(timer)
                }.start()
            }
            STRETCH_TIMER -> {
                stretchTimerState = TimerState.Running
                stretchTimer = object : CountDownTimer(stretchSecondsRemaining * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        stretchSecondsRemaining = millisUntilFinished / 1000
                        updateCountDownUI()
                        updateButtons()
                    }
                    override fun onFinish() = onTimerFinished(timer)
                }.start()
            }
            WALK_TIMER -> {
                walkTimerState = TimerState.Running
                walkTimer = object : CountDownTimer(walkSecondsRemaining * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        walkSecondsRemaining = millisUntilFinished / 1000
                        updateCountDownUI()
                        updateButtons()
                    }
                    override fun onFinish() = onTimerFinished(timer)
                }.start()
            }
        }
    }

    private fun updateButtons() {
        binding.content.mainButton.text = if (dayOnGoing)
            "End My Day"
        else "Start My Day"
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountDownUI() {
        var minutesUntilFinished = hydrateSecondsRemaining / 60
        var secondsUntilFinished = hydrateSecondsRemaining - minutesUntilFinished * 60
        var secondsStr = secondsUntilFinished.toString()
        binding.content.hydrateTimeText.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        binding.content.hydrateProgressbar.progress = (hydrateLengthSeconds - hydrateSecondsRemaining).toInt()

        minutesUntilFinished = stretchSecondsRemaining / 60
        secondsUntilFinished = stretchSecondsRemaining - minutesUntilFinished * 60
        secondsStr = secondsUntilFinished.toString()
        binding.content.stretchTimeText.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        binding.content.stretchProgressbar.progress = (stretchLengthSeconds - stretchSecondsRemaining).toInt()

        val hoursUntilFinished = walkSecondsRemaining / 3600
        minutesUntilFinished = (walkSecondsRemaining - hoursUntilFinished * 3600) / 60
        secondsUntilFinished = walkSecondsRemaining - hoursUntilFinished * 3600 - minutesUntilFinished * 60
        secondsStr = secondsUntilFinished.toString()
        val minutesStr = minutesUntilFinished.toString()

        binding.content.walkTimeText.textSize = if (hoursUntilFinished != 0L) 25F else 30F
        binding.content.walkTimeText.text = "${ if (hoursUntilFinished != 0L) hoursUntilFinished.toString() + ":" else "" }${
            if (minutesStr.length == 1 && hoursUntilFinished != 0L) "0" + minutesStr + ":" 
            else minutesStr + ":"}${
                if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        binding.content.walkProgressbar.progress = (walkLengthSeconds - walkSecondsRemaining).toInt()

    }

    private fun updateAdditionalInfo() {
        val hydrateCycle = PrefUtil.getTimerLength(this, HYDRATE_TIMER)
        val stretchCycle = PrefUtil.getTimerLength(this, STRETCH_TIMER)
        val walkCycle = PrefUtil.getTimerLength(this, WALK_TIMER)
        val walkHours = walkCycle / 60
        val walkMinutes = walkCycle - walkHours * 60

        binding.content.hydrateCycleText.text = "Cycle : ${hydrateCycle.toString()}m"
        binding.content.stretchCycleText.text = "Cycle : ${stretchCycle.toString()}m"
        binding.content.walkCycleText.text = "Cycle : ${ if (walkHours != 0) walkHours.toString() + "h " else ""}${
            if (walkMinutes != 0) walkMinutes.toString() + "m" else ""}"

        binding.content.hydrateStreakText.text = "Streak : ${hydrateTimerStreak.toString()}"
        binding.content.stretchStreakText.text = "Streak : ${stretchTimerStreak.toString()}"
        binding.content.walkStreakText.text = "Streak : ${walkTimerStreak.toString()}"
    }

    private fun onTimerFinished(timer: Int) {
        when (timer) {
            HYDRATE_TIMER -> {
                hydrateTimerState = TimerState.Stopped
                setNewTimerLength(timer)
                hydrateSecondsRemaining = hydrateLengthSeconds
                binding.content.hydrateProgressbar.progress = 0
            }
            STRETCH_TIMER -> {
                stretchTimerState = TimerState.Stopped
                setNewTimerLength(timer)
                stretchSecondsRemaining = stretchLengthSeconds
                binding.content.stretchProgressbar.progress = 0
            }
            WALK_TIMER -> {
                walkTimerState = TimerState.Stopped
                setNewTimerLength(timer)
                walkSecondsRemaining = walkLengthSeconds
                binding.content.walkProgressbar.progress = 0
            }
        }
        if (dayOnGoing)
            animateCardIn(timer)
    }

    private fun animateCardIn(timer: Int) {
        val front_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.front_animator) as AnimatorSet
        val back_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.back_animator) as AnimatorSet

        when (timer) {
            HYDRATE_TIMER -> {
                front_anim.setTarget(binding.content.hydrateCardViewFront)
                back_anim.setTarget(binding.content.hydrateCardViewBack)
                hydrateBackFront = true
                hydrateTimerStreak++
                binding.content.hydrateCardViewBack.setOnClickListener {
                    animateCardOut(timer)
                    startTimer(HYDRATE_TIMER)
                }
            }
            STRETCH_TIMER -> {
                front_anim.setTarget(binding.content.stretchCardViewFront)
                back_anim.setTarget(binding.content.stretchCardViewBack)
                stretchBackFront = true
                stretchTimerStreak++
                binding.content.stretchCardViewBack.setOnClickListener {
                    animateCardOut(timer)
                    startTimer(STRETCH_TIMER)
                }
            }
            WALK_TIMER -> {
                front_anim.setTarget(binding.content.walkCardViewFront)
                back_anim.setTarget(binding.content.walkCardViewBack)
                walkBackFront = true
                walkTimerStreak++
                binding.content.walkCardViewBack.setOnClickListener {
                    animateCardOut(timer)
                    startTimer(WALK_TIMER)
                }
            }
        }
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            handler.post {
                front_anim.start()
                back_anim.start()
                updateAdditionalInfo()
            }
        }
    }

    private fun animateCardOut(timer: Int) {
        val front_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.front_animator) as AnimatorSet
        val back_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.back_animator) as AnimatorSet
        when (timer) {
            HYDRATE_TIMER -> {
                front_anim.setTarget(binding.content.hydrateCardViewBack)
                back_anim.setTarget(binding.content.hydrateCardViewFront)
                binding.content.hydrateCardViewBack.isClickable = false
                hydrateBackFront = false
            }
            STRETCH_TIMER -> {
                front_anim.setTarget(binding.content.stretchCardViewBack)
                back_anim.setTarget(binding.content.stretchCardViewFront)
                binding.content.stretchCardViewBack.isClickable = false
                stretchBackFront = false
            }
            WALK_TIMER -> {
                front_anim.setTarget(binding.content.walkCardViewBack)
                back_anim.setTarget(binding.content.walkCardViewFront)
                binding.content.walkCardViewBack.isClickable = false
                walkBackFront = false
            }
        }
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            handler.post {
                front_anim.start()
                back_anim.start()
                updateButtons()
                updateCountDownUI()
            }
        }
    }

    fun displaySummary() {
        summaryDialog.setContentView(R.layout.popup_dialog_layout)
        summaryDialog.window?.setBackgroundDrawable(getDrawable(android.R.color.transparent))

        val button = summaryDialog.findViewById<Button>(R.id.dismiss_button)
        val hydrate_summary = summaryDialog.findViewById<TextView>(R.id.hydrate_summary_text)
        val stretch_summary = summaryDialog.findViewById<TextView>(R.id.stretch_summary_text)
        val walk_summary = summaryDialog.findViewById<TextView>(R.id.walk_summary_text)

        hydrate_summary.text = "Drank : ${hydrateTimerStreak.toString()} time(s) today"
        stretch_summary.text = "Stretched : ${stretchTimerStreak.toString()} time(s) today"
        walk_summary.text = "Walked : ${walkTimerStreak.toString()} time(s) today"
        button.setOnClickListener { summaryDialog.dismiss() }
        summaryDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}