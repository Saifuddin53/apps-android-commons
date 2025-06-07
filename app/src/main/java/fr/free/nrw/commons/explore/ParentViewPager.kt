package fr.free.nrw.commons.explore

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * ParentViewPager A custom ViewPager whose scrolling can be enabled or disabled.
 */
class ParentViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewPager(context, attrs) {

    /**
     * Boolean variable that stores the current state of pager scroll i.e(enabled or disabled)
     */
    var canScroll: Boolean = true

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return canScroll && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return canScroll && super.onInterceptTouchEvent(ev)
    }
}
