package ru.garretech.readmanga.adapters

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class MangaViewPager(context: Context, attr: AttributeSet?) : ViewPager(context, attr) {

    override fun onInterceptTouchEvent(ev: MotionEvent?) =
        try {
            super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            //uncomment if you really want to see these errors
            //e.printStackTrace();
            false
        }

}