package com.thewizrd.shared_resources.utils

import android.util.SparseArray

enum class ColorMode(val value: Int) {
    DEFAULT(0),
    IMAGE(1);

    companion object {
        private val map = SparseArray<ColorMode>()

        init {
            for (type in entries) {
                map.put(type.value, type)
            }
        }

        fun valueOf(value: Int): ColorMode {
            return map[value, DEFAULT]
        }
    }
}