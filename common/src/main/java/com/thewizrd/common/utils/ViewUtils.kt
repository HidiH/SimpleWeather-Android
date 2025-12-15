package com.thewizrd.common.utils

import android.view.View

val View.verticalPadding: Int
    get() = this.paddingTop + this.paddingBottom

val View.horizontalPaddingRelative: Int
    get() = this.paddingStart + this.paddingEnd

val View.horizontalPadding: Int
    get() = this.paddingLeft + this.paddingRight