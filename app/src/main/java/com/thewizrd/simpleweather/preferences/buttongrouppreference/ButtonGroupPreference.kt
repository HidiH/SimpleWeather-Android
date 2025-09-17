package com.thewizrd.simpleweather.preferences.buttongrouppreference

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ArrayRes
import androidx.core.content.res.use
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonGroup
import com.google.android.material.button.MaterialButtonToggleGroup
import com.thewizrd.simpleweather.R


/**
 * A [Preference] that displays a list of entries as a [MaterialButtonGroup]. Based on [ListPreference]
 *
 *
 * This preference saves a string value. This string will be the value from the
 * [entryValues] array.
 *
 * @property entries android:entries
 * @property entryValues android:entryValues
 */
class ButtonGroupPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {
    private lateinit var mEntries: Array<CharSequence>
    private lateinit var mEntryValues: Array<CharSequence>
    private var mValue: String? = null
    private var mValueSet = false

    private lateinit var buttonGroup: MaterialButtonToggleGroup

    init {
        context.obtainStyledAttributes(
            attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes
        ).use {
            mEntries = it.getTextArray(R.styleable.ButtonGroupPreference_android_entries)
            mEntryValues = it.getTextArray(R.styleable.ButtonGroupPreference_android_entryValues)
        }

        layoutResource = R.layout.preference_widget_buttongroup
        isSelectable = false
    }

    constructor(
        context: Context,
        entries: Array<CharSequence>,
        entryValues: Array<CharSequence>
    ) : this(context) {
        mEntries = entries
        mEntryValues = entryValues
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.isDividerAllowedAbove = true
        holder.isDividerAllowedBelow = true

        buttonGroup = holder.findViewById(R.id.button_group) as MaterialButtonToggleGroup

        buttonGroup.removeAllViews()

        val entries = mEntries
        val entryValues = mEntryValues

        entries.forEachIndexed { index, entry ->
            val button = MaterialButton(holder.itemView.context).apply {
                id = View.generateViewId()
                text = entry
                isCheckable = true
                setOnClickListener {
                    value = entryValues[index].toString()
                    buttonGroup.check(id)
                }
            }

            buttonGroup.addView(button)

            if (TextUtils.equals(mValue, entryValues[index])) {
                buttonGroup.check(button.id)
            }
        }
    }

    var entries: Array<CharSequence>
        get() = mEntries
        set(value) {
            mEntries = value
        }

    fun setEntries(@ArrayRes entriesResId: Int) {
        entries = context.resources.getTextArray(entriesResId)
    }

    var entryValues: Array<CharSequence>
        get() = mEntryValues
        set(value) {
            mEntryValues = value
        }

    fun setEntryValues(@ArrayRes entryValuesResId: Int) {
        entryValues = context.resources.getTextArray(entryValuesResId)
    }

    var value: String?
        get() = mValue
        set(value) {
            // Always persist/notify the first time.
            val changed = !TextUtils.equals(mValue, value)
            if (changed || !mValueSet) {
                mValue = value
                mValueSet = true
                persistString(value)
                if (changed) {
                    notifyChanged()
                }
            }
        }

    fun getEntry(): CharSequence? {
        val index: Int = getValueIndex()
        return if (index >= 0) mEntries[index] else null
    }

    fun findIndexOfValue(value: String?): Int {
        if (value != null) {
            for (i in mEntryValues.indices.reversed()) {
                if (TextUtils.equals(mEntryValues[i].toString(), value)) {
                    return i
                }
            }
        }

        return -1
    }

    fun setValueIndex(index: Int) {
        value = mEntryValues[index].toString()
    }

    private fun getValueIndex(): Int {
        return findIndexOfValue(mValue)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedString(defaultValue as String?)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState = SavedState(superState)
        myState.mValue = value
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        value = myState.mValue
    }


    private class SavedState : BaseSavedState {
        var mValue: String? = null

        internal constructor(source: Parcel) : super(source) {
            mValue = source.readString()
        }

        internal constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(mValue)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(`in`: Parcel): SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

}