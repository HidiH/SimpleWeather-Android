@file:JvmMultifileClass
@file:JvmName("PerfTrace")

package com.thewizrd.common.performance

import com.google.firebase.perf.metrics.Trace

class PerfTrace(private val name: String) {
    private val trace = Trace.create(name)

    fun startTrace() {
        trace.start()
    }

    fun stopTrace() {
        trace.stop()
    }

    fun putAttribute(name: String, value: String) {
        trace.putAttribute(name, value)
    }
}