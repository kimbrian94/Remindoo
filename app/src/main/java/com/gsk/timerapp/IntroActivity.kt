package com.gsk.timerapp

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.PrecomputedText
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.slider.Slider
import com.gsk.timerapp.databinding.ActivityIntroBinding
import com.gsk.timerapp.util.PrefUtil
import org.w3c.dom.Text

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    private lateinit var mDots: Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val background = resources.getDrawable(R.drawable.onboard_background, theme)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.transparent, theme)
            window.setBackgroundDrawable(background)
        }
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firstRunning = PrefUtil.getFirstRunning(this)

        if (!firstRunning) {
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
            finish()
        }

        val viewPager = binding.slideViewPager
        val sliderAdapter = SliderAdapter(this)

        binding.slideViewPager.adapter = sliderAdapter

        addDotsIndicator(0)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                addDotsIndicator(position)

                if (position == mDots.count() - 1) {
                    binding.finishButton.visibility = View.VISIBLE
                    binding.finishButton.isEnabled = true
                } else {
                    binding.finishButton.visibility = View.INVISIBLE
                    binding.finishButton.isEnabled = false
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        binding.finishButton.setOnClickListener {
            PrefUtil.setFirstRunning(this, false)
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun addDotsIndicator(position: Int){
        mDots = Array<TextView>(3){ TextView(this) }
        binding.dotsLayout.removeAllViews()

        for (i in 0..mDots.count()-1){
            mDots[i].setText(Html.fromHtml("&#8226;", 0))
            mDots[i].textSize = 35F
            mDots[i].setTextColor(resources.getColor(R.color.colorTransparentWhite, theme))
            binding.dotsLayout.addView(mDots[i])
        }


        mDots[position].setTextColor(resources.getColor(R.color.white, theme))
    }

}