package com.thewizrd.simpleweather.ui.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.wear.compose.foundation.ScrollInfoProvider
import androidx.wear.compose.material.PositionIndicatorState
import androidx.wear.compose.material.PositionIndicatorVisibility
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * An implementation of [PositionIndicatorState] to display the amount and position of a
 * [LazyVerticalGrid] or [LazyHorizontalGrid] component via its [LazyGridState].
 *
 * Note that size and position calculations ignore spacing between list items both for determining
 * the number and the number of visible items.

 * @param state the [LazyGridState] to adapt.
 *
 * @VisibleForTesting
 */
internal class LazyGridStateAdapter(
    private val state: LazyGridState
) : PositionIndicatorState {

    override val positionFraction: Float
        get() {
            return if (state.layoutInfo.visibleItemsInfo.isEmpty()) {
                0.0f
            } else {
                val decimalFirstItemIndex = decimalFirstItemIndex()
                val decimalLastItemIndex = decimalLastItemIndex()

                val decimalLastItemIndexDistanceFromEnd = state.layoutInfo.totalItemsCount -
                        decimalLastItemIndex

                if (decimalFirstItemIndex + decimalLastItemIndexDistanceFromEnd == 0.0f) {
                    0.0f
                } else {
                    decimalFirstItemIndex /
                            (decimalFirstItemIndex + decimalLastItemIndexDistanceFromEnd)
                }
            }
        }

    override fun sizeFraction(scrollableContainerSizePx: Float) =
        if (state.layoutInfo.totalItemsCount == 0) {
            1.0f
        } else {
            val decimalFirstItemIndex = decimalFirstItemIndex()
            val decimalLastItemIndex = decimalLastItemIndex()

            (decimalLastItemIndex - decimalFirstItemIndex) /
                    state.layoutInfo.totalItemsCount.toFloat()
        }

    override fun visibility(scrollableContainerSizePx: Float): PositionIndicatorVisibility {
        return if (sizeFraction(scrollableContainerSizePx) < 0.999f) {
            if (state.isScrollInProgress)
                PositionIndicatorVisibility.Show
            else
                PositionIndicatorVisibility.AutoHide
        } else {
            PositionIndicatorVisibility.Hide
        }
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? LazyGridStateAdapter)?.state == state
    }

    private fun decimalLastItemIndex(): Float {
        if (state.layoutInfo.visibleItemsInfo.isEmpty()) return 0f
        val lastItem = state.layoutInfo.visibleItemsInfo.last()
        // Coerce item sizes to at least 1 to avoid divide by zero for zero height items
        val lastItemVisibleSize =
            (state.layoutInfo.viewportEndOffset - lastItem.offset.toOffset(state.layoutInfo.orientation))
                .coerceAtMost(lastItem.size.toSize(state.layoutInfo.orientation)).coerceAtLeast(1)
        return lastItem.index.toFloat() +
                lastItemVisibleSize.toFloat() / lastItem.size.toSize(state.layoutInfo.orientation)
            .coerceAtLeast(1).toFloat()
    }

    private fun decimalFirstItemIndex(): Float {
        if (state.layoutInfo.visibleItemsInfo.isEmpty()) return 0f
        val firstItem = state.layoutInfo.visibleItemsInfo.first()
        val firstItemOffset =
            firstItem.offset.toOffset(state.layoutInfo.orientation) - state.layoutInfo.viewportStartOffset
        // Coerce item size to at least 1 to avoid divide by zero for zero height items
        return firstItem.index.toFloat() -
                firstItemOffset.coerceAtMost(0).toFloat() /
                firstItem.size.toSize(state.layoutInfo.orientation).coerceAtLeast(1).toFloat()
    }

    private fun IntOffset.toOffset(orientation: Orientation): Int {
        return if (orientation == Orientation.Vertical) {
            this.y
        } else {
            this.x
        }
    }

    private fun IntSize.toSize(orientation: Orientation): Int {
        return if (orientation == Orientation.Vertical) {
            this.height
        } else {
            this.width
        }
    }
}

internal class LazyGridScrollInfoProvider(val state: LazyGridState) : ScrollInfoProvider {
    override val isScrollAwayValid: Boolean
        get() = state.layoutInfo.totalItemsCount > 0
    override val isScrollable: Boolean
        get() = state.layoutInfo.totalItemsCount > 0
    override val isScrollInProgress: Boolean
        get() = state.isScrollInProgress
    override val anchorItemOffset: Float
        get() =
            state.layoutInfo.visibleItemsInfo.firstOrNull()?.let {
                if (it.index != 0) {
                    return@let Float.NaN
                }
                -it.offset.toOffset(state.layoutInfo.orientation).toFloat()
            } ?: Float.NaN
    override val lastItemOffset: Float
        get() {
            val layoutInfo = state.layoutInfo
            val lazyColumnHeightPx = layoutInfo.viewportSize.height
            val reverseLayout = state.layoutInfo.reverseLayout
            return if (reverseLayout) {
                layoutInfo.visibleItemsInfo.firstOrNull()?.let {
                    if (it.index != 0) {
                        return@let 0f
                    }
                    val bottomEdge =
                        -it.offset.toOffset(state.layoutInfo.orientation) + lazyColumnHeightPx + layoutInfo.viewportStartOffset
                    (lazyColumnHeightPx - bottomEdge).toFloat().coerceAtLeast(0f)
                } ?: 0f
            } else {
                layoutInfo.visibleItemsInfo.lastOrNull()?.let {
                    if (it.index != layoutInfo.totalItemsCount - 1) {
                        return@let 0f
                    }
                    val bottomEdge =
                        it.offset.toOffset(state.layoutInfo.orientation) + it.size.toSize(state.layoutInfo.orientation) - layoutInfo.viewportStartOffset
                    (lazyColumnHeightPx - bottomEdge).toFloat().coerceAtLeast(0f)
                } ?: 0f
            }
        }

    private fun IntOffset.toOffset(orientation: Orientation): Int {
        return if (orientation == Orientation.Vertical) {
            this.y
        } else {
            this.x
        }
    }

    private fun IntSize.toSize(orientation: Orientation): Int {
        return if (orientation == Orientation.Vertical) {
            this.height
        } else {
            this.width
        }
    }
}

@Composable
fun rememberLazyGridScrollState(lazyGridState: LazyGridState): ScrollState {
    val scrollState = rememberScrollState()
    val positionState = remember(lazyGridState) { LazyGridStateAdapter(lazyGridState) }

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo }
            .collect { layoutInfo ->
                val positionFraction = positionState.positionFraction

                val viewportSize = if (layoutInfo.orientation == Orientation.Vertical) {
                    layoutInfo.viewportSize.height
                } else {
                    layoutInfo.viewportSize.width
                }

                runCatching {
                    (ScrollState::class.declaredMemberProperties
                        .firstOrNull { it.name == "maxValue" } as? KMutableProperty1<ScrollState, Int>)
                        ?.apply { isAccessible = true }
                        ?.apply { set(scrollState, 100) }
                    (ScrollState::class.declaredMemberProperties
                        .firstOrNull { it.name == "value" } as? KMutableProperty1<ScrollState, Int>)
                        ?.apply { isAccessible = true }
                        ?.apply { set(scrollState, (positionFraction * 100f).toInt()) }
                    (ScrollState::class.declaredMemberProperties
                        .firstOrNull { it.name == "viewportSize" } as? KMutableProperty1<ScrollState, Int>)
                        ?.apply { isAccessible = true }
                        ?.apply { set(scrollState, viewportSize) }
                }
            }
    }

    return scrollState
}