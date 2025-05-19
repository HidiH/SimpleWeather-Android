package com.thewizrd.common.helpers

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.provider.Browser
import android.text.style.URLSpan
import android.view.View
import androidx.core.net.toUri

class CustomChromeTabSpan(url: String?) : URLSpan(url) {
    constructor(src: Parcel) : this(src.readString())

    override fun onClick(widget: View) {
        val uri = url.toUri()
        val context = widget.context
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
        // Custom Tab
        intent.putExtras(Bundle().apply {
            putBinder("android.support.customtabs.extra.SESSION", null)
        })

        runCatching {
            context.startActivity(intent)
        }
    }
}