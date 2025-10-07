package com.thewizrd.simpleweather.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.thewizrd.simpleweather.ui.compose.CircularWavyProgressIndicator

@Composable
fun <T : Any> LoadingPagingContent(
    pagingItems: LazyPagingItems<T>,
    content: @Composable () -> Unit
) {
    val loadingState = remember(pagingItems.loadState) {
        pagingItems.loadState.refresh
    }

    if (pagingItems.itemCount == 0 || loadingState is LoadState.Loading) {
        Box(
            modifier = Modifier.fillMaxRectangle(),
            contentAlignment = Alignment.Center
        ) {
            CircularWavyProgressIndicator(
                trackColor = Color.Transparent
            )
        }
    } else {
        content()
    }
}