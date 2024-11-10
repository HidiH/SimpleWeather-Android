package com.thewizrd.simpleweather.ui.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.wear.compose.material.PositionIndicatorState
import androidx.wear.compose.material.PositionIndicatorVisibility

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