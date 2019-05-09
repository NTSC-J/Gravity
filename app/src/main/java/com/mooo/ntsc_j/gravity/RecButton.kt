package com.mooo.ntsc_j.gravity

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.Button

class RecButton(context: Context, attrs: AttributeSet?) : Button(context, attrs), View.OnClickListener {
    var recording = false
        private set(value) {
            field = value
            text = if (value) "■" else "●"
            setTextColor(if(value) Color.BLACK else Color.RED)
            if (value)
                onStartRec?.invoke()
            else
                onStopRec?.invoke()
        }

    init {
        recording = false
    }

    var onStartRec: (() -> Unit)? = null
    var onStopRec: (() -> Unit)? = null

    override fun onClick(v: View?) {
        recording = !recording
    }
}
