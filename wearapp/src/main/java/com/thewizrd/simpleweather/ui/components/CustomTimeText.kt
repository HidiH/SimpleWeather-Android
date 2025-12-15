package com.thewizrd.simpleweather.ui.components

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.basicCurvedText
import androidx.wear.compose.foundation.weight
import androidx.wear.compose.material3.TimeSource
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TimeTextDefaults
import androidx.wear.compose.material3.curvedText
import androidx.wear.compose.material3.timeTextCurvedText
import androidx.wear.compose.material3.timeTextSeparator
import androidx.wear.tooling.preview.devices.WearDevices
import com.thewizrd.simpleweather.BuildConfig

/**
 * Custom version of TimeText (Curved Text) that enables leading text (if wanted) and hides while
 * scrolling so user can just focus on the list's items.
 */
@Composable
fun CustomTimeText(
    visible: Boolean,
    modifier: Modifier = Modifier,
    startText: String? = null,
    timeSource: TimeSource = TimeTextDefaults.rememberTimeSource(TimeTextDefaults.timeFormat())
) {
    val textStyle = TimeTextDefaults.timeTextStyle()
    val debugWarning = remember {
        val isEmulator = Build.PRODUCT.startsWith("sdk_gwear")

        if (BuildConfig.DEBUG && !isEmulator) {
            "Debug (slower)"
        } else {
            null
        }
    }
    val showWarning = debugWarning != null
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val visibleText = startText != null
        TimeText(
            modifier = modifier,
            timeSource = timeSource,
            content = { time ->
                if (visibleText) {
                    curvedText(
                        text = startText,
                        modifier = CurvedModifier.weight(1f),
                        overflow = TextOverflow.Ellipsis
                    )
                    timeTextSeparator()
                }
                timeTextCurvedText(time)
                if (showWarning) {
                    timeTextSeparator()
                    basicCurvedText(
                        text = debugWarning,
                        modifier = CurvedModifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        style = textStyle.copy(color = Color.Red)
                    )
                }
            },
        )
    }
}

@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = WearDevices.LARGE_ROUND
)
@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = WearDevices.SQUARE
)
@Preview(
    apiLevel = 34,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = WearDevices.SMALL_ROUND
)
// This will only be rendered properly in AS Chipmunk and beyond
@Composable
fun PreviewCustomTimeText() {
    CustomTimeText(
        visible = true,
        startText = "Testing Leading Text..."
    )
}