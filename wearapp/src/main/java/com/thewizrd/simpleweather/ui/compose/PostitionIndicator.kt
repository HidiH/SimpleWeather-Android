package com.thewizrd.simpleweather.ui.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.PositionIndicatorDefaults

@Composable
fun LazyGridPositionIndicator(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier,
    reverseDirection: Boolean = false,
    fadeInAnimationSpec: AnimationSpec<Float> = PositionIndicatorDefaults.visibilityAnimationSpec,
    fadeOutAnimationSpec: AnimationSpec<Float> = PositionIndicatorDefaults.visibilityAnimationSpec,
    positionAnimationSpec: AnimationSpec<Float> =
        PositionIndicatorDefaults.positionAnimationSpec
) = androidx.wear.compose.material.PositionIndicator(
    LazyGridStateAdapter(lazyGridState),
    indicatorHeight = 50.dp,
    indicatorWidth = 4.dp,
    paddingHorizontal = 2.dp,
    modifier = modifier,
    reverseDirection = reverseDirection,
    fadeInAnimationSpec = fadeInAnimationSpec,
    fadeOutAnimationSpec = fadeOutAnimationSpec,
    positionAnimationSpec = positionAnimationSpec
)