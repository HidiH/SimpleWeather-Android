package com.thewizrd.simpleweather.ui.components.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.thewizrd.simpleweather.ui.compose.tools.WearPreviewDevices

@Composable
fun PreferenceCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 24.dp,
                end = 16.dp,
                bottom = 8.dp
            ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge
    )
}

@WearPreviewDevices
@Composable
private fun PreferenceCategoryPreview() {
    Box(
        modifier = Modifier.fillMaxRectangle()
    ) {
        PreferenceCategory(title = "Category")
    }
}