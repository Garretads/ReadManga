package ru.garretech.readmanga.adapters

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow


class CustomTableLayout(context: Context, attrs: AttributeSet) : TableLayout(context, attrs),
    View.OnClickListener {
    private var activeRadioButton: RadioButton? = null
    var activeRadioButtonIndex: Int = -1
    private var rbList = ArrayList<RadioButton>()

    val checkedRadioButtonId: Int
        get() = if (activeRadioButton != null) {
            activeRadioButton!!.id
        } else -1

    fun checkChildAt(position: Int) {
        var rb = rbList.get(position)
        rb.isChecked = true
        activeRadioButton = rb
        activeRadioButtonIndex = activeRadioButton!!.tag as Int
    }

    override fun onClick(v: View) {
        val rb = v as RadioButton
        if (activeRadioButton != null) {
            activeRadioButton!!.isChecked = false
        }
        rb.isChecked = true
        activeRadioButton = rb
        activeRadioButtonIndex = activeRadioButton!!.tag as Int
    }


    override fun addView(child: View, index: Int, params: android.view.ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        setChildrenOnClickListener(child as TableRow)
    }


    override fun addView(child: View, params: android.view.ViewGroup.LayoutParams) {
        super.addView(child, params)
        setChildrenOnClickListener(child as TableRow)
    }


    private fun setChildrenOnClickListener(tr: TableRow) {
        val c = tr.childCount
        for (i in 0 until c) {
            val v = tr.getChildAt(i)
            if (v is RadioButton) {
                rbList.add(v)
                v.setOnClickListener(this)
            }
        }
    }

    companion object {
        private val TAG = "ToggleButtonGroupTableLayout"
    }
}