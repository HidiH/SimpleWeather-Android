package com.thewizrd.common.utils

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

fun <T : RecyclerView.Adapter<*>> RecyclerView.Adapter<*>?.isAdapterOfTypeOrConcatContains(
    typeToFind: Class<T>
): Boolean {
    if (this == null) return false

    // 1. Direct check: Is the adapter itself of the specified type?
    if (typeToFind.isAssignableFrom(this.javaClass)) {
        return true
    }

    // 2. ConcatAdapter check: Is it a ConcatAdapter?
    if (this is ConcatAdapter) {
        // Iterate through the adapters within the ConcatAdapter
        for (innerAdapter in this.adapters) {
            // Recursively check each inner adapter.
            // This handles nested ConcatAdapters as well.
            if (innerAdapter.isAdapterOfTypeOrConcatContains(typeToFind)) {
                return true
            }
        }
    }

    // Not found directly or within a ConcatAdapter
    return false
}