@file:JvmMultifileClass
@file:JvmName("PerfTrace")

package com.thewizrd.simpleweather.performance

import androidx.tracing.Trace
import kotlin.random.Random

class PerfTrace(private val name: String) {
    private val cookie = Random.nextInt()

    fun startTrace() {
        Trace.beginAsyncSection(name, cookie)
    }

    fun stopTrace() {
        Trace.endAsyncSection(name, cookie)
    }

    fun putAttribute(name: String, value: String) {
        // no-op
    }
}