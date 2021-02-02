package com.gsk.timerapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.viewpager.widget.PagerAdapter
import org.w3c.dom.Text

class SliderAdapter(val context: Context) : PagerAdapter() {

    val slide_images = arrayOf(
            R.drawable.onboard_drinking,
            R.drawable.onboard_stretching,
            R.drawable.onboard_walking
    )

    val slide_headings = arrayOf(
            "DRINK",
            "STRETCH",
            "WALK"
    )

    val slide_descs = arrayOf(
            "While sitting in front of a desk all day, we sometimes forget to meet the simplest bodily needs. For example The recommended daily fluid intake is 3 liters.",
            "Give yourself a mental break. Stretching also improves your mental state, as it reduces stress and releases tension.",
            "After a long day of hustle and grind, go for a walk to relieve some stress. Recharge and calm the mind to take on what's next."
    )

    override fun getCount(): Int {
        return slide_headings.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.slider_layout, container, false)

        val slideImageView = view.findViewById<ImageView>(R.id.slide_image)
        val slideHeading = view.findViewById<TextView>(R.id.slide_heading)
        val slideDescription = view.findViewById<TextView>(R.id.slide_desc)

        slideImageView.setImageResource(slide_images[position])
        slideHeading.setText(slide_headings[position])
        slideDescription.setText(slide_descs[position])

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }
}