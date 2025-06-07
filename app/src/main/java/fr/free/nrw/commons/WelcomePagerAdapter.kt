package fr.free.nrw.commons

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class WelcomePagerAdapter : PagerAdapter() {
    private val pageLayouts = intArrayOf(
        R.layout.welcome_wikipedia,
        R.layout.welcome_do_upload,
        R.layout.welcome_dont_upload,
        R.layout.welcome_image_example,
        R.layout.welcome_final
    )

    override fun getCount(): Int = pageLayouts.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val layout = inflater.inflate(pageLayouts[position], container, false) as ViewGroup
        if (position == pageLayouts.size - 1) {
            val moreInfo: TextView = layout.findViewById(R.id.welcomeInfo)
            Utils.setUnderlinedText(moreInfo, R.string.welcome_help_button_text, container.context)
            moreInfo.setOnClickListener {
                Utils.handleWebUrl(container.context, Uri.parse("https://commons.wikimedia.org/wiki/Help:Contents"))
            }
            layout.findViewById<View>(R.id.finishTutorialButton)
                .setOnClickListener { (container.context as WelcomeActivity).finishTutorial() }
        }
        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}

