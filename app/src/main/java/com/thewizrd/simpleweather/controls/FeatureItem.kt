package com.thewizrd.simpleweather.controls

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import com.thewizrd.shared_resources.di.settingsManager
import com.thewizrd.shared_resources.utils.Colors
import com.thewizrd.shared_resources.utils.ContextUtils.getAttrColor
import com.thewizrd.shared_resources.utils.UserThemeMode
import com.thewizrd.simpleweather.R

class FeatureItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs), Checkable {
    companion object {
        private val CHECKABLE_STATE_SET = intArrayOf(android.R.attr.state_checkable)
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }

    private val titleTextView: TextView
    private val dragHandle: View

    var isCheckable: Boolean = true
        set(value) {
            field = value
            refreshDrawableState()
        }

    var isDraggable: Boolean = true
        set(value) {
            field = value
            dragHandle.isVisible = value
        }

    private var _isChecked = false

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_featureitem, this, true)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(
                if (settingsManager.getUserThemeMode() == UserThemeMode.AMOLED_DARK) {
                    Colors.BLACK
                } else {
                    context.getAttrColor(R.attr.colorSurface)
                }
            )
            initializeElevationOverlay(context)
            elevation = 0f
        }

        titleTextView = findViewById(R.id.title)
        dragHandle = findViewById(R.id.drag_handle)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        dragHandle.setOnLongClickListener(l)
    }

    fun setTitle(text: CharSequence) {
        titleTextView.text = text
    }

    fun setTitle(@StringRes resId: Int) {
        titleTextView.setText(resId)
    }

    override fun setChecked(checked: Boolean) {
        _isChecked = checked
        refreshDrawableState()
    }

    override fun isChecked(): Boolean {
        return _isChecked
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 3)

        if (isCheckable) {
            mergeDrawableStates(drawableState, CHECKABLE_STATE_SET)
        }

        if (isChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }

        if (isEnabled) {
            mergeDrawableStates(drawableState, ENABLED_STATE_SET)
        }

        return drawableState
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        if (background is MaterialShapeDrawable) {
            (background as? MaterialShapeDrawable)?.elevation = elevation
        }
    }
}